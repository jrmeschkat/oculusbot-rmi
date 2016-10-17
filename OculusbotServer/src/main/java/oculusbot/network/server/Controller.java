package oculusbot.network.server;

import org.lwjgl.glfw.GLFW;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import oculusbot.basic.PropertyLoader;
import static oculusbot.basic.ServerProperties.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import oculusbot.basic.StatusThread;
//import oculusbot.bot.BotControlThread;
//import oculusbot.bot.StatusLED;
import oculusbot.pi.basics.Pins;
import oculusbot.rmi.PiOperations;
import oculusbot.video.SendVideoThread;

public class Controller extends StatusThread {
	private CommunicationsThread com;
	private SendVideoThread video;
	//	private StatusLED led;
	private PropertyLoader props;
	private SSHExecuter exec;
	private String hostname;
	private PiOperations ops = null;

	@Override
	protected void setup() {
		ignoreStatus = true;
		props = new PropertyLoader(PROPERTY_FILENAME, DEFAULT_PROPERTY_FILENAME);
		hostname = props.getProperty(PI_HOSTNAME);
		exec = new SSHExecuter(props.getProperty(PI_USER), props.getProperty(PI_PASSWORD), hostname);
		msg(exec.sendCommand("sudo OculusbotServerRMI/run.sh"));

		ops = bind(hostname, PiOperations.REGISTRY_NAME);
		try {
			System.out.println(ops.test("HELLO WORLD!"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		com = new CommunicationsThread(props.getPropertyAsInt(PORT_DISCOVERY), this);
		int camWidth = props.getPropertyAsInt(CAM_WIDTH);
		int camHeight = props.getPropertyAsInt(CAM_HEIGHT);
		msg("Cam resolution: " + camWidth + " x " + camHeight);
		video = new SendVideoThread(props.getPropertyAsInt(PORT_VIDEO), camWidth, camHeight);
		com.start();
		video.start();

		//		led = new StatusLED(Pins.GPIO_05, gpio);
	}

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
		//		led.setStatus(passthroughStatus(com, bot, video));
		pause(100);
	}

	@Override
	protected void shutdown() {
		com.interrupt();
		video.interrupt();
		//		led.shutdown();

		waitForClosingThreads(com, video);
		try {
			ops.interrupt();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		exec.disconnect();
	}

	public void registerClient(String ip) {
		video.registerClient(ip);
	}

	public void deregisterClient(String ip) {
		video.deregisterClient(ip);
	}

	public void keyReleased(int key) {
		if (key == GLFW.GLFW_KEY_S) {
			video.switchCameras();
		}
	}

	public void setPosition(double[] data) {
		try {
			if (data != null && data.length > 2)
				ops.setMotorPositions(data[0], data[1], data[2]);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
}
