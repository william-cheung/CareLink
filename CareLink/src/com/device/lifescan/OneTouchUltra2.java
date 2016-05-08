package com.device.lifescan;

import com.comminterface.CommInterface;
import com.device.GlucoseMeter;

public class OneTouchUltra2 extends OneTouchMeter {

	public OneTouchUltra2(CommInterface commInterface) {
		super(commInterface);
	}

	@Override
	public int getMeterType() {
		return GlucoseMeter.ONETOUCH_ULTRA2;
	}

	@Override
	public String getMeterName() {
		return "OneTouch Ultra2";
	}
}
