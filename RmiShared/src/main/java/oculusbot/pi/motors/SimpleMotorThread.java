package oculusbot.pi.motors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import oculusbot.basic.StatusThread;

public class SimpleMotorThread extends StatusThread {
	private static final long DELAY = 2;
	private static final int STEPS = 500;
	private static final int TYPE = 2;

	private GpioPinDigitalOutput blue;
	private GpioPinDigitalOutput pink;
	private GpioPinDigitalOutput yellow;
	private GpioPinDigitalOutput orange;
	private boolean running = true;
	private boolean forward = true;

	private int[][] waveForward = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
	private int[][] waveBackwards = { { 0, 0, 0, 1 }, { 0, 0, 1, 0 }, { 0, 1, 0, 0 }, { 1, 0, 0, 0 } };
	private int[][] fullstepForward = { { 1, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 1, 1 }, { 1, 0, 0, 1 } };
	private int[][] fullstepBackwards = { { 1, 0, 0, 1 }, { 0, 0, 1, 1 }, { 0, 1, 1, 0 }, { 1, 1, 0, 0 } };
	private int[][] halfstepForward = { { 1, 0, 0, 0 }, { 1, 1, 0, 0 }, { 0, 1, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 1, 0 },
			{ 0, 0, 1, 1 }, { 0, 0, 0, 1 }, { 1, 0, 0, 1 } };
	private int[][] halfstepBackwards = { { 1, 0, 0, 1 }, { 0, 0, 0, 1 }, { 0, 0, 1, 1 }, { 0, 0, 1, 0 },
			{ 0, 1, 1, 0 }, { 0, 1, 0, 0 }, { 1, 1, 0, 0 }, { 1, 0, 0, 0 } };

	public SimpleMotorThread(Pin blue, Pin pink, Pin yellow, Pin orange, GpioController gpio) {
		this.blue = gpio.provisionDigitalOutputPin(blue);
		this.pink = gpio.provisionDigitalOutputPin(pink);
		this.yellow = gpio.provisionDigitalOutputPin(yellow);
		this.orange = gpio.provisionDigitalOutputPin(orange);
	}


	public void toggle() {
		forward = !forward;
	}
	
	void setStep(boolean b, boolean p, boolean y, boolean o) {
		blue.setState(b);
		pink.setState(p);
		yellow.setState(y);
		orange.setState(o);
	}

	void forward(int steps) {
		for (int i = 0; i < steps; i++) {
			switch (TYPE) {
			case 1:
				runMatrix(waveForward);
				break;
			case 2:
				runMatrix(fullstepForward);
				break;
			case 3:
				runMatrix(halfstepForward);
				break;
			default:
				runMatrix(halfstepForward);
				break;
			}
		}
	}

	void backwards(int steps) {
		for (int i = 0; i < steps; i++) {
			switch (TYPE) {
			case 1:
				runMatrix(waveBackwards);
				break;
			case 2:
				runMatrix(fullstepBackwards);
				break;
			case 3:
				runMatrix(halfstepBackwards);
				break;
			default:
				runMatrix(halfstepBackwards);
				break;
			}
		}
	}

	void runMatrix(int[][] mat) {
		boolean[][] bool = new boolean[mat.length][];
		for (int i = 0; i < mat.length; i++) {
			bool[i] = new boolean[mat[i].length];
			for (int j = 0; j < mat[i].length; j++) {
				if (mat[i][j] != 0) {
					bool[i][j] = true;
				}
			}
		}

		for (int i = 0; i < bool.length; i++) {
			setStep(bool[i][0], bool[i][1], bool[i][2], bool[i][3]);
			try {
				sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void task() {
		if (forward) {
			forward(1);
		} else{
			backwards(1);
		}
	}


	@Override
	protected void shutdown() {
		System.out.print("Shutdown pins...");
		blue.setShutdownOptions(true, PinState.LOW);
		pink.setShutdownOptions(true, PinState.LOW);
		yellow.setShutdownOptions(true, PinState.LOW);
		orange.setShutdownOptions(true, PinState.LOW);
		System.out.println("DONE");
	}

}
