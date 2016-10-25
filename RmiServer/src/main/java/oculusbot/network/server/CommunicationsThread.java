package oculusbot.network.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static oculusbot.network.NetworkConstants.*;
import oculusbot.network.NetworkThread;

/**
 * The receiver of all network communications. Interprets the received message,
 * start does the appropriate operation and sends an acknowledge to the client.
 * 
 * @author Robert Meschkat
 *
 */
public class CommunicationsThread extends NetworkThread {

	private Controller controller;

	/**
	 * Creates the communications thread.
	 * @param port Port used.
	 * @param controller Controller which connects every part of the application
	 */
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

		//wait for packet
		try {
			packet = receive();
		} catch (SocketTimeoutException e) {
			return;
		}

		//get the message which chooses the operation
		String[] data = new String(packet.getData()).trim().split(" ");
		String msg = "";
		try {
			msg = data[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return;
		}

		//client wants the server IP
		if (msg.equals(OB_REQUEST_SERVER_IP)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			//send IP
			msg("Sent IP to " + packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		//client wants to register itself
		if (msg.equals(OB_REGISTER_CLIENT)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			msg("Registered client " + packet.getAddress().getHostAddress());
			//save clients IP
			controller.registerClient(packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		//clients wants to unregister itself
		if (msg.equals(OB_UNREGISTER_CLIENT)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			msg("Deregistered client " + packet.getAddress().getHostAddress());
			//remove clients IP
			controller.unregisterClient(packet.getAddress().getHostAddress());
			send(OB_ACK, packet);
		}

		//client send keyboard input
		if (msg.equals(OB_SEND_KEY)) {
			msg("Packet received from " + packet.getAddress().getHostAddress() + ": " + msg);
			try {
				msg("Received key " + packet.getAddress().getHostAddress() + ": " + data[1]);
				//let the controller handle the input
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

		//client send new position for the moters
		if (msg.equals(OB_POSITION_DATA)) {
			//tell the controller to change the position
			controller.setPosition(convert(data));
		}

	}

	/**
	 * Converts the received data to double values for the motor control.
	 * @param data
	 * @return
	 */
	private double[] convert(String[] data) {
		if (data.length < 4) {
			return null;
		}
		double[] result = new double[data.length - 1];
		for (int i = 0; i < result.length; i++) {
			try {
				result[i] = Double.parseDouble(data[i + 1]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return result;
	}

}
