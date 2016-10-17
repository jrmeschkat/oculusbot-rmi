package oculusbot.network.client;

import oculusbot.rift.RenderThread;

public class OculusbotClientMain {
	
	private RenderThread render;

	public OculusbotClientMain() {
		render = new RenderThread(1600, 600);
		render.start();
	}
	
	public OculusbotClientMain(String ip) {
		render = new RenderThread(1600, 600, ip);
		render.start();
	}


	public static void main(String[] args) {
		if(args.length > 1){
			if(args[0].equals("-d")){
				new OculusbotClientMain(args[1]);
			}
			
		} else{
			new OculusbotClientMain();
		}
	}

}
