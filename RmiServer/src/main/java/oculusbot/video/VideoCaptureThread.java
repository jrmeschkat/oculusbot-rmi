package oculusbot.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import oculusbot.basic.StatusThread;

/**
 * Thread that captures frames from a camera.
 * 
 * @author Robert Meschkat
 *
 */
public class VideoCaptureThread extends StatusThread {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 300;

	private VideoCapture cam;
	private Mat frame;
	private int camId;
	private int count = 0;
	private int camWidth;
	private int camHeight;
	private long timeStamp = 0;

	/**
	 * Returns the time in nano seconds (System.nanoSeconds()) when the current
	 * frame was grabbed.
	 * 
	 * @return
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns the image information.
	 * 
	 * @return
	 */
	public Mat getFrame() {
		return frame;
	}

	/**
	 * Creates a video thread with the default width and height.
	 * 
	 * @param camId
	 *            ID of the camera
	 */
	public VideoCaptureThread(int camId) {
		this(camId, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Creates a video thread with specific width and height and runs the cam
	 * setup.
	 * 
	 * @param camId
	 *            ID of the camera
	 * @param camWidth
	 * @param camHeight
	 */
	public VideoCaptureThread(int camId, int camWidth, int camHeight) {
		this.camId = camId;
		this.camWidth = camWidth;
		this.camHeight = camHeight;
		camSetup();
	}

	/**
	 * Tries to open a camera and to set its resolution.
	 */
	private void camSetup() {
		//open camera and set resolution
		cam = new VideoCapture();
		cam.open(camId);
		cam.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, camWidth);
		cam.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, camHeight);

		//check if camera is open
		if (!cam.isOpened()) {
			throw new IllegalStateException("Couldn't open cam: " + camId);
		}
	}

	@Override
	protected void setup() {
	}

	@Override
	protected void task() {
		if (count++ > 100) {
			System.gc();
			count = 0;
		}
		Mat buffer = new Mat();
		//grab a frame
		cam.grab();
		//save the grabbed frame in the buffer
		cam.retrieve(buffer);
		frame = buffer;
		//take a time stamp for latency measurement
		timeStamp = System.nanoTime();
	}

	@Override
	protected void shutdown() {
		System.out.println("Shutdown cam " + camId);
		cam.release();
		while (cam.isOpened()) {
		}
	}
}
