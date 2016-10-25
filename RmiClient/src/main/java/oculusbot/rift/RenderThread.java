package oculusbot.rift;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.net.InetAddress;

import oculusbot.basic.ClientProperties;
import oculusbot.basic.PropertyLoader;
import oculusbot.basic.StatusThread;
import oculusbot.network.client.SendPositionDataThread;
import oculusbot.network.client.Communications;
import oculusbot.opengl.Callback;
import oculusbot.opengl.Window;
import oculusbot.video.ReceiveVideoThread;

/**
 * The control thread for the client.
 * @author Robert Meschkat
 *
 */
public class RenderThread extends StatusThread {

	private Rift rift;
	private Window window;
	private ReceiveVideoThread video;
	private SendPositionDataThread position;
	private int width;
	private int height;
	private PropertyLoader props;
	private Communications com;
	private boolean doDiscovery = true;
	private String ip;

	/**
	 * @return The rift object
	 */
	public Rift getRift() {
		return rift;
	}

	/**
	 * Creates the control thread. 
	 * @param width Width of the window
	 * @param height Height of the window
	 */
	public RenderThread(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates control thread with a static server IP.
	 * @param width Width of the window
	 * @param height Height of the window
	 * @param ip Server IP
	 */
	public RenderThread(int width, int height, String ip) {
		this.width = width;
		this.height = height;
		this.ip = ip;
		doDiscovery = false;
	}

	@Override
	protected void setup() {
		System.out.println(ip);
		//load property file
		props = new PropertyLoader(ClientProperties.PROPERTY_FILENAME, ClientProperties.DEFAULT_PROPERTY_FILENAME);
		//start outgoing communications
		com = new Communications(props.getPropertyAsInt(ClientProperties.PORT_DISCOVERY));
		//do server discovery if no server IP is defined
		if (doDiscovery) {
			InetAddress serverIP = com.getServerIP();
			ip = serverIP.getHostAddress();
		}
		//register client at server
		com.registerClient();
		
		//create mirror window and add callback
		window = new Window(width, height);
		window.setCallback(new Callback() {

			public void keyPressed(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW_RELEASE) {
					if (key == GLFW_KEY_R) {
						rift.recenter();
					} else {
						com.sendKey(key);
					}
				}
			}
		});
		window.init();
		
		//start receive video thread
		video = new ReceiveVideoThread(props.getPropertyAsInt(ClientProperties.PORT_VIDEO), ip);
		video.start();
		
		//check if latency should be printed to console
		boolean showLatency = false;
		if (props.getProperty(ClientProperties.SHOW_LATENCY).equals("true")) {
			showLatency = true;
		}
		
		//start rift thread and link it with mirror window
		rift = new Rift(video, showLatency);

		window.register(new MirrorWindow(rift.getMirrorFramebuffer(width, height), width, height));
		rift.init();

		//start the send postion thread
		position = new SendPositionDataThread(this, rift);
		position.start();

	}

	@Override
	protected void task() {
		//run until the mirror window should close
		if (window.shouldClose()) {
			interrupt();
			return;
		}

		//render image to rift and window
		rift.render();
		window.render();
	}

	@Override
	protected void shutdown() {
		position.interrupt();
		video.interrupt();
		waitForClosingThreads(position, video);
		while (!rift.destroy())
			;
		window.destroy();
		com.unregisterClient();
	}

	/**
	 * Used to communicate between SendPositionDataThread and Communication.
	 * @param yaw
	 * @param pitch
	 * @param roll
	 */
	public void sendPosition(double yaw, double pitch, double roll) {
		com.sendPosition(yaw, pitch, roll);
	}

}
