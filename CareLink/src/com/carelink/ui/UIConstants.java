package com.carelink.ui;

import com.carelink.R;
import com.carelink.model.SportRecord;

public class UIConstants {

	public static final String ACTION_ADD_RECORDS 	= "ACTION_ADD_RECORD";
	public static final String EXTRA_NAME_RECORD 	= "RECORD";
	public static final String EXTRA_NAME_NOTE		= "NOTE"; 
	public static final String EXTRA_NAME_BOOLEAN	= "BOOLEAN";

	public static final int SPORT_TYPE_INDEX 		= 0;
	public static final int SPORT_NAME_RESID_INDEX 	= 1;
	public static final int SPORT_ICON_RESID_INDEX 	= 2;
	public static final int[][] SPORTS = {
		{SportRecord.SPORT_RUN_FAST, 		R.string.text_run_fast, 	R.drawable.ic_run_fast}, 
		{SportRecord.SPORT_RUN_SLOWLY, 		R.string.text_run_slowly, 	R.drawable.ic_run_slowly},
		{SportRecord.SPORT_WALK_FAST, 		R.string.text_walk_fast,	R.drawable.ic_walk_fast},
		{SportRecord.SPORT_WALK_SLOWLY,		R.string.text_walk_slowly,	R.drawable.ic_walk_slowly},
		{SportRecord.SPORT_DANCING,			R.string.text_dancing,		R.drawable.ic_dancing},
		{SportRecord.SPORT_SWIMMING,		R.string.text_swimming,		R.drawable.ic_swimming},
	};

	public static final String EXTRA_NAME_SPORT_INDEX 		= "SPORT_INDEX";

	public static final String PREFERENCES_NAME_RECORDS 	= "RECORDS";
	public static final String PREFERENCES_NAME_DEFAULTS 	= "DEFAULTS";
	public static final String PREFERENCES_NAME_PROFILE		= "PROFILE";
	public static final String PREFERENCES_NAME_SETTINGS 	= "SETTINGS";
	
	public static final String ACTION_DATE_CHANGED	= "ACTION_DATE_CHANGED";
	public static final String EXTRA_NAME_CALENDAR 	= "CALENDAR";
	
	public static final String EXTRA_NAME_REMINDER 	= "REMINDER";
	
	public static final String ACTION_REMINDERS_CHANGED		= "ACTION_REMINDERS_CHANGED";
	
	public static final String EXTRA_NAME_DRUG		= "DRUG";
	public static final String EXTRA_NAME_INSULIN	= "INSULIN";
	
	public static final String ACTION_RECORDS_CHANGED		= "ACTION_RECORDS_CHANGED";
	public static final String EXTRA_NAME_RECORD_TYPE		= "RECORD_TYPE";
	
	public static final String SETTINGS_KEY_METER			= "METER";
	
	public static final int MAX_DAYS_SHOW_TASK_STAUS		= 30;
	
	public static final String ACTION_CLOSE_MAIN_ACTIVITY	= "ACTION_CLOSE_MAIN_ACTIVITY";
	
	public static final String EXTRA_NAME_UID				= "UID";
}
