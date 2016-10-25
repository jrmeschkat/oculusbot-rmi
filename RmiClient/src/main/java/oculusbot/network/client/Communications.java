package oculusbot.network.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static oculusbot.network.NetworkConstants.*;

/**
 * All outgoing network communications for the client. This uses the standard
 * Java DatagramSocket.
 * 
 * @author Robert Meschkat
 *
 */
public class Communications {
	private DatagramSocket socket;
	private int port;
	private byte[] buf;
	private DatagramPacket in;
	private String server;

	/**
	 * Prepares the communications by creating a DatagramSocket.
	 * 
	 * @param port
	 *            Port used for communication.
	 */
	public Communications(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(DEFAULT_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Same as the other constructor but with a static IP.
	 * 
	 * @see #Communications(int)
	 * @param port
	 *            Port used for communication.
	 * @param server
	 *            Host IP.
	 */
	public Communications(int port, String server) {
		this(port);
		this.server = server;
	}

	/**
	 * Send a broadcast to the network to discover the server and save its IP.
	 * 
	 * @return
	 */
	public InetAddress getServerIP() {
		contactServer(OB_REQUEST_SERVER_IP, BROADCAST_IP, true);
		server = in.getAddress().getHostAddress();
		System.out.println("SERVER:" + server);
		return in.getAddress();
	}

	/**
	 * Used to register this client at the server.
	 */
	public void registerClient() {
		checkServerIp();
		contactServer(OB_REGISTER_CLIENT, server, false);
	}

	/**
	 * Used to unregister this client at the server.
	 */
	public void unregisterClient() {
		checkServerIp();
		contactServer(OB_UNREGISTER_CLIENT, server, false);
	}

	/**
	 * Used to send a keyboard input to the server.
	 * 
	 * @param key
	 *            Key to send
	 */
	public void sendKey(int key) {
		checkServerIp();
		contactServer(OB_SEND_KEY + " " + key, server, false);
	}

	/**
	 * Sends position data from the
	 * {@link oculusbot.network.client.SendPositionDataThread
	 * SendPositionDataThread} to the server.
	 * 
	 * @param yaw
	 * @param pitch
	 * @param roll
	 */
	public void sendPosition(double yaw, double pitch, double roll) {
		checkServerIp();
		String data = OB_POSITION_DATA + " " + yaw + " " + pitch + " " + roll;
		try {
			send(data, server, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if server IP has been set. If not a broadcast is used to discover
	 * it.
	 */
	private void checkServerIp() {
		if (server == null) {
			getServerIP();
		}
	}

	/**
	 * Sends data to the server and waits for an ACK. If no acknowledge-message
	 * is received then the data will be send again.
	 * 
	 * @param data Data to send
	 * @param ip receiver IP
	 * @param broadcast if true the message will be send as a broadcast
	 */
	private void contactServer(String data, String ip, boolean broadcast) {
		try {
			socket.setBroadcast(broadcast);

			while (true) {
				send(data, ip);

				if (waitForAck()) {
					return;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void send(String data, String ip) throws IOException {
		send(data, ip, true);
	}

	/**
	 * Creates a DatagramPacket and sends it to the server.
	 * 
	 * @param data
	 *            Data to send.
	 * @param ip
	 *            IP of the receiver.
	 * @param printMsg
	 *            If true a message will be printed to the console.
	 * @throws IOException
	 */
	private void send(String data, String ip, boolean printMsg) throws IOException {
		if (printMsg) {
			System.out.print("Sending " + data + "...");
		}
		buf = data.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
		socket.send(packet);
		if (printMsg) {
			System.out.println("DONE");
		}
	}

	/**
	 * Waits for an ACKNOWLEDGE message from the server.
	 * 
	 * @return true if ACK is received else false.
	 * @throws IOException
	 */
	private boolean waitForAck() throws IOException {
		System.out.print("Waiting for answer...");
		buf = new byte[DEFAULT_PACKAGE_SIZE];
		in = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(in);
			String msg = new String(in.getData()).trim();
			if (msg.equals(OB_ACK)) {
				System.out.println("DONE");
				return true;
			} else {
				System.err.println("ERROR: Unknown answer.");
			}
		} catch (SocketTimeoutException e) {
			System.err.println("ERROR: No response.");
		}
		return false;
	}

}
