package oculusbot.basic;

/**
 * Contains names to access all properties in the server config file. 
 * @author Robert Meschkat
 *
 */
public interface ServerProperties {
	String PROPERTY_FILENAME = "./server.cfg";
	String DEFAULT_PROPERTY_FILENAME = "config/default_server.cfg";
	String PORT_DISCOVERY = "port.discovery";
	String PORT_BOT = "port.bot";
	String PORT_VIDEO = "port.video";
	String PI_HOSTNAME = "pi.hostname";
	String PI_USER = "pi.user";
	String PI_PASSWORD = "pi.password";
	String CAM_WIDTH = "cam.width";
	String CAM_HEIGHT = "cam.height";
	String CAM_ID_LEFT = "cam.id.left";
	String CAM_ID_RIGHT = "cam.id.right";
}
