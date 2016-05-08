package com.carelink.model;

import android.annotation.SuppressLint;
import java.util.Date;

/**
 * @author William Cheung
 * @version 0.0.1
 * 
 * <p>Date: 2014-11-20</p> 
 */
public class GlucoseRecord extends Record {
	private static final long serialVersionUID = 1L;
	
	public static final int TAG_RANDOM          = 0;
	public static final int TAG_BEFORE_BREAKFAST= 1;
	public static final int TAG_BEFORE_LUNCH    = 2;
	public static final int TAG_BEFORE_DINNER   = 3;
	public static final int TAG_AFTER_BREAKFAST = 4;
	public static final int TAG_AFTER_LUNCH     = 5;
	public static final int TAG_AFTER_DINNER    = 6; 
	public static final int TAG_BEFORE_SLEEP    = 7;
	
	public static final float THRESHOLD_BEFORE_MEAL_HIGH 	= 11.1f;
	public static final float THRESHOLD_BEFORE_MEAL_LOW		= 0.0f;
	public static final float THRESHOLD_AFTER_MEAL_HIGH		= 0.0f;
	public static final float THRESHOLD_AFTER_MEAL_LOW		= 3.9f;
	
	public static final String UNIT = "mmol/L";
	
	private float value;  
	private int tag;
	
	/**
	 * @return value in mmol/L
	 */
	public float getValue() {
		return value;
	}
	
	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
	
	/**
	 * @param value glucose value (in mmol/L)
	 */	
	public GlucoseRecord(float value, Date date, int tag, Note note) {
		this(value, date, tag, note, SRC_MANUAL);
	}
	
	public GlucoseRecord(float value, Date date, int tag, Note note, int src) {
		super(Record.TYPE_GLUCOSE, date, note, src);
		this.value = value;
		this.tag = tag;
	}

	@SuppressLint("DefaultLocale") 
	@Override
	public String toString() {
		String valstr = String.format("%4.1f", value);
		String[] tags = {"RANDOM", "BEFORE_BREAKFAST", "BEFORE_LUNCH", "BEFORE_DINNER", 
				"AFTER_BREAKFAST", "AFTER_LUNCH", "AFTER_DINNER", "TAG_BEFORE_SLEEP"};
		return "Record [value : " + valstr + "mmol/L, date : " + getDate() + " tag: " + tags[tag] + "]";
	}

	@Override
	public String getDescription() {
		return "" + value + " " + UNIT;
	}
}
