package oculusbot.network.server;

import java.io.IOException;
import java.util.Scanner;

public class RMIServerMain {

	public static void main(String[] args) throws IOException {
		Controller controller = new Controller();
		controller.start();
		
		Scanner in = new Scanner(System.in);
		String line = "";
		while(!line.toLowerCase().equals("y")){
			System.out.print("Exit (y/n)? ");
			line = in.nextLine();
		}
		in.close();
		controller.interrupt();
	}

}
