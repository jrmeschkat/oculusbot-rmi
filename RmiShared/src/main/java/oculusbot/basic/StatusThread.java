package oculusbot.basic;

/**
 * An abstract blueprint for a thread with an initialization, a constantly
 * updated method and a shutdown.
 * 
 * @author Robert Meschkat
 *
 */
public abstract class StatusThread extends Thread {
	protected Status status = Status.DEAD;
	protected boolean ignoreStatus = false;

	public Status getStatus() {
		return status;
	}

	/**
	 * Abstract method which will be called once during startup.
	 */
	protected abstract void setup();

	/**
	 * Abstract method which will be called repeatedly until thread is
	 * interrupted.
	 */
	protected abstract void task();

	/**
	 * Abstract method which will be called if thread is interrupted.
	 */
	protected abstract void shutdown();

	@Override
	public void run() {
		//run setup
		setName(getClass().getName());
		status = Status.SETUP;
		setup();

		//start update loop
		status = Status.READY;
		while (!interrupted()) {
			task();
		}
	}

	/**
	 * Combines status of parameter-threads and this thread. Combining means
	 * that the status with the highest priority will be returned.
	 * 
	 * @param threads
	 *            Other StatusThread-objects.
	 * @return Combined status.
	 */
	protected Status passthroughStatus(StatusThread... threads) {
		Status result;
		if (ignoreStatus) {
			result = Status.DEAD;
		} else {
			result = this.status;
		}

		for (StatusThread s : threads) {
			if (s != null) {
				if (s.getStatus().ordinal() > result.ordinal()) {
					result = s.getStatus();
				}
			}
		}

		return result;
	}

	/**
	 * Calls Thread.sleep-method.
	 * 
	 * @param millis
	 *            Time to sleep in ms.
	 */
	protected void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			interrupt();
		}
	}

	/**
	 * Prints a message with an identifier to stdout.
	 * 
	 * @param msg
	 */
	protected void msg(String msg) {
		System.out.println(getClass().getName() + " (" + status + "): " + msg);
	}

	@Override
	public void interrupt() {
		super.interrupt();
		shutdown();
	}

	/**
	 * Wait until all parameter-threads aren't alive anymore. Normally used
	 * during shutdown.
	 * 
	 * @param threads
	 *            Other StatusThread-objects.
	 */
	protected void waitForClosingThreads(StatusThread... threads) {
		boolean alive = true;
		while (alive) {
			alive = false;
			for (StatusThread t : threads) {
				alive = alive || t.isAlive();
			}
		}
	}

}
