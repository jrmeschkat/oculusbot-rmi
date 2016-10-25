package oculusbot.config;

import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import static oculusbot.basic.ServerProperties.*;
import oculusbot.basic.PropertyLoader;

/**
 * This class is used to configure the cameras. It iterates over the device IDs
 * from 0 to a specific limit and asks the user if he wants to use each found
 * camera.
 * 
 * @author Robert Meschkat
 *
 */
public class CameraConfig {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static final int LIMIT = 10;

	public static void lookupCameras() {
		//load the properties so that the results can be saved
		PropertyLoader props = new PropertyLoader(PROPERTY_FILENAME, DEFAULT_PROPERTY_FILENAME);
		Scanner in = new Scanner(System.in);
		System.out.println("Checking camera IDs 0-" + LIMIT + ":");

		//check each camera ID
		for (int i = 0; i < LIMIT; i++) {
			//try to open camera
			VideoCapture v = new VideoCapture(i);
			//if camera is open ask the user if he wants to use it as left or right cam
			if (v.isOpened()) {
				System.out.println("Detected camera with ID " + i);
				System.out.print("Is this the left (l), right (r) or another camera (x)?\t ");
				String line = in.nextLine();
				System.out.println();
				//save the results
				if (line.equals("l")) {
					props.updateValue(CAM_ID_LEFT, "" + i);
				}
				if (line.equals("r")) {
					props.updateValue(CAM_ID_RIGHT, "" + i);
				}
				v.release();
			}
		}

		in.close();
		System.out.println("Checked all cameras.");
	}

}
