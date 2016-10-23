package oculusbot.pi.basics;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Remapping of the GPIO pins so that the naming is coherent with the default
 * naming of the GPIO of the RaspberryPi.
 * 
 * @author Robert Meschkat
 *
 */
public interface Pins {
	Pin GPIO_02 = RaspiPin.GPIO_08;
	Pin GPIO_03 = RaspiPin.GPIO_09;
	Pin GPIO_04 = RaspiPin.GPIO_07;
	Pin GPIO_05 = RaspiPin.GPIO_21;
	Pin GPIO_06 = RaspiPin.GPIO_22;
	Pin GPIO_07 = RaspiPin.GPIO_11;
	Pin GPIO_08 = RaspiPin.GPIO_10;
	Pin GPIO_09 = RaspiPin.GPIO_13;
	Pin GPIO_10 = RaspiPin.GPIO_12;
	Pin GPIO_11 = RaspiPin.GPIO_14;
	Pin GPIO_12 = RaspiPin.GPIO_26;
	Pin GPIO_13 = RaspiPin.GPIO_23;
	Pin GPIO_14 = RaspiPin.GPIO_15;
	Pin GPIO_15 = RaspiPin.GPIO_16;
	Pin GPIO_16 = RaspiPin.GPIO_27;
	Pin GPIO_17 = RaspiPin.GPIO_00;
	Pin GPIO_18 = RaspiPin.GPIO_01;
	Pin GPIO_19 = RaspiPin.GPIO_24;
	Pin GPIO_20 = RaspiPin.GPIO_28;
	Pin GPIO_21 = RaspiPin.GPIO_29;
	Pin GPIO_22 = RaspiPin.GPIO_03;
	Pin GPIO_23 = RaspiPin.GPIO_04;
	Pin GPIO_24 = RaspiPin.GPIO_05;
	Pin GPIO_25 = RaspiPin.GPIO_06;
	Pin GPIO_26 = RaspiPin.GPIO_25;
	Pin GPIO_27 = RaspiPin.GPIO_02;
}
