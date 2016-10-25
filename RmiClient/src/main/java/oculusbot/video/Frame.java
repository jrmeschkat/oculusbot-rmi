package oculusbot.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Contains all information received by
 * {@link oculusbot.video.ReceiveVideoThread ReceiveVideoThread} and the ping
 * time.
 * 
 * @author Robert Meschkat
 *
 */
public class Frame {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private Mat mat;
	private long timeElapsed;
	private long timeReceived;
	private double ping;

	/**
	 * Returns the image data as Mat.
	 * 
	 * @return
	 */
	public Mat getMat() {
		return mat;
	}

	/**
	 * Saves all received data and the current ping. Image information will be
	 * converted to a Mat.
	 * 
	 * @param data
	 *            Received data
	 * @param timeElapsed
	 *            Time elapsed before sending.
	 * @param timeReceived
	 *            Time when the packet was received in nano seconds
	 *            (System.nanoSeconds()).
	 * @param ping
	 *            The current ping
	 */
	public Frame(byte[] data, long timeElapsed, long timeReceived, double ping) {
		mat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
		this.timeElapsed = timeElapsed;
		this.timeReceived = timeReceived;
		this.ping = ping;
	}

	/**
	 * Calculates the latency. The parameter is the stop time for the
	 * measurement.
	 * 
	 * @param time
	 *            Stop time in nano seconds (System.nanoSeconds())
	 * @return
	 */
	public double getLatency(long time) {
		return (((time - timeReceived) + timeElapsed) / 1000000d) + ping;
	}
}
