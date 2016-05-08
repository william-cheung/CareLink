package com.carelink.util;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.carelink.model.*;
import com.google.gson.Gson;

public class JsonHelper {
	public static String recordToJsonString(Record record) {
		Gson gson = new Gson();
		return gson.toJson(record);
	}

	public static Record jsonStringToRecord(String jsonString) {
		try {
			Gson gson = new Gson();
			JSONObject jsonObject = new JSONObject(jsonString);
			int type = (Integer)jsonObject.get("type");
			Record record = null;
			switch (type) {
			case Record.TYPE_GLUCOSE:
				record = gson.fromJson(jsonString, GlucoseRecord.class);
				break;
			case Record.TYPE_HEART_PARAMS:
				record = gson.fromJson(jsonString, HeartParamsRecord.class);
				break;
			case Record.TYPE_INSULIN:
				record = gson.fromJson(jsonString, InsulinRecord.class);
				break;
			case Record.TYPE_DRUGS:
				record = gson.fromJson(jsonString, DrugRecord.class);
				break;
			case Record.TYPE_SPORTS:
				record = gson.fromJson(jsonString, SportRecord.class);
				break;
			case Record.TYPE_WEIGHT:
				record = gson.fromJson(jsonString, WeightRecord.class);
				break;
			case Record.TYPE_DISCOMFORT:
				record = gson.fromJson(jsonString, DiscomfortRecord.class);
				break;
			case Record.TYPE_OTHERS:
				record = gson.fromJson(jsonString, OtherRecord.class);
				break;
			default:
				break;
			}
			return record;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String reminderToJsonString(Reminder reminder) {
		Gson gson = new Gson();
		return gson.toJson(reminder);
	}

	public static Reminder jsonStringToReminder(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, Reminder.class);
	}
	
	public static String dateToJsonString(Date date) {
		return new Gson().toJson(date);
	}
	
	public static Date jsonStringToDate(String jsonString) {
		return new Gson().fromJson(jsonString, Date.class);
	}
}
