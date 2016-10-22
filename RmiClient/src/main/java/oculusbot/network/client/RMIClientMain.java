package oculusbot.network.client;

import oculusbot.rift.RenderThread;

public class RMIClientMain {
	
	private RenderThread render;

	public RMIClientMain() {
		render = new RenderThread(1600, 600);
		render.start();
	}
	
	public RMIClientMain(String ip) {
		render = new RenderThread(1600, 600, ip);
		render.start();
	}


	public static void main(String[] args) {
		if(args.length > 1){
			if(args[0].equals("-d")){
				new RMIClientMain(args[1]);
			}
			
		} else{
			new RMIClientMain();
		}
	}

}
