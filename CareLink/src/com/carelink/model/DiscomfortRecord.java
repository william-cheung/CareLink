package com.carelink.model;

import java.util.Date;

public class DiscomfortRecord extends Record {
	private static final long serialVersionUID = 1L;
	private String tags = "";
	private String text = "";
	public DiscomfortRecord(String tags, String text, Date date) {
		super(Record.TYPE_DISCOMFORT, date, null);
		this.tags = tags;
		this.text = text;
	}
	public String getTags() {
		return tags;
	}
	public String getText() {
		return text;
	}
}
