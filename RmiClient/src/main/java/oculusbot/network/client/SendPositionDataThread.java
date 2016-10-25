package oculusbot.network.client;

import org.lwjgl.ovr.OVRPosef;
import org.lwjgl.ovr.OVRQuatf;
import org.lwjgl.ovr.OVRTrackingState;

import static org.lwjgl.ovr.OVR.*;

import oculusbot.basic.StatusThread;
import oculusbot.rift.RenderThread;
import oculusbot.rift.Rift;

/**
 * Thread which gets the orientation of the rift and sends it to the server.
 * 
 * @author Robert Meschkat
 *
 */
public class SendPositionDataThread extends StatusThread {
	/**
	 * The minimum which is used to check if the new data should be send to the
	 * server.
	 */
	private static final double SEND_LIMIT = 2.5;
	private double oldYaw = 180;
	private double oldPitch = 180;
	private double oldRoll = 180;
	private Rift rift;
	private RenderThread render;

	private double yaw;
	private double pitch;
	private double roll;

	/**
	 * Create a SendPositionDataThread.
	 * 
	 * @param render
	 *            Controller class which will transfer the output to the network
	 *            class.
	 * @param rift
	 *            Initialized {@link oculusbot.rift.Rift Rift}-object.
	 */
	public SendPositionDataThread(RenderThread render, Rift rift) {
		this.rift = rift;
		this.render = render;
	}

	@Override
	protected void setup() {
	}

	@Override
	protected void task() {
		if (rift == null) {
			return;
		}
		//read the tracking state of the rift which contains the orientation
		OVRTrackingState trackingState = OVRTrackingState.malloc();
		ovr_GetTrackingState(rift.getSession(), 0, true, trackingState);

		//get the orientation from the tracking state
		OVRPosef pose = trackingState.HeadPose().ThePose();
		OVRQuatf orientation = pose.Orientation();
		yaw = toDeg(orientation.y());
		pitch = -toDeg(orientation.x());
		roll = -toDeg(orientation.z());
		trackingState.free();

		//check if the difference from the last reading is big enough 
		//so that the new data should be send to the server 
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

	/**
	 * Returns true if one of the axis of the orientation differs enough from
	 * the last reading.
	 * 
	 * @return
	 */
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
