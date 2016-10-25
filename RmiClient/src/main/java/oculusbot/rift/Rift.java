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
import org.lwjgl.ovr.OVRMirrorTextureDesc;
import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRRecti;
import org.lwjgl.ovr.OVRSessionStatus;
import org.lwjgl.ovr.OVRSizei;
import org.lwjgl.ovr.OVRTextureSwapChainDesc;
import org.lwjgl.ovr.OVRTrackingState;
import org.lwjgl.ovr.OVRVector3f;

import oculusbot.opengl.FrameBufferObject;
import oculusbot.opengl.Renderable;
import oculusbot.opengl.renderable.MatCanvas;
import oculusbot.video.Frame;
import oculusbot.video.ReceiveVideoThread;

import org.lwjgl.PointerBuffer;

/**
 * Class to interact with the OculusRift using the LWJGL LibOVR bindings.
 * 
 * @author Robert Meschkat
 *
 */
public class Rift {
	private long session;
	private OVRSessionStatus sessionStatus;
	private OVRHmdDesc hmdDesc;
	private OVRFovPort[] fovPorts = new OVRFovPort[2];
	private OVRPosef[] eyePoses = new OVRPosef[2];
	private OVREyeRenderDesc[] eyeRenderDescs = new OVREyeRenderDesc[2];
	private long chain;
	private FrameBufferObject[] fbos;
	private OVRLayerEyeFov layer0;
	private PointerBuffer layers;
	private int textureWidth;
	private int textureHeight;
	private Renderable canvas;
	private boolean showLatency = false;

	/**
	 * Returns the session handle that identifies the current Rift instance.
	 * 
	 * @return
	 */
	public long getSession() {
		return session;
	}

	/**
	 * Recenters the tracking origin so that the current orientation is the new
	 * center.
	 */
	public void recenter() {
		ovr_RecenterTrackingOrigin(session);
	}

	/**
	 * Creates a rift object and determines if the latency should be printed to
	 * the console.
	 * 
	 * @param video
	 *            Video thread that delivers image data.
	 * @param showLatency
	 *            If true the latency will be printed to stdout
	 */
	public Rift(ReceiveVideoThread video, boolean showLatency) {
		this(video);
		this.showLatency = showLatency;
	}

	/**
	 * Creates a rift object.
	 * 
	 * @param video
	 *            Video thread that delivers image data.
	 */
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

		//initialize LibOVR
		OVRInitParams initParams = OVRInitParams.calloc();
		//determine how the LibOVR should handle logging 
		initParams.LogCallback(OVRLogCallback.create(new OVRLogCallbackI() {

			public void invoke(long userData, int level, long message) {
				System.out.println(memASCII(message));
			}
		}));

		if (ovr_Initialize(initParams) != ovrSuccess) {
			throw new RuntimeException("Couldn't initialize LibOVR.");
		}
		initParams.free();

		//create HMD session handle
		PointerBuffer hmdPointer = memAllocPointer(1);
		OVRGraphicsLuid luid = OVRGraphicsLuid.calloc(); //LUID = locally unique identifier 
		if (ovr_Create(hmdPointer, luid) != ovrSuccess) {
			throw new RuntimeException("Couldn't create HMD.");
		}
		session = hmdPointer.get(0);
		memFree(hmdPointer);
		luid.free();

		//used during rendering to check if frame should be rendered
		sessionStatus = OVRSessionStatus.calloc();

		//get HMD description
		hmdDesc = OVRHmdDesc.malloc();
		ovr_GetHmdDesc(session, hmdDesc);
		//print some information and check if HMD was initialized correctly
		System.out.println("OVR: " + hmdDesc.ManufacturerString() + " - " + hmdDesc.ProductNameString() + "\n");
		if (hmdDesc.Type() == ovrHmd_None) {
			throw new RuntimeException("Couldn't create correct HMD. Might be not correct initilialzed.");
		}

		//FOV, projections and renderDesc
		for (int eye = 0; eye < 2; eye++) {
			//initialize field of view (FOV) with default values 
			fovPorts[eye] = hmdDesc.DefaultEyeFov(eye);

			//get render information
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

		//calculate viewport size for each eye
		float pixelsPerDisplayPixel = 1;
		OVRSizei leftTextureSize = OVRSizei.malloc();
		ovr_GetFovTextureSize(session, ovrEye_Left, fovPorts[ovrEye_Left], pixelsPerDisplayPixel, leftTextureSize);

		OVRSizei rightTextureSize = OVRSizei.malloc();
		ovr_GetFovTextureSize(session, ovrEye_Right, fovPorts[ovrEye_Right], pixelsPerDisplayPixel, rightTextureSize);

		//calculate the complete texture size for rendering
		textureWidth = (leftTextureSize.w() + rightTextureSize.w());
		textureHeight = Math.max(leftTextureSize.h(), rightTextureSize.h());
		System.out.println("Texture size: " + textureWidth + " x " + textureHeight);

		leftTextureSize.free();
		rightTextureSize.free();

		//create a texture swap chain
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

		//get texture swap chain length
		int chainLength = 0;
		IntBuffer chainLengthBuffer = BufferUtils.createIntBuffer(1);
		ovr_GetTextureSwapChainLength(session, chain, chainLengthBuffer);
		chainLength = chainLengthBuffer.get();
		System.out.println("Texture swap chain length: " + chainLength);

		//create a FBO for each element of the texture swap chain
		fbos = new FrameBufferObject[chainLength];
		for (int i = 0; i < chainLength; i++) {
			IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
			OVRGL.ovr_GetTextureSwapChainBufferGL(session, chain, i, textureBuffer);
			int texture = textureBuffer.get();
			fbos[i] = new FrameBufferObject(textureWidth, textureHeight, texture);
		}

		//create the viewports
		OVRRecti[] viewports = new OVRRecti[2];
		for (int eye = 0; eye < 2; eye++) {
			viewports[eye] = OVRRecti.calloc();
		}

		//x-coordinate is the left side of the texture
		viewports[ovrEye_Left].Pos().x(0).y(0);
		viewports[ovrEye_Left].Size().w(textureWidth / 2).h(textureHeight);
		//x-coordinate is moved by the width of the left texture
		viewports[ovrEye_Right].Pos().x(textureWidth / 2).y(0);
		viewports[ovrEye_Right].Size().w(textureWidth / 2).h(textureHeight);

		//create a layer as a canvas for the video. only one layer is 
		//required since there will be no HUD or anything else.
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
		//check if the frame should be rendered
		ovr_GetSessionStatus(session, sessionStatus);
		if (!sessionStatus.IsVisible() || sessionStatus.ShouldQuit()) {
			return;
		}

		//recenter if necessary
		if (sessionStatus.ShouldRecenter()) {
			ovr_RecenterTrackingOrigin(session);
		}

		//get the orientation of the HMD
		double timing = ovr_GetPredictedDisplayTime(session, 0);
		OVRTrackingState trackingState = OVRTrackingState.malloc();
		ovr_GetTrackingState(session, timing, true, trackingState);
		OVRPosef headPose = trackingState.HeadPose().ThePose();
		trackingState.free();

		//calculate new eye positions
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

			//get the index of the currently used buffer
			IntBuffer indexBuffer = BufferUtils.createIntBuffer(1);
			ovr_GetTextureSwapChainCurrentIndex(session, chain, indexBuffer);
			int index = indexBuffer.get();

			//RENDERING TO FRAMEBUFFER
			fbos[index].bind();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glViewport(0, 0, textureWidth, textureHeight);
			canvas.render();
			fbos[index].unbind();
		}
		//unbind FBO and texture to be sure
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		//update texture swap chain and send frame to HMD
		ovr_CommitTextureSwapChain(session, chain);
		int result = ovr_SubmitFrame(session, 0, null, layers);
		//check if frame was sent correctly and print latency
		if (result == ovrSuccess_NotVisible) {
			System.out.println("FRAME NOT VISIBLE");
		} else if (result != ovrSuccess) {
			System.err.println("FRAME SUBMIT FAILED!");
		} else {
			if (showLatency && canvas instanceof MatCanvas) {
				Frame frame = ((MatCanvas) canvas).getFrame();
				double latency = frame.getLatency(System.nanoTime());
				System.out.println("Latency (ms): " + latency);
			}
		}
	}

	/**
	 * Frees the memory and destroys the HMD session
	 * 
	 * @return
	 * @throws NullPointerException
	 */
	public boolean destroy() throws NullPointerException {

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

	/**
	 * Creates a FBO that mirrors the content of the HMD so it can be rendered
	 * to a mirror window on a second display.
	 * 
	 * @param width
	 *            Width of the FBO
	 * @param height
	 *            Height of the FBO
	 * @return
	 */
	public int getMirrorFramebuffer(int width, int height) {
		//create a mirror texture handle
		OVRMirrorTextureDesc mirrorDesc = OVRMirrorTextureDesc.calloc().Format(OVR_FORMAT_R8G8B8A8_UNORM_SRGB)
				.Width(width).Height(height);

		PointerBuffer mirrorTexture = BufferUtils.createPointerBuffer(1);
		if (OVRGL.ovr_CreateMirrorTextureGL(session, mirrorDesc, mirrorTexture) != ovrSuccess) {
			throw new RuntimeException("Couldn't create mirror texture.");
		}
		mirrorDesc.free();

		//create the mirror texture as an OpenGL handle
		IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
		OVRGL.ovr_GetMirrorTextureBufferGL(session, mirrorTexture.get(0), textureBuffer);
		int texture = textureBuffer.get();

		//bind the texture to the mirror FBO
		int framebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer);
		glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
		glFramebufferRenderbuffer(GL_READ_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, 0);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

		return framebuffer;
	}

}
