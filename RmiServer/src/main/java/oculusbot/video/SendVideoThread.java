package oculusbot.video;

import java.io.IOException;
import java.util.LinkedList;

import oculusbot.basic.Status;
import oculusbot.network.NetworkThread;

/**
 * Thread that sends the frames to each client.
 * 
 * @author Robert Meschkat
 *
 */
public class SendVideoThread extends NetworkThread {
	private FrameGrabberThread frameGrabber;
	private LinkedList<String> clients;
	private int camWidth = 0;
	private int camHeight = 0;
	private int camIdLeft = 0;
	private int camIdRight = 1;

	/**
	 * Creates the send video thread.
	 * 
	 * @param port
	 *            Port used for sending
	 * @param camWidth
	 *            Width for each camera
	 * @param camHeight
	 *            Height for each camera
	 * @param camIdLeft
	 *            ID that identifies the left camera
	 * @param camIdRight
	 *            ID that identifies the right camera
	 */
	public SendVideoThread(int port, int camWidth, int camHeight, int camIdLeft, int camIdRight) {
		super(port, false);
		clients = new LinkedList<>();
		this.camWidth = camWidth;
		this.camHeight = camHeight;
		this.camIdLeft = camIdLeft;
		this.camIdRight = camIdRight;
	}

	@Override
	public Status getStatus() {
		return passthroughStatus(frameGrabber);
	}

	@Override
	protected void setup() {
		super.setup();
		if (camHeight > 0 && camWidth > 0) {
			frameGrabber = new FrameGrabberThread(camWidth, camHeight, camIdLeft, camIdRight);
		} else {
			frameGrabber = new FrameGrabberThread();
		}
		frameGrabber.start();
	}

	/**
	 * Tells the FrameGrabber to switch image position before concatenation.
	 */
	public void switchCameras() {
		frameGrabber.switchCameras();
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		//do nothing if no client is registered
		if (clients.isEmpty()) {
			pause(100);
			return;
		}
		
		//get every information for packet
		byte[] frame = frameGrabber.grabFrameAsByte();
		byte[] timeElapsed = longToByteArray(System.nanoTime() - frameGrabber.getTimeStamp());
		
		//concatenate the data into one array
		//elapsed time for this operation negligible
		byte[] data = new byte[frame.length + Long.BYTES];
		System.arraycopy(timeElapsed, 0, data, 0, timeElapsed.length);
		System.arraycopy(frame, 0, data, timeElapsed.length, frame.length);
		
		//send if there is a correct frame
		if (frame != null) {
			for (String ip : clients) {
				send(data, ip, port);
			}
		}
	}

	@Override
	protected void shutdown() {
		frameGrabber.interrupt();
		waitForClosingThreads(frameGrabber);
	}

	/**
	 * Adds a client to the receiver list.
	 * 
	 * @param ip
	 *            IP of the client.
	 */
	public void registerClient(String ip) {
		for (String client : clients) {
			if (client.equals(ip)) {
				return;
			}
		}
		clients.add(ip);
	}

	/**
	 * Removes a client from the receiver list. 
	 * 
	 * @param ip
	 *            IP of the client.
	 */
	public void unregisterClient(String ip) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).equals(ip)) {
				clients.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Converts a long value to a byte array for sending.
	 * 
	 * @param l
	 *            Value to convert.
	 * @return
	 */
	private byte[] longToByteArray(long l) {
		byte[] result = new byte[Long.BYTES];

		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (l >> (i * 8));
		}

		return result;
	}

}
