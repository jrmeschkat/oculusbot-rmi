package oculusbot.basic;

public abstract class StatusThread extends Thread {
	protected Status status = Status.DEAD;
	protected boolean ignoreStatus = false;

	public Status getStatus() {
		return status;
	}

	protected abstract void setup();

	protected abstract void task();

	protected abstract void shutdown();

	@Override
	public synchronized void start() {
		super.start();
	}

	@Override
	public void run() {
		setName(getClass().getName());
		status = Status.SETUP;
		setup();
		status = Status.READY;
		while (!interrupted()) {
			task();
		}
	}

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

	protected void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			interrupt();
		}
	}

	protected void msg(String msg) {
		System.out.println(getClass().getName() + " ("+status+"): " + msg);
	}

	@Override
	public void interrupt() {
		shutdown();
		super.interrupt();
	}
	
	protected void waitForClosingThreads(StatusThread... threads){
		boolean alive = true;
		while(alive){
			alive = false;
			for(StatusThread t : threads){
				alive = alive || t.isAlive();
			}
		}
	}

}
