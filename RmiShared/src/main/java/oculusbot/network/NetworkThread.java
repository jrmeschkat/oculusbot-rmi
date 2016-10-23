package oculusbot.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import oculusbot.basic.StatusThread;

import static oculusbot.network.NetworkConstants.*;

/**
 * Extended version of {@link oculusbot.basic.StatusThread StatusThread} used
 * for network communication. This class uses UDP-network communication based on
 * the default java DatagrammSocket.
 * 
 * @author Robert Meschkat
 *
 */
public abstract class NetworkThread extends StatusThread {
	protected int port;
	protected String ip;
	protected DatagramSocket socket;
	protected byte[] buffer;
	protected int packetSize = DEFAULT_PACKAGE_SIZE;
	protected boolean staticPort = true;

	/**
	 * Set maximum packet size for a received packet.
	 * 
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * Creates NetworkThread object.
	 * 
	 * @param ip
	 *            IP used for sending.
	 * @param port
	 *            Port used for sending.
	 */
	public NetworkThread(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * Creates NetworkThread object with the
	 * {@link oculusbot.network.NetworkConstants#DEFAULT_IP default ip}.
	 * 
	 * @param port
	 *            Port used for sending.
	 */
	public NetworkThread(Integer port) {
		this(DEFAULT_IP, port);
	}

	/**
	 * Creates NetworkThread object with the
	 * {@link oculusbot.network.NetworkConstants#DEFAULT_IP default ip} and a
	 * static port.
	 * 
	 * @param port
	 *            Port used for sending.
	 * @param staticPort
	 *            If true the port will be static.
	 */
	public NetworkThread(int port, boolean staticPort) {
		this(port);
		this.staticPort = staticPort;
	}

	@Override
	protected void setup() {
		try {
			if (staticPort) {
				socket = new DatagramSocket(port);
			} else {
				socket = new DatagramSocket();
			}
			socket.setSoTimeout(DEFAULT_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void task() {
		try {
			doNetworkOperation();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void shutdown() {
	}

	/**
	 * A repeatedly called method (Uses {@link oculusbot.basic.StatusThread#task
	 * task}-method from StatusThread) in which the send- and receive-methods
	 * can be used.
	 * 
	 * @throws IOException
	 */
	protected abstract void doNetworkOperation() throws IOException;

	/**
	 * Tries to receive a packet on the created DatagramSocket-object.
	 * 
	 * @return Received DatagramPacket.
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	protected DatagramPacket receive() throws SocketTimeoutException, IOException {
		buffer = new byte[packetSize];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return packet;
	}

	/**
	 * Creates an answer from receiver-DatagramPacket and the data and sends it. 
	 * @param data 
	 * @param receiver
	 * @throws IOException
	 */
	protected void send(String data, DatagramPacket receiver) throws IOException {
		send(data.getBytes(), receiver.getAddress(), receiver.getPort());
	}

	/**
	 * Same functionality as {@link #send(byte[], InetAddress, int)} but
	 * converts IP-String to InetAddress first.
	 * 
	 * @param data
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	protected void send(byte[] data, String ip, int port) throws IOException {
		send(data, InetAddress.getByName(ip), port);
	}

	/**
	 * Sends data to a specific host on a specific port.
	 * 
	 * @param data
	 *            Data to send.
	 * @param receiver
	 *            Host who will receive the packet.
	 * @param port
	 *            Port used to send packet.
	 * @throws IOException
	 */
	protected void send(byte[] data, InetAddress receiver, int port) throws IOException {
		buffer = data;
		DatagramPacket out = new DatagramPacket(buffer, buffer.length, receiver, port);
		socket.send(out);
	}

	/**
	 * Converts string to byte-array, puts data in buffer and calls
	 * {@link #send() send}-method.
	 * 
	 * @param data
	 * @throws IOException
	 */
	protected void send(String data) throws IOException {
		buffer = data.getBytes();
		send();
	}

	/**
	 * Puts data in buffer and calls {@link #send() send}-method.
	 * 
	 * @param data
	 * @throws IOException
	 */
	protected void send(byte[] data) throws IOException {
		buffer = data;
		send();
	}

	/**
	 * Tries to send the current buffer to the saved IP or sends a broadcast if
	 * the default IP is used.
	 * 
	 * @throws IOException
	 */
	private void send() throws IOException {
		InetAddress address;
		if (ip.equals(DEFAULT_IP)) {
			address = InetAddress.getByName(BROADCAST_IP);
		} else {
			address = InetAddress.getByName(ip);
		}
		DatagramPacket out = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(out);
	}

}
