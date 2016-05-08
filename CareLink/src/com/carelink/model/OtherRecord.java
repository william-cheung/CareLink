package com.carelink.model;

import java.util.Date;

public class OtherRecord extends Record {
	private static final long serialVersionUID = 1L;
	private String text;
	
	public OtherRecord(String text, Date date) {
		super(Record.TYPE_OTHERS, date, null);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
