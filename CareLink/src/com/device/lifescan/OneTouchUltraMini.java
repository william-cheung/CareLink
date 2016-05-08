package com.device.lifescan;

import com.comminterface.CommInterface;
import com.device.GlucoseMeter;

public class OneTouchUltraMini extends OneTouchMeter2 {

	public OneTouchUltraMini(CommInterface commInterface) {
		super(commInterface);
	}

	@Override
	public String getMeterName() {
		return "OneTouch UltraMini";
	}

	@Override
	public int getMeterType() {
		return GlucoseMeter.ONETOUCH_ULTRAMINI;
	}
}
