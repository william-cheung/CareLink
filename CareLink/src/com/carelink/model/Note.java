package com.carelink.model;

import java.io.Serializable;

public class Note implements Serializable {
	private static final long serialVersionUID = 1L;
	private String tags = "";
	private String text = "";
	
	public Note(String tags, String text) {
		this.tags = tags;
		this.text = text;
	}
	
	public String getTags() {
		return tags;
	}
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "{\"tags\":" + tags + ",\"text\":" + text + "}";
	}
}
