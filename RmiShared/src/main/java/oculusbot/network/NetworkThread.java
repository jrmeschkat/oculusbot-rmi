package oculusbot.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import oculusbot.basic.StatusThread;

import static oculusbot.network.NetworkConstants.*;

public abstract class NetworkThread extends StatusThread {
	protected int port;
	protected String ip;
	protected DatagramSocket socket;
	protected byte[] buffer;
	protected int packetSize = DEFAULT_PACKAGE_SIZE;
	protected boolean staticPort = true;

	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	public NetworkThread(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}

	public NetworkThread(Integer port) {
		this(DEFAULT_IP, port);
	}
	
	public NetworkThread(int port, boolean staticPort) {
		this(port);
		this.staticPort = staticPort;
	}

	@Override
	protected void setup() {
		try {
			if(staticPort){
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
	
	protected abstract void doNetworkOperation() throws IOException;

	protected DatagramPacket receive() throws SocketTimeoutException, IOException {
		buffer = new byte[packetSize];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return packet;
	}

	protected void send(String data, DatagramPacket receiver) throws IOException {
		send(data.getBytes(), receiver.getAddress(), receiver.getPort());
	}
	
	protected void send(byte[] data, String ip, int port) throws IOException {
		send(data, InetAddress.getByName(ip), port);
	}
	
	protected void send(byte[] data, InetAddress receiver, int port) throws IOException {
		buffer = data;
		DatagramPacket out = new DatagramPacket(buffer, buffer.length, receiver, port);
		socket.send(out);
	}

	protected void send(String data) throws IOException {
		buffer = data.getBytes();
		send();
	}

	protected void send(byte[] data) throws IOException {
		buffer = data;
		send();
	}

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
