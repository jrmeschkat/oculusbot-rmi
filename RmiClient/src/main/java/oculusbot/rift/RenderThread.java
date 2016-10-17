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

	public Rift getRift() {
		return rift;
	}

	public RenderThread(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public RenderThread(int width, int height, String ip) {
		this.width = width;
		this.height = height;
		this.ip = ip;
		doDiscovery = false;
	}

	@Override
	protected void setup() {
		System.out.println(ip);
		props = new PropertyLoader(ClientProperties.PROPERTY_FILENAME, ClientProperties.DEFAULT_PROPERTY_FILENAME);
		com = new Communications(props.getPropertyAsInt(ClientProperties.PORT_DISCOVERY));
		if(doDiscovery){
			InetAddress serverIP = com.getServerIP();
			ip = serverIP.getHostAddress();
		}
		com.registerClient();
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
		video = new ReceiveVideoThread(props.getPropertyAsInt(ClientProperties.PORT_VIDEO));
		video.start();
		rift = new Rift(video);

		window.register(new MirrorWindow(rift.getMirrorFramebuffer(width, height), width, height));
		rift.init();

		position = new SendPositionDataThread(this, rift);
		position.start();

	}

	@Override
	protected void task() {
		if (window.shouldClose()) {
			interrupt();
			return;
		}

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
		com.deregisterClient();
	}

	public void sendPosition(double yaw, double pitch, double roll) {
		com.sendPosition(yaw, pitch, roll);
	}

}
