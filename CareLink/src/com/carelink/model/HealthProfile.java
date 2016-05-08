package com.carelink.model;

import java.util.Date;

public class HealthProfile {
	private String name 			= "";
	private int gender 				= -1;
	private Date birthDate 			= null;
	private String phone 			= "";
	private int height				= -1;
	private int weight				= -1;
	private String allergyHistory	= "";
	private int typeOfDiabetes		= -1;
	private int yearsOfIllness 		= -1;
	private String hospitalName 	= "";
	private String doctorName		= "";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getAllergyHistory() {
		return allergyHistory;
	}
	public void setAllergyHistory(String allergyHistory) {
		this.allergyHistory = allergyHistory;
	}
	public int getTypeOfDiabetes() {
		return typeOfDiabetes;
	}
	public void setTypeOfDiabetes(int typeOfDiabetes) {
		this.typeOfDiabetes = typeOfDiabetes;
	}
	public int getYearsOfIllness() {
		return yearsOfIllness;
	}
	public void setYearsOfIllness(int yearsOfIllness) {
		this.yearsOfIllness = yearsOfIllness;
	}
	public String getHospitalName() {
		return hospitalName;
	}
	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}
	public String getDoctorName() {
		return doctorName;
	}
	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}
}