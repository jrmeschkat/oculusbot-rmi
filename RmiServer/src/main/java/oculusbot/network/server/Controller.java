package oculusbot.network.server;

import org.lwjgl.glfw.GLFW;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static oculusbot.basic.ServerProperties.*;
import oculusbot.basic.PropertyLoader;
import oculusbot.basic.StatusThread;
import oculusbot.rmi.PiOperations;
import oculusbot.video.SendVideoThread;

/**
 * Class that handles communication between the different parts of the program.
 * 
 * @author Robert Meschkat
 *
 */
public class Controller extends StatusThread {
	private CommunicationsThread com;
	private SendVideoThread video;
	private PropertyLoader props;
	private SSHExecuter exec;
	private String hostname;
	private PiOperations ops = null;

	@Override
	protected void setup() {
		ignoreStatus = true;
		//load the property file
		props = new PropertyLoader(PROPERTY_FILENAME, DEFAULT_PROPERTY_FILENAME);

		//use SSH to run a script on the RaspberryPi which starts the component that controls the motors 
		hostname = props.getProperty(PI_HOSTNAME);
		exec = new SSHExecuter(props.getProperty(PI_USER), props.getProperty(PI_PASSWORD), hostname);
		msg(exec.sendCommand("sudo RMIPiServer/run.sh"));

		//bind the method handler to the objects in the RMI-registry that was create by the Pi-component
		ops = bind(hostname, PiOperations.REGISTRY_NAME);
		//create the communications thread
		com = new CommunicationsThread(props.getPropertyAsInt(PORT_DISCOVERY), this);
		//load some properties for the video thread
		int camWidth = props.getPropertyAsInt(CAM_WIDTH);
		int camHeight = props.getPropertyAsInt(CAM_HEIGHT);
		int camIdLeft = props.getPropertyAsInt(CAM_ID_LEFT);
		int camIdRight = props.getPropertyAsInt(CAM_ID_RIGHT);
		msg("Cam resolution: " + camWidth + " x " + camHeight);
		//start the video and communications thread
		video = new SendVideoThread(props.getPropertyAsInt(PORT_VIDEO), camWidth, camHeight, camIdLeft, camIdRight);
		com.start();
		video.start();

	}

	/**
	 * Tries to bind the RMI-handle to the registry. Retries if no connection
	 * was established.
	 * 
	 * @param hostname
	 *            Name of the host of the RMI-registry.
	 * @param objectname
	 *            Name of the object that should be bound to the handle.
	 * @return
	 */
	private PiOperations bind(String hostname, String objectname) {
		PiOperations ops;
		while (true) {
			try {
				ops = (PiOperations) Naming.lookup("rmi://" + hostname + "/" + objectname);
				msg("Connected to RMI-registry on " + hostname);
				break;
			} catch (MalformedURLException e) {
				System.err.println("RMI-URL not correct.");
				e.printStackTrace();
			} catch (RemoteException | NotBoundException e) {
				msg("Searching for RMI-registry.");
				continue;
			}
		}

		return ops;
	}

	@Override
	protected void task() {
		pause(100);
	}

	@Override
	protected void shutdown() {
		com.interrupt();
		video.interrupt();

		waitForClosingThreads(com, video);
		try {
			ops.interrupt();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		exec.disconnect();
	}

	/**
	 * Tells the video thread to add a client to the receiver list.
	 * 
	 * @param ip
	 *            IP of the client
	 */
	public void registerClient(String ip) {
		video.registerClient(ip);
	}

	/**
	 * Tells the video thread to remove a client from the receiver list.
	 * 
	 * @param ip
	 *            IP of the client
	 */
	public void unregisterClient(String ip) {
		video.unregisterClient(ip);
	}

	/**
	 * Handles the received keyboard input of the client.
	 * 
	 * @param key
	 *            Key that determines the operation
	 */
	public void keyReleased(int key) {
		if (key == GLFW.GLFW_KEY_S) {
			video.switchCameras();
		}
	}

	/**
	 * Tells the RaspberryPi-component to update the target motor position.
	 * 
	 * @param data
	 *            New position data.
	 */
	public void setPosition(double[] data) {
		try {
			if (data != null && data.length > 2)
				ops.setMotorPositions(data[0], data[1], data[2]);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
}
