package com.device;

import com.carelink.model.GlucoseRecord;

public abstract class GlucoseMeter {
	// Better not change the value of constants below, William
	public static final int ONETOUCH_ULTRA		= 1;
	public static final int ONETOUCH_ULTRA2		= 2;
	public static final int ONETOUCH_ULTRAEASY 	= 3;
	public static final int ONETOUCH_ULTRAMINI	= 4;
	
	public static final int ONETOUCH_SELECT		= 11;
	
	public static final int FREESTYLE_FREEDOM	= 21;
	
	/**
	 * Get the Meter's name
	 */
	public abstract String getMeterName();
	
	public abstract int getMeterType();
	
	public boolean powerOn() {
		return true;
	}
	
	public GlucoseRecord[] getAllRecords() {
		return null;
	}
	
	public boolean deleteAllRecords() {
		return false;
	}
}
