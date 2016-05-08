package com.carelink.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.carelink.util.Utils;

public class Reminder implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int TYPE_MEASURE_GLUCOSE 		= 0;
	public static final int TYPE_MEASURE_HEART_PARAMS 	= 1;
	public static final int TYPE_TAKE_DRUGS				= 2;
	public static final int TYPE_INSULIN				= 3;

	public static final int DEFAULT_SNOOZE_INTERVAL		= 10;
	public static final int DEFAULT_SNOOZE_TIMES		= 3;

	public static final String UNIT_DRUG	= "Ƭ/U";
	public static final String UNIT_INSULIN	= "U";

	private int id;
	private int type;
	private int hourOfDay;
	private int minute;
	private Date dateCreated;
	private String weekdayTags;
	private int snoozeInterval;
	private int snoozeTimes;
	private String ringtonePath;
	
	private int glucoseTag = GlucoseRecord.TAG_RANDOM;
	
	private String drugName;
	private int dosage;

	private Date dateStatusUpdated;
	private String status;

	public Reminder(int type, 
			int hourOfDay, int minute, String weekdayTags, 
			int snoozeIntervalInMin, int snoozeTimes, 
			String ringtonePath,
			Date dateCreated) {
		this.type = type;
		this.dateCreated = dateCreated;
		this.hourOfDay = hourOfDay;
		this.minute = minute;
		this.setWeekdayTags(weekdayTags);
		this.setSnoozeInterval(snoozeIntervalInMin);
		this.setSnoozeTimes(snoozeTimes);
		this.setRingtonePath(ringtonePath);
		this.status = "";
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getHourOfDay() {
		return hourOfDay;
	}

	public void setHourOfDay(int hourOfDay) {
		this.hourOfDay = hourOfDay;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public String getWeekdayTags() {
		return weekdayTags;
	}

	public void setWeekdayTags(String weekdayTags) {
		this.weekdayTags = weekdayTags;
	}

	public int getSnoozeInterval() {
		return snoozeInterval;
	}

	public void setSnoozeInterval(int snoozeInterval) {
		this.snoozeInterval = snoozeInterval;
	}

	public int getSnoozeTimes() {
		return snoozeTimes;
	}

	public void setSnoozeTimes(int snoozeTimes) {
		this.snoozeTimes = snoozeTimes;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * Is the reminder active at time <b>calendar</b> ?
	 */
	public boolean isActive(Calendar calendar) {
		if (isMarkedAsDone(calendar)) {
			return false;
		}

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(calendar.getTime());
		calendar2.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar2.set(Calendar.MINUTE, minute);
		calendar2.set(Calendar.SECOND, 0);
		
		boolean[] isWeekdaySelected = new boolean[7];	
		ArrayList<Integer> indices = Utils.parseIntegerList(getWeekdayTags());
		if (indices.size() == 0) {
			if (Utils.isSameDay(getDateCreated(), calendar.getTime())) {
				if (calendar2.getTimeInMillis() > calendar.getTimeInMillis()) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			for (Integer index : indices) {
				isWeekdaySelected[index] = true;
			}
			int weekday = calendar.get(Calendar.DAY_OF_WEEK);
			weekday = (weekday - 2 + 7) % 7;
			if (isWeekdaySelected[weekday]) {
				if (calendar2.getTimeInMillis() > calendar.getTimeInMillis()) {
					return true;
				} else {
					return false;
				}
			} 
		}
		return false;
	}

	/**
	 * Is the reminder active at current time ?
	 */
	public boolean isActive() {
		return isActive(Calendar.getInstance());
	}

	public boolean isMarkedAsDone(Calendar calendar) {
		ArrayList<Integer> status = Utils.parseIntegerList(getStatus());
		if (status.size() > 0 && !Utils.before(getDateStatusUpdated(), Utils.getStartOfDay(calendar.getTime()))) {
			int diff = Utils.diffInDays(getDateStatusUpdated(), calendar.getTime()); 
			if (diff < status.size()) {
				int index = status.size() - 1 - diff;
				if (status.get(index) == 1) {
					return true;
				}
			}
		}
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRingtonePath() {
		return ringtonePath;
	}

	public void setRingtonePath(String ringtonePath) {
		this.ringtonePath = ringtonePath;
	}

	public int getGlucoseTag() {
		return glucoseTag;
	}

	public void setGlucoseTag(int glucoseTag) {
		this.glucoseTag = glucoseTag;
	}

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public int getDosage() {
		return dosage;
	}

	public void setDosage(int dosage) {
		this.dosage = dosage;
	}

	public String getDescription() {
		if (type == TYPE_TAKE_DRUGS) {
			return drugName + "\t\t" + dosage + " " + UNIT_DRUG;
		} else if (type == TYPE_INSULIN) {
			return drugName + "\t\t" + dosage + " " + UNIT_INSULIN;
		}
		return "";
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDateStatusUpdated() {
		return dateStatusUpdated;
	}

	public void setDateStatusUpdated(Date dateStatusUpdated) {
		this.dateStatusUpdated = dateStatusUpdated;
	}
}
