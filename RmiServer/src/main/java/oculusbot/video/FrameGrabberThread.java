package oculusbot.video;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import oculusbot.basic.Status;
import oculusbot.basic.StatusThread;

/**
 * Thread that starts two video threads and combines their images.
 * 
 * @author Robert Meschkat
 *
 */
public class FrameGrabberThread extends StatusThread {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * Limit that ensures that the data still fits in an UDP-packet.
	 */
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
	private long timeStamp;
	/**
	 * Border used to prevent texture filtering and chromatic aberration.
	 * 
	 * @see org.lwjgl.ovr.OVR#ovr_GetFovTextureSize(long, int,
	 *      org.lwjgl.ovr.OVRFovPort, float, org.lwjgl.ovr.OVRSizei)
	 *      ovr_GetFovTextureSize
	 */
	private Mat border = null;

	/**
	 * Returns the older time stamp of both frames.
	 * 
	 * @return
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Creates a frame grabber thread with default camera resolutions and IDs.
	 */
	public FrameGrabberThread() {
		this(0, 0, 0, 1);
	}

	/**
	 * Creates a frame grabber thread with specific camera resolutions.
	 * 
	 * @param camWidth
	 *            Width for each {@link VideoCaptureThread}
	 * @param camHeight
	 *            Height for each {@link VideoCaptureThread}
	 * @param camIdLeft
	 *            Device ID for the left camera
	 * @param camIdRight
	 *            Device ID for the right camera
	 */
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

	/**
	 * Switches how the frames of both cameras are combined.
	 */
	public void switchCameras() {
		switchCams = !switchCams;
	}

	/**
	 * Returns the current frame as byte array. Cuts of everything thats larger than {@value #MAX_DATA_SIZE}.
	 * 
	 * @return
	 */
	public byte[] grabFrameAsByte() {
		try {
			byte[] data = buffer.toArray();
			//remove everything that is over the limit
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
			//start camera threads with specific resolution
			leftThread = new VideoCaptureThread(camIdLeft, camWidth, camHeight);
			rightThread = new VideoCaptureThread(camIdRight, camWidth, camHeight);
		} else {
			//start camera threads with default resolution
			leftThread = new VideoCaptureThread(camIdLeft);
			rightThread = new VideoCaptureThread(camIdRight);
		}
		leftThread.start();
		rightThread.start();
	}

	@Override
	protected void task() {
		//create buffers for the result, the left and the right frame
		Mat m = new Mat();
		Mat left = new Mat();
		Mat right = new Mat();
		//get frames in the correct order
		if (switchCams) {
			left = leftThread.getFrame();
			right = rightThread.getFrame();
		} else {
			right = leftThread.getFrame();
			left = rightThread.getFrame();
		}
		//check if frames contain data
		if (left == null || left.empty() || right == null || right.empty()) {
			return;
		}

		if (border == null) {
			int rows = Math.max(left.rows(), right.rows());
			border = new Mat(rows, 8, left.type());
		}

		//combine the time stamps 
		timeStamp = Math.min(leftThread.getTimeStamp(), rightThread.getTimeStamp());

		//concatenate both images and save the result in the buffer
		Core.hconcat(Arrays.asList(new Mat[] { left, border, right }), m);
		//create a buffer and options for compression
		MatOfByte buf = new MatOfByte();
		MatOfInt params = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, QUALITY);
		//compress and save the image
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
