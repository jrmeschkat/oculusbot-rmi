package oculusbot.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that defines which methods will be available for the RMI-object.
 * @author Robert Meschkat
 *
 */
public interface PiOperations extends Remote{
	String REGISTRY_NAME = "PiOperations";
	
	/**
	 * Tells the controller to update target angle of BotControlThread.
	 * @param yaw
	 * @param pitch
	 * @param roll
	 * @throws RemoteException
	 */
	void setMotorPositions(double yaw, double pitch, double roll) throws RemoteException;
	
	/**
	 * Interrupts the controller and leads to shutdown. 
	 * @throws RemoteException
	 */
	void interrupt() throws RemoteException;
}
