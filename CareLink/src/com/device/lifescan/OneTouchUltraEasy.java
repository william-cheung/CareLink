package com.device.lifescan;

import com.comminterface.CommInterface;
import com.device.GlucoseMeter;

public class OneTouchUltraEasy extends OneTouchMeter2 {

public OneTouchUltraEasy(CommInterface commInterface) {
		super(commInterface);
	}

	@Override
	public String getMeterName() {
		return "OneTouch UltraEasy";
	}
	
	@Override
	public int getMaxNRecords() {
		return 500;
	}

	@Override
	public int getMeterType() {
		return GlucoseMeter.ONETOUCH_ULTRAEASY;
	}
}

