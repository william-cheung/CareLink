package com.carelink.model;

import java.util.Date;

public class DrugRecord extends Record {
	private static final long serialVersionUID = 1L;
	private Drug drug;
	private int dosage;
	public DrugRecord(Drug drug, int dosage, Date date, Note note) {
		super(Record.TYPE_DRUGS, date, note);
		this.drug = drug;
		this.dosage = dosage;
	}
	public Drug getDrug() {
		return drug;
	}
	public int getDosage() {
		return dosage;
	}

	@Override
	public String getDescription() {
		if (drug != null) {
			return drug.getName();
		} 
		return "";
	}
}
