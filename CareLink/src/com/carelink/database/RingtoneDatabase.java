package com.carelink.database;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.util.Log;

public class RingtoneDatabase {
	private static RingtoneDatabase instance = null;
	private static String[] ringtoneNames = new String[0];
	private static String[] ringtonePaths = new String[0];
	
	public static void init(Context context) {
		if (instance == null) {
			instance = new RingtoneDatabase(context);
		}
	}
	
	private RingtoneDatabase(Context context) {
		RingtoneManager ringtoneManager = new RingtoneManager(context);
		ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
		Cursor cursor = ringtoneManager.getCursor();
		ringtoneNames = new String[cursor.getCount()];
		ringtonePaths = new String[cursor.getCount()];				
		if (cursor.moveToFirst()) {		    			
			do {
				ringtoneNames[cursor.getPosition()] = 
						ringtoneManager.getRingtone(cursor.getPosition()).getTitle(context);
				ringtonePaths[cursor.getPosition()] = 
						ringtoneManager.getRingtoneUri(cursor.getPosition()).toString();
			} while (cursor.moveToNext());					
		}
		Log.d(getClass().getSimpleName(), "Finished Loading " + ringtoneNames.length + " Ringtones.");
		cursor.close();
	}
	
	public static String[] getRingoneNames() {
		return ringtoneNames;
	}
	
	public static String[] getRingtonePaths() {
		return ringtonePaths;
	}
}
