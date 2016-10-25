package oculusbot.network.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import oculusbot.basic.StatusThread;

/**
 * Class that uses the systems ping-command to determine the networks latency.
 * 
 * @author Robert Meschkat
 *
 */
public class PingThread extends StatusThread {
	/**
	 * Default packet size used for the ping.
	 */
	public static final int DEFAULT_PACKET_SIZE = 2048;

	/**
	 * Number of packets used to determine the average value.
	 */
	public static final int PACKET_COUNT = 5;
	private String cmd;
	private String host;
	private int packetSize;
	private boolean windows = false;
	private double ping = 0;

	/**
	 * Get the last measured ping.
	 * 
	 * @return
	 */
	public double getPing() {
		return ping;
	}

	/**
	 * Set the packet size used for the ping.
	 * 
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * Creates the thread and determines the OS of the machine because Windows-
	 * and Unix-machines use different names for the option names.
	 * 
	 * @param host
	 *            Host which will be pinged.
	 * @param packetSize
	 */
	public PingThread(String host, int packetSize) {
		super();
		this.host = host;
		this.packetSize = packetSize;
		if (System.getProperty("os.name").contains("Windows")) {
			windows = true;
		}
	}

	/**
	 * @see PingThread#PingThread(String, int)
	 * @param host
	 */
	public PingThread(String host) {
		this(host, DEFAULT_PACKET_SIZE);
	}

	@Override
	protected void setup() {
		//create the command depending on the OS
		cmd = "ping ";
		if (windows) {
			cmd += "-n " + PACKET_COUNT + " -l " + packetSize;
		} else {
			cmd += "-c " + PACKET_COUNT + " -s " + packetSize;
		}
		cmd += " " + host;
	}

	@Override
	protected void task() {
		try {
			//run the command
			Process pro = Runtime.getRuntime().exec(cmd);
			pro.waitFor();

			//read the output of the command
			BufferedReader r = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			ArrayList<String> output = new ArrayList<>();
			String line = "";
			while ((line = r.readLine()) != null) {
				output.add(line);
			}

			//parse the results
			ping = parseAverageTime(output.toArray(new String[output.size()]));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void shutdown() {

	}

	/**
	 * Parses the ping-commands console output and calculates the average.
	 * @param output output of the ping command
	 * @return average ping
	 */
	private double parseAverageTime(String[] output) {
		double average = 0;

		for (String line : output) {
			line = line.toLowerCase();
			//stop if statistics part of the output is reached
			if (line.contains("statisti")) {
				break;
			}

			//find the latency information in this line and save it
			String[] data = line.split(" ");
			for (String s : data) {
				if (s.startsWith("zeit") || s.startsWith("time")) {
					average += Double.valueOf(s.replaceAll("[^0-9.]+", ""));
				}
			}

		}

		return average / PACKET_COUNT;
	}

}
