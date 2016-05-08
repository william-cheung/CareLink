package com.carelink.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.carelink.model.Reminder;
import com.carelink.util.JsonHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* 
 * usage:  
 * DatabaseSetup.init(egActivityOrContext); 
 * DatabaseSetup.createEntry() or DatabaseSetup.getContactNames() or DatabaseSetup.getDb() 
 * DatabaseSetup.deactivate() then job done 
 */

public class ReminderDatabase extends SQLiteOpenHelper {
	static ReminderDatabase instance = null;
	static SQLiteDatabase database = null;
	
	static final String DATABASE_NAME = "REMINDER_DATABASE";
	static final int DATABASE_VERSION = 1;
	
	private static String tableName = "REMINDER_TABLE";
	private static int uid = -1;
	
	public static final String COLUMN_REMINDER_ID = "_id";
	public static final String COLUMN_REMINDER_JSON = "rem_json";	
	
	private static String[] columns = new String[] { 
			COLUMN_REMINDER_ID,
			COLUMN_REMINDER_JSON,
		};
	
	private ReminderDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static void init(Context context, int uid) {
		if (null == instance || uid != ReminderDatabase.uid)  {
			instance = new ReminderDatabase(context);
			tableName = "REMINDER_TABLE_" + uid;
			getDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ( " 
					+ COLUMN_REMINDER_ID + " INTEGER primary key autoincrement, " 
					+ COLUMN_REMINDER_JSON + " TEXT NOT NULL)");
		}
	}

	public static SQLiteDatabase getDatabase() {
		if (null == database) {
			database = instance.getWritableDatabase();
		}
		return database;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public static void deactivate() {
		if (null != database && database.isOpen()) {
			database.close();
		}
		database = null;
		instance = null;
	}

	public static long insert(Reminder reminder) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_REMINDER_JSON, JsonHelper.reminderToJsonString(reminder));
		return getDatabase().insert(tableName, null, cv);
	}
	
	public static int update(Reminder reminder) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_REMINDER_JSON, JsonHelper.reminderToJsonString(reminder));				
		return getDatabase().update(tableName, cv, "_id=" + reminder.getId(), null);
	}
	
	public static int delete(Reminder reminder) {
		return delete(reminder.getId());
	}
	
	public static int delete(int id) {
		return getDatabase().delete(tableName, "_id=" + id, null);
	}
	
	public static int clear() {
		return getDatabase().delete(tableName, "1", null);
	}
	
	public static Reminder getReminder(int id) {
		Cursor cursor = getDatabase().query(tableName, columns, COLUMN_REMINDER_ID + "=" + id, null, null, null,
				null);
		Reminder reminder = null;
		if(cursor.moveToFirst()){
			String jsonString = cursor.getString(1);
			reminder = JsonHelper.jsonStringToReminder(jsonString);
			reminder.setId(cursor.getInt(0));
		}
		cursor.close();
		return reminder;
	}

	public static ArrayList<Reminder> getAllReminders() {
		ArrayList<Reminder> reminders = new ArrayList<Reminder>();
		Cursor cursor = getDatabase().query(tableName, columns, null, null, null, null,
				null);
		if (cursor.moveToFirst()) {
			do {
				String jsonString = cursor.getString(1);
				Reminder reminder = JsonHelper.jsonStringToReminder(jsonString);
				reminder.setId(cursor.getInt(0));
				reminders.add(reminder);
			} while (cursor.moveToNext());			
		}
		cursor.close();
		
		Collections.sort(reminders, new Comparator<Reminder>() {
			public int compare(Reminder lhs, Reminder rhs) {
				if (lhs.getHourOfDay() != rhs.getHourOfDay()) {
					return lhs.getHourOfDay() - rhs.getHourOfDay();
				}
				return lhs.getMinute() - rhs.getMinute();
			}
		});
		return reminders;
	}
}