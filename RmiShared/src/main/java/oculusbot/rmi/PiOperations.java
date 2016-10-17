package oculusbot.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PiOperations extends Remote{
	String REGISTRY_NAME = "PiOperations";
	
	void setMotorPositions(double yaw, double pitch, double roll) throws RemoteException;
	void interrupt() throws RemoteException;
	String test(String text) throws RemoteException;
}
