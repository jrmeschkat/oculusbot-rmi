package oculusbot.video;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import oculusbot.network.NetworkThread;

public class ReceiveVideoThread extends NetworkThread {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	private Mat frame;

	public Mat getFrame() {
		return frame;
	}

	public ReceiveVideoThread(int port) {
		super(port);
	}
	
	@Override
	protected void setup() {
		super.setup();
		frame = new Mat();
		setPacketSize(32000);
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		DatagramPacket packet;
		try {
			packet = receive();
		} catch (SocketTimeoutException e) {
			return;
		}
		
		frame = Imgcodecs.imdecode(new MatOfByte(packet.getData()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

	}


}
