package com.carelink.model;

import java.util.Date;

/**
 * @author William Cheung
 * @version 0.0.1
 * 
 * <p>Date: 2014-11-20</p> 
 */
public class HeartParamsRecord extends Record {
	private static final long serialVersionUID = 1L;
	private int systolic;
	private int diastolic;
	private int heartRate;

//	public HeartParamsRecord(int systolic, int diastolic, Date date) {
//		this.systolic = systolic;
//		this.diastolic = diastolic;
//		this.heartRate = -1;	// invalid
//		setDate(date);
//	}
//	
//	public HeartParamsRecord(int heartRate, Date date) {
//		this.systolic = this.diastolic = -1;  // invalid
//		this.heartRate = heartRate;
//		setDate(date);	
//	}

	public HeartParamsRecord(int systolic, int diastolic, int heartRate,
			Date date, Note note) {
		super(Record.TYPE_HEART_PARAMS, date, note);
		this.systolic = systolic;
		this.diastolic = diastolic;
		this.heartRate = heartRate;
	}

	public int getSystolic() {
		return systolic;
	}
	public int getDiastolic() {
		return diastolic;
	}
	
	public int getHeartRate() {
		return heartRate;
	}

	@Override
	public String getDescription() {
		return "" + systolic + "/" + diastolic +" mmHg, " + heartRate;
	}	
}
