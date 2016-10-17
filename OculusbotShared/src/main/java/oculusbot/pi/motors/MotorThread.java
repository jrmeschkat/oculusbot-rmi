package oculusbot.pi.motors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import oculusbot.basic.StatusThread;
import oculusbot.pi.basics.PinInputController;

public class MotorThread extends StatusThread {
	private static final long DEFAULT_DELAY = 4;
	private static final double STEP_ANGLE_RATIO = 360.0 / 2048.0; // = 360 degrees / 2048 steps = 0,17578125

	private double minAngle = -180;
	private double maxAngle = 180;
	private double centerAngle = 0;
	private GpioPinDigitalOutput blue;
	private GpioPinDigitalOutput pink;
	private GpioPinDigitalOutput yellow;
	private GpioPinDigitalOutput orange;
	private int currentStep = 0;
	private double currentAngle = -1;
	private double targetAngle = 0;
	private long delay = DEFAULT_DELAY;
	private PinInputController input;
	private boolean centerForwards;

	private boolean[][] fullstep = { { true, true, false, false }, { false, true, true, false },
			{ false, false, true, true }, { true, false, false, true } };

	public void setDelay(long delay) {
		if (delay < 2) {
			delay = DEFAULT_DELAY;
		}
		this.delay = delay;
	}

	public void setDelay() {
		this.delay = DEFAULT_DELAY;
	}

	public double getCurrentAngle() {
		return currentAngle;
	}

	public double getCenterAngle() {
		return centerAngle;
	}

	public void setTargetAngle(double angle) {
		if (angle > maxAngle) {
			angle = maxAngle;
		}

		if (angle < minAngle) {
			angle = minAngle;
		}

		this.targetAngle = angle;
	}

	public MotorThread(Pin blue, Pin pink, Pin yellow, Pin orange, Pin in, GpioController gpio, double range,
			boolean centerForwards) {
		this.input = new PinInputController(in, gpio);
		this.blue = gpio.provisionDigitalOutputPin(blue);
		this.pink = gpio.provisionDigitalOutputPin(pink);
		this.yellow = gpio.provisionDigitalOutputPin(yellow);
		this.orange = gpio.provisionDigitalOutputPin(orange);
		this.centerForwards = centerForwards;
		this.maxAngle = range;
		this.minAngle = -range;
	}

	@Override
	protected void setup() {
		center();
	}

	@Override
	protected void task() {
		if (Math.abs(currentAngle - targetAngle) > STEP_ANGLE_RATIO) {
			if (currentAngle - targetAngle < 0) {
				forwards();
			} else {
				backwards();
			}
		}
	}

	public void center() {
		findStart();

		for (int i = 0; i < maxAngle / STEP_ANGLE_RATIO; i++) {
			move(!centerForwards);
		}
		currentAngle = 0;
		targetAngle = 0;
	}

	private void move(boolean direction) {
		if (direction) {
			forwards();
		} else {
			backwards();
		}
	}

	private void findStart() {
		if (centerForwards) {
			currentAngle = minAngle;
		} else {
			currentAngle = maxAngle;
		}

		while (input.isHigh()) {
			move(centerForwards);
		}

		if (centerForwards) {
			currentAngle = maxAngle;
		} else {
			currentAngle = minAngle;
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

	void setStep(boolean b, boolean p, boolean y, boolean o) {
		blue.setState(b);
		pink.setState(p);
		yellow.setState(y);
		orange.setState(o);
	}

	public void forwards() {
		if (currentAngle > maxAngle) {
			return;
		}
		boolean[] tmp = fullstep[currentStep];
		setStep(tmp[0], tmp[1], tmp[2], tmp[3]);
		pause(delay);
		currentStep = (currentStep + 1) % fullstep.length;
		currentAngle += STEP_ANGLE_RATIO;
	}

	public void backwards() {
		if (currentAngle < minAngle) {
			return;
		}
		boolean[] tmp = fullstep[currentStep];
		setStep(tmp[0], tmp[1], tmp[2], tmp[3]);
		pause(delay);
		currentStep--;
		if (currentStep < 0) {
			currentStep = fullstep.length - 1;
		}
		currentAngle -= STEP_ANGLE_RATIO;
	}

}
