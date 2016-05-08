package com.carelink.model;

import java.util.Date;

public class WeightRecord extends Record {
	private static final long serialVersionUID = 1L;
	private int weight;

	public WeightRecord(int weight, Date date, Note note) {
		super(Record.TYPE_WEIGHT, date, note);
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public String getDescription() {
		return "" + weight + " Kg";
	}
}
