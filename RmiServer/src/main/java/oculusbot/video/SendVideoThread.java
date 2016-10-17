package oculusbot.video;

import java.io.IOException;
import java.util.LinkedList;

import oculusbot.basic.Status;
import oculusbot.network.NetworkThread;

public class SendVideoThread extends NetworkThread {
	private FrameGrabberThread frameGrabber;
	private LinkedList<String> clients;
	private int camWidth = 0;
	private int camHeight = 0;

	public SendVideoThread(int port) {
		super(port, false);
		clients = new LinkedList<>();
	}

	public SendVideoThread(int port, int camWidth, int camHeight) {
		this(port);
		this.camWidth = camWidth;
		this.camHeight = camHeight;
	}

	@Override
	public Status getStatus() {
		return passthroughStatus(frameGrabber);
	}

	@Override
	protected void setup() {
		super.setup();
		if(camHeight > 0 && camWidth > 0){
			frameGrabber = new FrameGrabberThread(camWidth, camHeight);
		} else {
			frameGrabber = new FrameGrabberThread();
		}
		frameGrabber.start();
	}

	public void switchCameras() {
		frameGrabber.switchCameras();
	}

	@Override
	protected void doNetworkOperation() throws IOException {
		if(clients.isEmpty()){
			pause(100);
			return;
		}
		byte[] data = frameGrabber.grabFrameAsByte();
		if (data != null) {
			for(String ip : clients){
				send(data, ip, port);
			}
		}
	}

	@Override
	protected void shutdown() {
		frameGrabber.interrupt();
		waitForClosingThreads(frameGrabber);
	}

	public void registerClient(String ip) {
		for(String client : clients){
			if(client.equals(ip)){
				return;
			}
		}
		clients.add(ip);
	}

	public void deregisterClient(String ip) {
		for(int i = 0; i < clients.size(); i++){
			if (clients.get(i).equals(ip)) {
				clients.remove(i);
				return;
			}
		}
	}
	

}
