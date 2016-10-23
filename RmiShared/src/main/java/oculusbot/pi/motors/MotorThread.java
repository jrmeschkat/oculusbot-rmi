package oculusbot.pi.motors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import oculusbot.basic.StatusThread;
import oculusbot.pi.basics.PinInputController;

/**
 * Thread used to control stepper motors
 * <a href="https://arduino-info.wikispaces.com/SmallSteppers">28BYJ-48</a>.
 *
 * An <a href="http://www.ti.com/lit/ds/symlink/uln2003a.pdf">ULN2003</a> is
 * used as a stepper motor driver.
 * 
 * @author Robert Meschkat
 *
 */
public class MotorThread extends StatusThread {
	/**
	 * Delay between steps during movement. A greater value would lead to a
	 * slower movement.
	 */
	private static final long DEFAULT_DELAY = 4;
	/**
	 * Defines size of the angle which will be covered by one step.
	 */
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

	/**
	 * Set the delay between steps. A longer delay would mean a slower movement
	 * of the motor. Minimum is {@value #DEFAULT_DELAY}.
	 * 
	 * @param delay
	 */
	public void setDelay(long delay) {
		if (delay < 4) {
			delay = DEFAULT_DELAY;
		}
		this.delay = delay;
	}

	/**
	 * Resets the delay to {@value #DEFAULT_DELAY}.
	 */
	public void resetDelay() {
		this.delay = DEFAULT_DELAY;
	}

	/**
	 * Returns current positon.
	 * 
	 * @return
	 */
	public double getCurrentAngle() {
		return currentAngle;
	}

	/**
	 * Returns the center Angle.
	 * 
	 * @return
	 */
	public double getCenterAngle() {
		return centerAngle;
	}

	/**
	 * Sets the angle to which the motor should move. If the value exceeds the
	 * minimum or the maximum of the motor then the target will be set to the
	 * respective border.
	 * 
	 * @param angle
	 */
	public void setTargetAngle(double angle) {
		if (angle > maxAngle) {
			angle = maxAngle;
		}

		if (angle < minAngle) {
			angle = minAngle;
		}

		this.targetAngle = angle;
	}

	/**
	 * Creates an object to control a stepper motor with the RaspberryPi.
	 * 
	 * @param blue
	 *            Output pin for blue motor control line.
	 * @param pink
	 *            Output pin for pink motor control line.
	 * @param yellow
	 *            Output pin for yellow motor control line.
	 * @param orange
	 *            Output pin for orange motor control line.
	 * @param in
	 *            Input pin for limit switch.
	 * @param gpio
	 *            GPIO controller (only one per application)
	 * @param range
	 *            Maximum/Minimum for this motor.
	 * @param centerForwards
	 *            Defines if motor moves forwards or backwards during centering
	 *            process.
	 */
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
		center(); //center at startup to ensure that all angles are correct
	}

	@Override
	protected void task() {
		//move only if target angle is not reached already
		if (Math.abs(currentAngle - targetAngle) > STEP_ANGLE_RATIO) {
			if (currentAngle - targetAngle < 0) {
				forwards();
			} else {
				backwards();
			}
		}
	}

	/**
	 * Moves to minimum first with {@link #findStart() findStart}-method and
	 * then moves backwards until center postion is reached.
	 */
	public void center() {
		//move to minimum
		findStart();

		//move back to center
		for (int i = 0; i < maxAngle / STEP_ANGLE_RATIO; i++) {
			move(!centerForwards);
		}
		currentAngle = 0;
		targetAngle = 0;
	}

	/**
	 * Moves motor with {@link #forwards() forwards}- and {@link #backwards() backwards}-methods.
	 * 
	 * @param direction
	 *            If true the motor moves forwards. Otherwise it moves
	 *            backwards.
	 */
	private void move(boolean direction) {
		if (direction) {
			forwards();
		} else {
			backwards();
		}
	}

	/**
	 * Used to find the minimum or maximum position of the motor. To find this
	 * border the moter moves till the limit switch is triggered.
	 */
	private void findStart() {
		if (centerForwards) {
			currentAngle = minAngle;
		} else {
			currentAngle = maxAngle;
		}

		//move to maximum/minimum
		while (input.isHigh()) {
			move(centerForwards);
		}

		//reset angle to correct value
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

	/**
	 * Sets the GPIO pins to the states defined by the parameters. 
	 * @param b
	 * @param p
	 * @param y
	 * @param o
	 */
	void setStep(boolean b, boolean p, boolean y, boolean o) {
		blue.setState(b);
		pink.setState(p);
		yellow.setState(y);
		orange.setState(o);
	}

	/**
	 * Moves the motor one step forwards. 
	 */
	public void forwards() {
		//check if still inside border.
		if (currentAngle > maxAngle) {
			return;
		}
		//get and set the next step
		boolean[] tmp = fullstep[currentStep];
		setStep(tmp[0], tmp[1], tmp[2], tmp[3]);
		pause(delay);
		//update the current angle and step
		currentStep = (currentStep + 1) % fullstep.length;
		currentAngle += STEP_ANGLE_RATIO;
	}

	/**
	 * Moves the motor one step backwards
	 * @see #forwards()
	 */
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
