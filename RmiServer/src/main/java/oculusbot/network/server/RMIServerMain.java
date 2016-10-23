package oculusbot.network.server;

import java.io.IOException;
import java.util.Scanner;
import oculusbot.config.CameraConfig;

/**
 * Main class for the server. Starts a controller and waits for a specific
 * keyboard input to close program.
 * 
 * @author Robert Meschkat
 *
 */

import oculusbot.config.CameraConfig;

import oculusbot.config.CameraConfig;

public class RMIServerMain {

	public static void main(String[] args) throws IOException {
		if(args != null && args.length > 0 && args[0].equals("-c")){
			CameraConfig.lookupCameras();
		}
		
		Controller controller = new Controller();
		controller.start();

		// wait till user wants to close server
		Scanner in = new Scanner(System.in);
		String line = "";
		while (!line.toLowerCase().equals("y")) {
			System.out.print("Exit (y/n)? ");
			line = in.nextLine();
		}
		in.close();
		controller.interrupt();
	}

}
