package com.carelink.model;

import java.util.Date;

public class InsulinRecord extends Record {
	private static final long serialVersionUID = 1L;
	private Insulin insulin;
	private int dosage;
	public InsulinRecord(Insulin insulin, int dosage, Date date, Note note) {
		super(Record.TYPE_INSULIN, date, note);
		this.insulin = insulin;
		this.dosage = dosage;
	}
	public Insulin getInsulin() {
		return insulin;
	}
	public int getDosage() {
		return dosage;
	}
	
	@Override
	public String getDescription() {
		if (insulin != null) {
			return insulin.getName();
		}
		return "";
	}
}
