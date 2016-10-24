package oculusbot.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import oculusbot.basic.StatusThread;

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

	public Mat getFrame() {
		return frame;
	}

	public VideoCaptureThread(int camId) {
		this(camId, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public VideoCaptureThread(int camId, int camWidth, int camHeight) {
		this.camId = camId;
		this.camWidth = camWidth;
		this.camHeight = camHeight;
		camSetup();
	}
	
	private void camSetup(){
		cam = new VideoCapture();
		cam.open(camId);
		cam.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, camWidth);
		cam.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, camHeight);

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
		cam.grab();
		cam.retrieve(buffer);
		frame = buffer;
	}

	@Override
	protected void shutdown() {
		cam.release();
	}
}
