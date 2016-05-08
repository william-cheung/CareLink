package com.carelink.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.carelink.model.Record;
import com.carelink.util.JsonHelper;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** 
 * usage:  <br />
 * DatabaseSetup.init(egActivityOrContext); <br/>
 * DatabaseSetup.createEntry() or DatabaseSetup.getContactNames() or DatabaseSetup.getDb()  <br />
 * DatabaseSetup.deactivate() then job done  
 */
public class RecordDatabase extends SQLiteOpenHelper {
	static RecordDatabase instance = null;
	static SQLiteDatabase database = null;

	static final String DATABASE_NAME = "RECORD_DATABASE";
	static final int DATABASE_VERSION = 1;

	private static String tableName = "RECORD_TABLE";
	private static int uid = -1;
	
	//public static final String RECORD_TABLE = "record";
	public static final String COLUMN_RECORD_ID 	= "_id";
	public static final String COLUMN_RECORD_DATE 	= "date";
	public static final String COLUMN_RECORD_TYPE	= "type";
	public static final String COLUMN_RECORD_JSON	= "json";

	private static final int INDEX_RECORD_ID 	= 0;
	//	private static final int INDEX_RECORD_DATE	= 1;
	//	private static final int INDEX_RECORD_TYPE 	= 2;
	private static final int INDEX_RECORD_JSON	= 3;

	private static String[] columns = new String[] { 
		COLUMN_RECORD_ID, 
		COLUMN_RECORD_DATE,
		COLUMN_RECORD_TYPE,
		COLUMN_RECORD_JSON,};

	public static final int UPPER_LIMIT_IN_DAYS = 120;

	private RecordDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static void init(Context context, int uid) {
		if (null == instance || uid != RecordDatabase.uid) {
			instance = new RecordDatabase(context);
			tableName = "RECORD_TABLE_" + uid;
			getDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ( " 
					+ COLUMN_RECORD_ID 		+ " INTEGER primary key autoincrement, " 
					+ COLUMN_RECORD_TYPE	+ " INTEGER, "
					+ COLUMN_RECORD_DATE 	+ " TEXT NOT NULL, "
					+ COLUMN_RECORD_JSON 	+ " TEXT NOT NULL)");
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

	public static void dropTable() {
		if (database != null) {
			database.execSQL("DROP TABLE IF EXISTS " + tableName);
		}
	}

	public static void deactivate() {
		if (null != database && database.isOpen()) {
			database.close();
		}
		database = null;
		instance = null;
	}

	@SuppressLint("SimpleDateFormat") 
	private static String getDateString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	public static long insert(Record record) {
		//Log.d(DATABASE_NAME, "insert : " + JsonHelper.recordToJsonString(record));
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RECORD_DATE, getDateString(record.getDate()));
		cv.put(COLUMN_RECORD_TYPE, record.getType());
		cv.put(COLUMN_RECORD_JSON, JsonHelper.recordToJsonString(record));
		return getDatabase().insert(tableName, null, cv);
	}

	public static int update(Record record) {
		//Log.d(DATABASE_NAME, "update : " + JsonHelper.recordToJsonString(record));	
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RECORD_DATE, getDateString(record.getDate()));
		cv.put(COLUMN_RECORD_TYPE, record.getType());
		cv.put(COLUMN_RECORD_JSON, JsonHelper.recordToJsonString(record));
		return getDatabase().update(tableName, cv, "_id=" + record.getId(), null);
	}

	public static Record getRecord(int id) {
		Cursor cursor = getDatabase().query(tableName, columns, COLUMN_RECORD_ID + "=" + id, null, null, null,
				null);
		Record record = null;
		if(cursor.moveToFirst()){
			String jsonString = cursor.getString(INDEX_RECORD_JSON);
			record = JsonHelper.jsonStringToRecord(jsonString);
			record.setId(cursor.getInt(INDEX_RECORD_ID));
		}
		cursor.close();
		return record;
	}

	public static boolean isRecordsInLocal(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.roll(Calendar.DATE, 1);
		Date tomorrow = Utils.getStartOfDay(calendar.getTime());
		date = Utils.getStartOfDay(date);
		long diff = Utils.diffInMillis(tomorrow, date);
		diff = diff / 86400000;
		if (diff > UPPER_LIMIT_IN_DAYS) {
			return false;
		}
		return true;
	}

	private static Comparator<Record> comparator = new Comparator<Record>() {
		public int compare(Record lhs, Record rhs) {
			long time1 = lhs.getDate().getTime();
			long time2 = rhs.getDate().getTime();
			if (time1 > time2) return 1;
			else if (time1 < time2) return -1;
			return 0;
		}
	};

	public static ArrayList<Record> getRecords(Date date) {
		if (!isRecordsInLocal(date)) {
			return null;
		}

		ArrayList<Record> records = new ArrayList<Record>();
		String selection = COLUMN_RECORD_DATE + "=?";
		String[] selectionArgs = {getDateString(date)};
		Cursor cursor = getDatabase().query(tableName, columns, selection, selectionArgs, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String jsonString = cursor.getString(INDEX_RECORD_JSON);
				Record record = JsonHelper.jsonStringToRecord(jsonString);
				// Log.d("RecordDatabase", jsonString);
				record.setId(cursor.getInt(INDEX_RECORD_ID));
				records.add(record);
			} while (cursor.moveToNext());			
		}
		cursor.close();

		Collections.sort(records, comparator);
		return records;
	}

	public static ArrayList<Record> getRecords(int type) {
		ArrayList<Record> records = new ArrayList<Record>();
		String selection = COLUMN_RECORD_TYPE + "=?";
		String[] selectionArgs = {"" + type};
		Cursor cursor = getDatabase().query(tableName, columns, selection, selectionArgs, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String jsonString = cursor.getString(INDEX_RECORD_JSON);
				Record record = JsonHelper.jsonStringToRecord(jsonString);
				record.setId(cursor.getInt(INDEX_RECORD_ID));
				records.add(record);
			} while (cursor.moveToNext());			
		}
		cursor.close();

		Collections.sort(records, comparator);
		return records;
	}

	public static ArrayList<Record> getAllRecords() {
		ArrayList<Record> records = new ArrayList<Record>();
		Cursor cursor = getDatabase().query(tableName, columns, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				String jsonString = cursor.getString(INDEX_RECORD_JSON);
				Record record = JsonHelper.jsonStringToRecord(jsonString);
				//Log.d("getAllRecords in the database", jsonString);
				record.setId(cursor.getInt(INDEX_RECORD_ID));
				records.add(record);
			} while (cursor.moveToNext());			
		}
		cursor.close();

		Collections.sort(records, comparator);
		return records;
	}


	public static int delete(Record record){
		return delete(record.getId());
	}

	public static int delete(int id){
		return getDatabase().delete(tableName, "_id=" + id, null);
	}

	public static void clean() {
		ArrayList<Record> records = getAllRecords();
		for (Record record : records) {
			if(!isRecordsInLocal(record.getDate())) {
				delete(record);
			} else {
				break;
			}
		}
	}

	public static int clear(){
		return getDatabase().delete(tableName, "1", null);
	}
}