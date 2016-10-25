package oculusbot.pi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import oculusbot.basic.StatusThread;
import oculusbot.bot.BotControlThread;
import oculusbot.rmi.PiOperations;
import oculusbot.rmi.PiOperationsImplementation;

/**
 * Class that handles communication between different objects of the
 * RMI-RaspberryPi-component.
 * 
 * @author Robert Meschkat
 *
 */
public class PiController extends StatusThread {
	private PiOperationsImplementation piOperations;
	private BotControlThread bot;
	private boolean running = true;
	private GpioController gpio;

	@Override
	protected void setup() {
		System.setProperty("java.rmi.server.hostname", "oculusbot");
		//create the GPIO instance that is used to interact with the motors
		gpio = GpioFactory.getInstance();
		//start the thread that controls the motors
		bot = new BotControlThread(gpio);
		bot.start();

		try {
			//create the RMI registry
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			//bind the implementation of the PiOperations-interface at the registry
			piOperations = new PiOperationsImplementation(this);
			Naming.rebind(PiOperations.REGISTRY_NAME, piOperations);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void task() {
		while (running) {
			pause(100);
		}
		pause(1000);
		interrupt();
	}

	@Override
	protected void shutdown() {
		bot.interrupt();
		waitForClosingThreads(bot);
		gpio.shutdown();
		System.exit(0);
	}

	/**
	 * Calling this method leads to the exit of this program.
	 */
	public void close() {
		running = false;
	}

	/**
	 * Sets new target angle in the {@link BotControlThread}.
	 * @param yaw
	 * @param pitch
	 * @param roll
	 */
	public void set(double yaw, double pitch, double roll) {
		bot.set(yaw, pitch, roll);
	}

}
