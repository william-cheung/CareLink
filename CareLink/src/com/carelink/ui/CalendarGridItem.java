package com.carelink.ui;


public class CalendarGridItem {
	public static final int CURR_MONTH = 0;
	public static final int PREV_MONTH = 1;
	public static final int NEXT_MONTH = 2;
	public static final int TODAY = 3;
	
	private int date;
	private int tag;

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public CalendarGridItem(int date, int tag) {
		this.date = date;
		this.tag = tag;
	}	
}