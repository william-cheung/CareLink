package com.carelink.model;

import java.util.Date;

import com.carelink.ui.UIConstants;

public class SportRecord extends Record {
	private static final long serialVersionUID = 1L;
	
	public static final int SPORT_RUN_FAST = 0;
	public static final int SPORT_RUN_SLOWLY = 1;
	public static final int SPORT_WALK_FAST = 2;
	public static final int SPORT_WALK_SLOWLY = 3;
	public static final int SPORT_DANCING = 4;
	public static final int SPORT_SWIMMING = 5;

	public SportRecord(int sportIndex, int duration, Date date, Note note) {
		super(Record.TYPE_SPORTS, date, note);
		this.duration = duration;
		this.sportIndex = sportIndex;
	}

	private int sportIndex;
	private int duration;

	public int getSportIndex() {
		return sportIndex;
	}
	
	public int getDuration() {
		return duration;
	}

	@Override
	public String getDescription() {
//		Resources resources = MyApplication.getInstance().getResources();
//		return resources.getString(Constants.SPORTS_NAME_RESID[sportIndex]) +
//				" " + duration + " " +
//				resources.getString(R.string.text_sports_duration_unit);
		return "" + duration + " min";
	}
	
	public static final int getSportType(int sportIndex) {
		if (sportIndex == -1) return -1;
		return UIConstants.SPORTS[sportIndex][UIConstants.SPORT_TYPE_INDEX];
	}
	
	public static final int getSportNameResId(int sportIndex) {
		if (sportIndex == -1) return -1;
		return UIConstants.SPORTS[sportIndex][UIConstants.SPORT_NAME_RESID_INDEX];
	}
	
	public static final int getSportIconResId(int sportIndex) {
		if (sportIndex == -1) return -1;
		return UIConstants.SPORTS[sportIndex][UIConstants.SPORT_ICON_RESID_INDEX];
	}
}
