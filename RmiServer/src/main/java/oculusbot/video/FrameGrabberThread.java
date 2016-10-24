package oculusbot.video;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import oculusbot.basic.Status;
import oculusbot.basic.StatusThread;

public class FrameGrabberThread extends StatusThread {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static final int MAX_DATA_SIZE = 65000;
	private static final int QUALITY = 18;
	private MatOfByte buffer;
	private VideoCaptureThread leftThread;
	private VideoCaptureThread rightThread;
	private boolean switchCams = false;
	private int camWidth;
	private int camHeight;
	private int camIdLeft;
	private int camIdRight;
	/**
	 * Border used to prevent texture filtering and chromatic aberration.
	 * @see org.lwjgl.ovr.OVR#ovr_GetFovTextureSize(long, int, org.lwjgl.ovr.OVRFovPort, float, org.lwjgl.ovr.OVRSizei) ovr_GetFovTextureSize
	 */
	private Mat border = null;

	public FrameGrabberThread() {
		this(0, 0, 0, 1);
	}

	public FrameGrabberThread(int camWidth, int camHeight, int camIdLeft, int camIdRight) {
		this.camWidth = camWidth;
		this.camHeight = camHeight;
		this.camIdLeft = camIdLeft;
		this.camIdRight = camIdRight;
	}

	@Override
	public Status getStatus() {
		return passthroughStatus(leftThread, rightThread);
	}

	public void switchCameras() {
		switchCams = !switchCams;
	}

	public byte[] grabFrameAsByte() {
		try {
			byte[] data = buffer.toArray();
			if(data.length > MAX_DATA_SIZE){
				data = new byte[MAX_DATA_SIZE];
				System.arraycopy(buffer.toArray(), 0, data, 0, data.length);
				msg("WARNING: Image to big. Data will be lost.");
			}
			
			return data;
		} catch (RuntimeException e) {
			return null;
		}

	}

	@Override
	protected void setup() {
		this.buffer = new MatOfByte();
		if (camWidth > 0 && camHeight > 0) {
			leftThread = new VideoCaptureThread(camIdLeft, camWidth, camHeight);
			rightThread = new VideoCaptureThread(camIdRight, camWidth, camHeight);
		} else {
			leftThread = new VideoCaptureThread(camIdLeft);
			rightThread = new VideoCaptureThread(camIdRight);
		}
		leftThread.start();
		rightThread.start();
	}

	@Override
	protected void task() {
		Mat m = new Mat();
		Mat left = new Mat();
		Mat right = new Mat();
		if (switchCams) {
			left = leftThread.getFrame();
			right = rightThread.getFrame();
		} else {
			right = leftThread.getFrame();
			left = rightThread.getFrame();
		}

		if (left == null || left.empty() || right == null || right.empty()) {
			return;
		}

		if(border == null){
			int rows = Math.max(left.rows(), right.rows());
			border = new Mat(rows, 8, left.type());
		}
		
		Core.hconcat(Arrays.asList(new Mat[] { left, right }), m);
		MatOfByte buf = new MatOfByte();
		MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, QUALITY);
		Imgcodecs.imencode(".jpg", m, buf, params);
		buffer = buf;
	}

	@Override
	protected void shutdown() {
		leftThread.interrupt();
		rightThread.interrupt();
		waitForClosingThreads(leftThread, rightThread);
	}

}
