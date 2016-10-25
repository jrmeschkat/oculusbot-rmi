package oculusbot.basic;

/**
 * Contains names to access all properties in the client config file. 
 * @author Robert Meschkat
 *
 */
public interface ClientProperties {
	String PROPERTY_FILENAME = "./client.cfg";
	String DEFAULT_PROPERTY_FILENAME = "config/default_client.cfg";
	String PORT_DISCOVERY = "port.discovery";
	String PORT_VIDEO = "port.video";
	String SHOW_LATENCY = "latency";
}
