package oculusbot.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import oculusbot.pi.PiController;
import oculusbot.rmi.PiOperations;

/**
 * Class that implements the behavior for the
 * {@link PiOperations}-RMI-interface.
 * 
 * @author Robert Meschkat
 *
 */
public class PiOperationsImplementation extends UnicastRemoteObject implements PiOperations {
	private static final long serialVersionUID = 1L;
	private PiController controller;

	/**
	 * Creates a implementation of the {@link PiOperations}-interface.
	 * 
	 * @param controller
	 *            The {@link PiController}-object that will be controlled by
	 *            this implementation
	 * @throws RemoteException
	 */
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

}
