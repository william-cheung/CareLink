package com.carelink.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public abstract class Record implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_GLUCOSE		= 0;
	public static final int TYPE_HEART_PARAMS	= 1;
	public static final int TYPE_INSULIN		= 2;
	public static final int TYPE_DRUGS			= 3;
	public static final int TYPE_WEIGHT			= 4;
	public static final int TYPE_SPORTS			= 5;
	public static final int TYPE_DISCOMFORT		= 6;
	public static final int TYPE_OTHERS			= 7;
	
	public static final int SRC_MANUAL			= 0;
	public static final int SRC_AUTO			= 1;
	
	private int id = -1;
	private String guid;
	private int type;
	private Date date;
	
	private int src = SRC_MANUAL;
	
	private Note note;

	public Record(int type, Date date, Note note) {
		this(type, date, note, SRC_MANUAL);
	}
	
	public Record(int type, Date date, Note note, int src) {
		this.type = type;
		this.date = date;
		this.note = note;
		this.src = src;
		this.guid = UUID.randomUUID().toString();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return "";
	}

	public Note getNote() {
		return note;
	}

	public int getSrc() {
		return src;
	}

	public void setSrc(int src) {
		this.src = src;
	}
	
	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getGuid() {
		return guid;
	}
}
