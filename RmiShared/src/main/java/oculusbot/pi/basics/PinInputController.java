package oculusbot.pi.basics;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Class to handle GPIO input.
 * @author Robert Meschkat
 *
 */
public class PinInputController {
	private GpioPinDigitalInput input;
	private boolean high;

	/**
	 * Returns the current state of this pin. 
	 * @return
	 */
	public boolean isHigh() {
		return high;
	}

	/**
	 * Uses the Pi4J-library to read the pin state. 
	 * @param in The GPIO pin to use as input.
	 * @param gpio The GPIO controller (only one per application). 
	 */
	public PinInputController(Pin in, GpioController gpio) {
		input = gpio.provisionDigitalInputPin(in, PinPullResistance.PULL_UP);
		high = input.getState().isHigh();

		input.addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				high = event.getState().isHigh();
			}
		});
	}

	@Override
	public String toString() {
		return high ? "HIGH" : "LOW";
	}

}
