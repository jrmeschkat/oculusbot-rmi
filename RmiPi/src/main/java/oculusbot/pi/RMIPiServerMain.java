package oculusbot.pi;

import oculusbot.pi.PiController;

/**
 * Main class for the RaspberryPi-component of the RMI-version of the Oculusbot.
 * @author Robert Meschkat
 *
 */
public class RMIPiServerMain {

	public static void main(String[] args) {
		new PiController().start();
	}

}
