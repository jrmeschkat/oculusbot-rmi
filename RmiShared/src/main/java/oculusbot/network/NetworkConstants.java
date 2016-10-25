package oculusbot.network;

/**
 * Contains default informations for network communications and all network
 * messages. The messages are used to identify the sent data.
 * 
 * @author Robert Meschkat
 *
 */
public interface NetworkConstants {
	/*--------------------------
	 * NETWORK MESSAGES
	 *--------------------------*/
	String OB_ACK = "OB_ACK";
	String OB_REQUEST_SERVER_IP = "OB_REQUEST_SERVER_IP";
	String OB_REGISTER_CLIENT = "OB_REGISTER_CLIENT";
	String OB_UNREGISTER_CLIENT = "OB_UNREGISTER_CLIENT";
	String OB_POSITION_DATA = "OB_POSITION_DATA";
	String OB_SEND_KEY = "OB_SEND_KEY";
	/*--------------------------
	 * NETWORK DEFAULT VALUES 
	 *--------------------------*/
	int DEFAULT_PACKAGE_SIZE = 2048;
	String DEFAULT_IP = "0.0.0.0";
	String BROADCAST_IP = "255.255.255.255";
	int DEFAULT_TIMEOUT = 5000;

}
