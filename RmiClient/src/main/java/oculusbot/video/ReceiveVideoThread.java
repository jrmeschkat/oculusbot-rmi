package oculusbot.video;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import oculusbot.network.NetworkThread;
import oculusbot.network.test.PingThread;

/**
 * Receives the video data from the two cameras on the bot.
 * 
 * @author Robert Meschkat
 *
 */
public class ReceiveVideoThread extends NetworkThread {
	/**
	 * The maximum packet size which can be received.
	 */
	private static final int MAX_PACKET_SIZE = 65000;

	private Frame frame;
	private PingThread ping;

	/**
	 * Returns the last received frame.
	 * 
	 * @return
	 */
	public Frame getFrame() {
		return frame;
	}

	/**
	 * Create a thread to receive video from the bot.
	 * 
	 * @param port
	 *            The port to use.
	 * @param ip
	 *            IP of the server
	 */
	public ReceiveVideoThread(int port, String ip) {
		super(port);
		ping = new PingThread(ip, MAX_PACKET_SIZE);
	}

	@Override
	protected void setup() {
		super.setup();
		setPacketSize(MAX_PACKET_SIZE);
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		DatagramPacket packet;
		long timeReceived = 0;
		try {
			packet = receive();
			//save the time the packet was received
			timeReceived = System.nanoTime();
		} catch (SocketTimeoutException e) {
			return;
		}

		//separate data from elapsed time
		byte[] data = packet.getData();
		long timeElapsed = byteArrayToLong(Arrays.copyOf(data, Long.BYTES));
		data = Arrays.copyOfRange(data, Long.BYTES, data.length);

		//save all information in an object
		frame = new Frame(data, timeElapsed, timeReceived, ping.getPing());

	}

	@Override
	protected void shutdown() {
		super.shutdown();
		ping.interrupt();
	}

	/**
	 * Converts byte array to a long.
	 * 
	 * @param b
	 * @return
	 */
	private long byteArrayToLong(byte[] b) {
		long result = 0;

		for (int i = 0; i < b.length; i++) {
			result += ((long) b[i] & 0xff) << (i * 8);
		}

		return result;
	}
}
