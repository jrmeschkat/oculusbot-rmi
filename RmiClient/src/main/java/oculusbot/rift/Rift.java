package oculusbot.rift;

import static org.lwjgl.ovr.OVR.*;
import static org.lwjgl.ovr.OVRGL.*;
import static org.lwjgl.ovr.OVRUtil.*;
import static org.lwjgl.ovr.OVRErrorCode.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBFramebufferObject.*;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.ovr.OVRDetectResult;
import org.lwjgl.ovr.OVREyeRenderDesc;
import org.lwjgl.ovr.OVRFovPort;
import org.lwjgl.ovr.OVRGL;
import org.lwjgl.ovr.OVRGraphicsLuid;
import org.lwjgl.ovr.OVRHmdDesc;
import org.lwjgl.ovr.OVRInitParams;
import org.lwjgl.ovr.OVRLayerEyeFov;
import org.lwjgl.ovr.OVRLogCallback;
import org.lwjgl.ovr.OVRLogCallbackI;
import org.lwjgl.ovr.OVRMatrix4f;
import org.lwjgl.ovr.OVRMirrorTextureDesc;
import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRQuatf;
import org.lwjgl.ovr.OVRRecti;
import org.lwjgl.ovr.OVRSessionStatus;
import org.lwjgl.ovr.OVRSizei;
import org.lwjgl.ovr.OVRTextureSwapChainDesc;
import org.lwjgl.ovr.OVRTrackingState;
import org.lwjgl.ovr.OVRUtil;
import org.lwjgl.ovr.OVRVector3f;

import oculusbot.opengl.FrameBufferObject;
import oculusbot.opengl.Renderable;
import oculusbot.opengl.renderable.MatCanvas;
import oculusbot.video.ReceiveVideoThread;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;

public class Rift {
	private long session;
	private OVRSessionStatus sessionStatus;
	private OVRHmdDesc hmdDesc;
	private OVRFovPort[] fovPorts = new OVRFovPort[2];
	private OVRPosef[] eyePoses = new OVRPosef[2];
	private OVRMatrix4f[] projections = new OVRMatrix4f[2];
	private OVREyeRenderDesc[] eyeRenderDescs = new OVREyeRenderDesc[2];
	private long chain;
	private FrameBufferObject[] fbos;
	private OVRLayerEyeFov layer0;
	private PointerBuffer layers;
	private int textureWidth;
	private int textureHeight;
	private Renderable canvas;

	public long getSession() {
		return session;
	}

	public void recenter() {
		ovr_RecenterTrackingOrigin(session);
	}

	public Rift(ReceiveVideoThread video) {
		canvas = new MatCanvas(video);
		//check if oculus and services is available
		OVRDetectResult detect = OVRDetectResult.calloc();
		ovr_Detect(0, detect);
		if (!detect.IsOculusHMDConnected() || !detect.IsOculusServiceRunning()) {
			throw new IllegalStateException("Oculus not detected or service not running.");
		} else {
			System.out.println("Oculus connected and service is running.");
		}
		detect.free();

		//init hmd
		OVRInitParams initParams = OVRInitParams.calloc();
		initParams.LogCallback(OVRLogCallback.create(new OVRLogCallbackI() {

			public void invoke(long userData, int level, long message) {
				//TODO maybe extend msg
				System.out.println(memASCII(message));
			}
		}));

		if (ovr_Initialize(initParams) != ovrSuccess) {
			throw new RuntimeException("Couldn't initialize ovr.");
		}
		initParams.free();

		//create hmd
		PointerBuffer hmdPointer = memAllocPointer(1);
		OVRGraphicsLuid luid = OVRGraphicsLuid.calloc();
		if (ovr_Create(hmdPointer, luid) != ovrSuccess) {
			throw new RuntimeException("Couldn't create hmd.");
		}
		session = hmdPointer.get(0);
		memFree(hmdPointer);
		luid.free();

		sessionStatus = OVRSessionStatus.calloc();

		//get hmd desc
		hmdDesc = OVRHmdDesc.malloc();
		ovr_GetHmdDesc(session, hmdDesc);
		System.out.println("OVR: " + hmdDesc.ManufacturerString() + " - " + hmdDesc.ProductNameString() + "\n");
		if (hmdDesc.Type() == ovrHmd_None) {
			throw new RuntimeException("Couldn't create correct hmd. Might be not correct initilialzed.");
		}

		//FOV, projections and renderDesc
		for (int eye = 0; eye < 2; eye++) {
			fovPorts[eye] = hmdDesc.DefaultEyeFov(eye);
			projections[eye] = OVRMatrix4f.malloc();
			ovrMatrix4f_Projection(fovPorts[eye], 0.5f, 500f, ovrProjection_None, projections[eye]);
			eyeRenderDescs[eye] = OVREyeRenderDesc.malloc();
			ovr_GetRenderDesc(session, eye, fovPorts[eye], eyeRenderDescs[eye]);

			System.out.println("eye " + eye + " = " + fovPorts[eye].UpTan() + ", " + fovPorts[eye].DownTan() + ", "
					+ fovPorts[eye].LeftTan() + ", " + fovPorts[eye].RightTan());
			System.out.println("ipd eye " + eye + " = " + eyeRenderDescs[eye].HmdToEyeOffset().x());
		}

		//recenter view
		ovr_RecenterTrackingOrigin(session);
	}

	public void init() {

		float pixelsPerDisplayPixel = 1;
		OVRSizei leftTextureSize = OVRSizei.malloc();
		ovr_GetFovTextureSize(session, ovrEye_Left, fovPorts[ovrEye_Left], pixelsPerDisplayPixel, leftTextureSize);

		OVRSizei rightTextureSize = OVRSizei.malloc();
		ovr_GetFovTextureSize(session, ovrEye_Right, fovPorts[ovrEye_Right], pixelsPerDisplayPixel, rightTextureSize);

		textureWidth = (leftTextureSize.w() + rightTextureSize.w());
		textureHeight = Math.max(leftTextureSize.h(), rightTextureSize.h());
		System.out.println("Texture size: " + textureWidth + " x " + textureHeight);

		leftTextureSize.free();
		rightTextureSize.free();

		OVRTextureSwapChainDesc swapChainDesc = OVRTextureSwapChainDesc.calloc().Type(ovrTexture_2D).ArraySize(1)
				.Format(OVR_FORMAT_R8G8B8A8_UNORM_SRGB).Width(textureWidth).Height(textureHeight).MipLevels(1)
				.SampleCount(1).StaticImage(false);

		PointerBuffer textureSetPointerBuffer = BufferUtils.createPointerBuffer(1);
		int error = 0;
		if ((error = ovr_CreateTextureSwapChainGL(session, swapChainDesc, textureSetPointerBuffer)) != ovrSuccess) {
			throw new IllegalStateException("Couldn't create swap texture set. Error: " + error);
		}

		chain = textureSetPointerBuffer.get(0);
		swapChainDesc.free();

		int chainLength = 0;
		IntBuffer chainLengthBuffer = BufferUtils.createIntBuffer(1);
		ovr_GetTextureSwapChainLength(session, chain, chainLengthBuffer);
		chainLength = chainLengthBuffer.get();

		fbos = new FrameBufferObject[chainLength];
		for (int i = 0; i < chainLength; i++) {
			IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
			OVRGL.ovr_GetTextureSwapChainBufferGL(session, chain, i, textureBuffer);
			int texture = textureBuffer.get();
			fbos[i] = new FrameBufferObject(textureWidth, textureHeight, texture);
		}

		OVRRecti[] viewports = new OVRRecti[2];
		for (int eye = 0; eye < 2; eye++) {
			viewports[eye] = OVRRecti.calloc();
		}

		viewports[ovrEye_Left].Pos().x(0).y(0);
		viewports[ovrEye_Left].Size().w(textureWidth / 2).h(textureHeight);
		viewports[ovrEye_Right].Pos().x(textureWidth / 2).y(0);
		viewports[ovrEye_Right].Size().w(textureWidth / 2).h(textureHeight);

		layer0 = OVRLayerEyeFov.calloc();
		layer0.Header().Type(ovrLayerType_EyeFov);
		layer0.Header().Flags(ovrLayerFlag_TextureOriginAtBottomLeft);
		for (int eye = 0; eye < 2; eye++) {
			layer0.ColorTexture(textureSetPointerBuffer);
			layer0.Viewport(eye, viewports[eye]);
			layer0.Fov(eye, fovPorts[eye]);
			viewports[eye].free();
		}

		layers = BufferUtils.createPointerBuffer(1);
		layers.put(0, layer0);

		canvas.init();
	}

	public void render() {
		System.gc();
		ovr_GetSessionStatus(session, sessionStatus);
		if (!sessionStatus.IsVisible() || sessionStatus.ShouldQuit()) {
			return;
		}

		if (sessionStatus.ShouldRecenter()) {
			ovr_RecenterTrackingOrigin(session);
		}

		double timing = ovr_GetPredictedDisplayTime(session, 0);
		OVRTrackingState trackingState = OVRTrackingState.malloc();
		ovr_GetTrackingState(session, timing, true, trackingState);
		OVRPosef headPose = trackingState.HeadPose().ThePose();
		trackingState.free();

		OVRVector3f.Buffer hmdToEyeViewOffsets = OVRVector3f.calloc(2);
		hmdToEyeViewOffsets.put(0, eyeRenderDescs[ovrEye_Left].HmdToEyeOffset());
		hmdToEyeViewOffsets.put(1, eyeRenderDescs[ovrEye_Right].HmdToEyeOffset());

		OVRPosef.Buffer outEyePoses = OVRPosef.create(2);
		ovr_CalcEyePoses(headPose, hmdToEyeViewOffsets, outEyePoses);
		eyePoses[ovrEye_Left] = outEyePoses.get(0);
		eyePoses[ovrEye_Right] = outEyePoses.get(1);

		for (int eye = 0; eye < 2; eye++) {
			OVRPosef eyePose = eyePoses[eye];
			layer0.RenderPose(eye, eyePose);

			IntBuffer indexBuffer = BufferUtils.createIntBuffer(1);
			ovr_GetTextureSwapChainCurrentIndex(session, chain, indexBuffer);
			int index = indexBuffer.get();

			fbos[index].bind();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glViewport(0, 0, textureWidth, textureHeight);
			canvas.render();
			fbos[index].unbind();
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		ovr_CommitTextureSwapChain(session, chain);
		int result = ovr_SubmitFrame(session, 0, null, layers);
		if (result == ovrSuccess_NotVisible) {
			System.out.println("FRAME NOT VISIBLE");
		}
		if (result != ovrSuccess) {
			System.err.println("FRAME SUBMIT FAILED!");
		}
	}

	public boolean destroy() throws NullPointerException {
		for (OVRMatrix4f projection : projections) {
			projection.free();
		}

		for (OVREyeRenderDesc eyeRenderDesc : eyeRenderDescs) {
			eyeRenderDesc.free();
		}

		layer0.free();
		sessionStatus.free();
		canvas.destroy();
		hmdDesc.free();

		if (chain != 0) {
			ovr_DestroyTextureSwapChain(session, chain);
		}

		ovr_Destroy(session);
		ovr_Shutdown();
		return true;
	}

	public int getMirrorFramebuffer(int width, int height) {
		OVRMirrorTextureDesc mirrorDesc = OVRMirrorTextureDesc.calloc().Format(OVR_FORMAT_R8G8B8A8_UNORM_SRGB)
				.Width(width).Height(height);

		PointerBuffer mirrorTexture = BufferUtils.createPointerBuffer(1);
		if (OVRGL.ovr_CreateMirrorTextureGL(session, mirrorDesc, mirrorTexture) != ovrSuccess) {
			throw new RuntimeException("Couldn't create mirror texture.");
		}
		mirrorDesc.free();

		IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
		OVRGL.ovr_GetMirrorTextureBufferGL(session, mirrorTexture.get(0), textureBuffer);
		int texture = textureBuffer.get();

		int framebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer);
		glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
		glFramebufferRenderbuffer(GL_READ_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, 0);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

		return framebuffer;
	}

}
