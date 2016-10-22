package oculusbot.network.client;

import static org.lwjgl.ovr.OVR.*;
import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRQuatf;
import org.lwjgl.ovr.OVRTrackingState;

import oculusbot.basic.StatusThread;
import oculusbot.rift.RenderThread;
import oculusbot.rift.Rift;

public class SendPositionDataThread extends StatusThread {
	private static final double SEND_LIMIT = 2.5;
	private double oldYaw = 180;
	private double oldPitch = 180;
	private double oldRoll = 180;
	private Rift rift;
	private RenderThread render;

	private double yaw;
	private double pitch;
	private double roll;
	
	public SendPositionDataThread(RenderThread render, Rift rift) {
		this.rift = rift;
		this.render = render;
	}

//	@Override
//	protected void doNetworkOperation() throws IOException {
//		if (rift == null) {
//			return;
//		}
//		OVRTrackingState trackingState = OVRTrackingState.malloc();
//		ovr_GetTrackingState(rift.getSession(), 0, true, trackingState);
//		OVRPosef pose = trackingState.HeadPose().ThePose();
//		OVRQuatf orientation = pose.Orientation();
//
//		yaw = -toDeg(orientation.y());
//		pitch = -toDeg(orientation.x());
//		roll = toDeg(orientation.z());
//		trackingState.free();
//		if (checkLimit()) {
//			String data = yaw + " " + pitch + " " + roll;
//			send(data);
//			oldYaw = yaw;
//			oldPitch = pitch;
//			oldRoll = roll;
//		}
//	}
	
	@Override
	protected void setup() {
	}
	
	@Override
	protected void task() {
		if (rift == null) {
			return;
		}
		OVRTrackingState trackingState = OVRTrackingState.malloc();
		ovr_GetTrackingState(rift.getSession(), 0, true, trackingState);
		OVRPosef pose = trackingState.HeadPose().ThePose();
		OVRQuatf orientation = pose.Orientation();
		yaw = toDeg(orientation.y());
		pitch = -toDeg(orientation.x());
		roll = -toDeg(orientation.z());
		trackingState.free();
		if (checkLimit()) {
			render.sendPosition(yaw, pitch, roll);
			oldYaw = yaw;
			oldPitch = pitch;
			oldRoll = roll;
		}
	}
	
	@Override
	protected void shutdown() {
	}

	private boolean checkLimit() {
		if (Math.abs(oldYaw - yaw) > SEND_LIMIT) {
			return true;
		}
		if (Math.abs(oldPitch - pitch) > SEND_LIMIT) {
			return true;
		}
		if (Math.abs(oldRoll - roll) > SEND_LIMIT) {
			return true;
		}
		return false;
	}

	private double toDeg(double rad) {
		return Math.toDegrees(rad);
	}
	

}
