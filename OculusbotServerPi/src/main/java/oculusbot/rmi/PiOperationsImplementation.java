package oculusbot.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import oculusbot.pi.PiController;
import oculusbot.rmi.PiOperations;

public class PiOperationsImplementation extends UnicastRemoteObject implements PiOperations {
	private static final long serialVersionUID = 1L;
	private PiController controller;

	public PiOperationsImplementation(PiController controller) throws RemoteException {
		super();
		this.controller = controller;
	}

	@Override
	public void setMotorPositions(double yaw, double pitch, double roll) throws RemoteException {
		controller.set(yaw, pitch, roll);
	}

	@Override
	public void interrupt() throws RemoteException {
		controller.close();
	}

	@Override
	public String test(String text) throws RemoteException {
		return getClass().getName() + ": " + text;
	}

}
