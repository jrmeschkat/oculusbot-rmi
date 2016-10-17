package oculusbot.network.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static oculusbot.network.NetworkConstants.*;
import oculusbot.network.NetworkThread;

public class CommunicationsThread extends NetworkThread {

	private Controller controller;

	public CommunicationsThread(int port, Controller controller) {
		super(port);
		this.controller = controller;
	}

	@Override
	protected void setup() {
		super.setup();
		try {
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		DatagramPacket packet = null;

		try {
			packet = receive();
		} catch (SocketTimeoutException e) {
			return;
		}

		String[] data = new String(packet.getData()).trim().split(" ");
		String msg = "";
		try {
			msg = data[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return;
		}


		if (msg.equals(OB_REQUEST_SERVER_IP)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			msg("Sent IP to " + packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		if (msg.equals(OB_REGISTER_CLIENT)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			msg("Registered client " + packet.getAddress().getHostAddress());
			controller.registerClient(packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		if (msg.equals(OB_DEREGISTER_CLIENT)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			msg("Deregistered client " + packet.getAddress().getHostAddress());
			controller.deregisterClient(packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		if (msg.equals(OB_SEND_KEY)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			try {
				msg("Received key " + packet.getAddress().getHostAddress() + ": " + data[1]);
				controller.keyReleased(Integer.parseInt(data[1]));
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}

			send(OB_ACK, packet);
		}
		
		if (msg.equals(OB_POSITION_DATA)) {
			controller.setPosition(convert(data)); 
		}

	}
	
	private double[] convert(String[] data) {
		if(data.length < 4){
			return null;
		}
		double[] result = new double[data.length-1];
		for (int i = 0; i < result.length; i++) {
			try {
				result[i] = Double.parseDouble(data[i+1]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return result;
	}

}
