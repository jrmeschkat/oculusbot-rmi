package oculusbot.bot;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import oculusbot.basic.Status;

/**
 * Controls a LED on the circuit board depending on the current status. Not used at the moment.
 * @author Robert Meschkat
 *
 */
public class StatusLED {
	private static final long BLINK_FAST = 200;
	private static final long BLINK_SLOW = 500;
	private GpioPinDigitalOutput out;
	private Status status = Status.DEAD;
	
	public StatusLED(Pin pin, GpioController gpio) {
		out = gpio.provisionDigitalOutputPin(pin);
		out.low();
	}
	
	public void setStatus(Status status){
		if(this.status.equals(status)){
			return;
		}
		
		this.status = status;
		out.blink(0, PinState.LOW);
		switch (status) {
		case DEAD:
			out.low();
			return;
		case READY:
			out.high();
			return;
		case EVENT:
			out.blink(BLINK_FAST);
			return;
		case SETUP:
			out.blink(BLINK_SLOW);
			return;

		default:
			out.low();
			break;
		}
	}
	
	public void shutdown(){
		out.setShutdownOptions(true, PinState.LOW);
	}
}
