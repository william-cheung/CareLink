package com.carelink.model;

import java.io.Serializable;

public class Drug implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
//	private int dosageMin;
//	private int dosageMax;
//	private int dosageUnit;
//	private int frequencyPerDay;
	
	public Drug(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
