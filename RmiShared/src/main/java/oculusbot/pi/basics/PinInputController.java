package oculusbot.pi.basics;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PinInputController {
	private GpioPinDigitalInput input;
	private boolean high;
	private boolean lastStatus;

	public boolean isHigh() {
		return high;
	}

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
