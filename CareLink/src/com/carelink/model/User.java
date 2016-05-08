package com.carelink.model;

public class User {
	public static final int GENDER_UNKNOWN 	= 0;
	public static final int GENDER_MALE 	= 1;
	public static final int GENDER_FEMALE	= 2;
	
	public static final int DIABETES_NONE			= 0;
	public static final int DIABETES_TYPE_I			= 1;
	public static final int DIABETES_TYPE_II		= 2;
	public static final int DIABETES_GESTATIONAL	= 3;
	public static final int DIABETES_PRE			= 4;
	public static final int DIABETES_LADA			= 5;
	
	private int id = -1;
	private String phoneNumber = "";
	private String password	= "";
	private HealthProfile healthProfile = null;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public HealthProfile getHealthProfile() {
		return healthProfile;
	}
	public void setHealthProfile(HealthProfile healthProfile) {
		this.healthProfile = healthProfile;
	}
}
