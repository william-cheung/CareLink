package com.device.lifescan;

import com.comminterface.CommInterface;
import com.device.GlucoseMeter;

public class OneTouchUltra extends OneTouchMeter {

	public OneTouchUltra(CommInterface commInterface) {
		super(commInterface);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getMeterName() {
		return "OneTouch Ultra";
	}

	@Override
	public int getMaxNRecords() {
		return 150;
	}

	@Override
	public int getMeterType() {
		return GlucoseMeter.ONETOUCH_ULTRA;
	}
}
