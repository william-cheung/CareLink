package com.carelink.ui;

/**
 * TextListItem along with TextListAdapter is designed for List of Name Strings in an alert dialog.
 * 
 * @author william
 */

public class TextListItem {
	private String text;
	public TextListItem(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
}
