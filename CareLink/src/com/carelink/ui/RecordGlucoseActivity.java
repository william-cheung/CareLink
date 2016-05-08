package com.carelink.ui;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.carelink.R;
import com.carelink.database.ReminderDatabase;
import com.carelink.model.GlucoseRecord;
import com.carelink.model.Reminder;
import com.carelink.util.JsonHelper;
import com.carelink.util.Utils;
import com.carelink.widget.NumberPicker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

@SuppressLint({ "DefaultLocale"}) 
public class RecordGlucoseActivity extends RecordActivity {

	public static final String ACTIVITY_TAG = "RecordGlucoseActivity";
	public static final String TIMEPICKER_TAG = "timepicker";
	public static final String DATEPICKER_TAG = "datepicker";

	private final String KEY_GLUCOSE_VALUE_INT_PART = "GLUCOSE_VALUE_INT_PART";
	private final String KEY_GLUCOSE_VALUE_DEC_PART = "GLUCOSE_VALUE_DEC_PART";

	private Map<Integer, Integer> tagMap;
	private Map<Integer, Integer>  rTagMap;
	private TextView glucoseValueTextView;

	private int rid = -1;
	private int glucoseValueIntPart = 5;
	private int glucoseValueDecPart = 0;
	private int tag = GlucoseRecord.TAG_RANDOM;

//	private boolean startedForAnExistingRecord = false;

	private boolean  startedForTask = false;
	private Reminder reminder = null;
	private Calendar startedCalendar = null;

	@SuppressLint("UseSparseArrays") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_glucose);
		setTitleTextResource(R.string.title_activity_record_glucose);
		setNoteClass(GlucoseNoteActivity.class);
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			GlucoseRecord record = (GlucoseRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				tag = record.getTag();
				int[] parts = Utils.parseFloatValue(record.getValue());
				glucoseValueIntPart = parts[0];
				glucoseValueDecPart = parts[1];
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
				setNote(record.getNote());

//				startedForAnExistingRecord = true;
			}
		} else if (defaultsPreferences != null) {
			int tmp = defaultsPreferences.getInt(KEY_GLUCOSE_VALUE_INT_PART, -1);
			if (tmp != -1) glucoseValueIntPart = tmp;
			tmp = defaultsPreferences.getInt(KEY_GLUCOSE_VALUE_DEC_PART, -1);
			if (tmp != -1) glucoseValueDecPart = tmp;
		}

		if (bundle != null) {
			reminder = (Reminder) bundle.get(UIConstants.EXTRA_NAME_REMINDER);
			startedCalendar = (Calendar) bundle.get(UIConstants.EXTRA_NAME_CALENDAR);
			if (reminder != null && startedCalendar != null) {
				startedForTask = true;
				
				tag = reminder.getGlucoseTag();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startedCalendar.getTime());
				calendar.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
				calendar.set(Calendar.MINUTE, reminder.getMinute());
				setDate(calendar);
			}
		}

		tagMap = new HashMap<Integer, Integer>();
		tagMap.put(GlucoseRecord.TAG_BEFORE_BREAKFAST, R.id.button_tag0);
		tagMap.put(GlucoseRecord.TAG_BEFORE_LUNCH, R.id.button_tag1);
		tagMap.put(GlucoseRecord.TAG_BEFORE_DINNER, R.id.button_tag2);
		tagMap.put(GlucoseRecord.TAG_BEFORE_SLEEP, R.id.button_tag3);
		tagMap.put(GlucoseRecord.TAG_AFTER_BREAKFAST, R.id.button_tag4);
		tagMap.put(GlucoseRecord.TAG_AFTER_LUNCH, R.id.button_tag5);
		tagMap.put(GlucoseRecord.TAG_AFTER_DINNER, R.id.button_tag6);
		tagMap.put(GlucoseRecord.TAG_RANDOM, R.id.button_tag7);
		rTagMap = new HashMap<Integer, Integer>();
		rTagMap.put(R.id.button_tag0, GlucoseRecord.TAG_BEFORE_BREAKFAST);
		rTagMap.put(R.id.button_tag1, GlucoseRecord.TAG_BEFORE_LUNCH);
		rTagMap.put(R.id.button_tag2, GlucoseRecord.TAG_BEFORE_DINNER);
		rTagMap.put(R.id.button_tag3, GlucoseRecord.TAG_BEFORE_SLEEP);
		rTagMap.put(R.id.button_tag4, GlucoseRecord.TAG_AFTER_BREAKFAST);
		rTagMap.put(R.id.button_tag5, GlucoseRecord.TAG_AFTER_LUNCH);
		rTagMap.put(R.id.button_tag6, GlucoseRecord.TAG_AFTER_DINNER);
		rTagMap.put(R.id.button_tag7, GlucoseRecord.TAG_RANDOM);

		int resid = tagMap.get(tag);
		TextView textView = (TextView) findViewById(resid);
		textView.setBackgroundResource(R.drawable.tag_bg_selected);
		textView.setTextColor(getResources().getColor(R.color.white));

		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				int resid = tagMap.get(tag);
				TextView textView = (TextView) findViewById(resid);
				textView.setBackgroundResource(R.drawable.tag_bg_normal);
				textView.setTextColor(getResources().getColor(R.color.theme));
				tag = rTagMap.get(v.getId());
				textView = (TextView) v;
				textView.setBackgroundResource(R.drawable.tag_bg_selected);
				textView.setTextColor(getResources().getColor(R.color.white));
			}
		};
		findViewById(R.id.button_tag0).setOnClickListener(listener);
		findViewById(R.id.button_tag1).setOnClickListener(listener);
		findViewById(R.id.button_tag2).setOnClickListener(listener);
		findViewById(R.id.button_tag3).setOnClickListener(listener);
		findViewById(R.id.button_tag4).setOnClickListener(listener);
		findViewById(R.id.button_tag5).setOnClickListener(listener);
		findViewById(R.id.button_tag6).setOnClickListener(listener);
		findViewById(R.id.button_tag7).setOnClickListener(listener);

		View editGlucoseValueView = findViewById(R.id.view_editGlucoseValue);
		editGlucoseValueView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog();
			}
		});
		glucoseValueTextView = (TextView) findViewById(R.id.textView_glucoseValue);
		glucoseValueTextView.setText(String.format("%d.%d", glucoseValueIntPart, glucoseValueDecPart));

		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(ACTIVITY_TAG, "button_ok onClick()");
				GlucoseRecord record = new GlucoseRecord(
						Utils.calcFloatValue(glucoseValueIntPart, glucoseValueDecPart), 
						getDate().getTime(), 
						tag, getNote());
				record.setId(rid);

				Log.d(ACTIVITY_TAG, JsonHelper.recordToJsonString(record));
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
//				if (startedForAnExistingRecord) {
//					Log.d(ACTIVITY_TAG, "startedForAnExistingRecord : " + startedForAnExistingRecord);
//					intent.putExtra(UIConstants.EXTRA_NAME_BOOLEAN, startedForAnExistingRecord);
//				}
				getContext().sendBroadcast(intent);

				if (defaultsPreferences != null) {
					Editor editor = defaultsPreferences.edit();
					editor.putInt(KEY_GLUCOSE_VALUE_INT_PART, glucoseValueIntPart);
					editor.putInt(KEY_GLUCOSE_VALUE_DEC_PART, glucoseValueDecPart);
					editor.commit();
				}

				if (startedForTask) {
					reminder = updateReminderStatus(reminder, startedCalendar.getTime());
					ReminderDatabase.update(reminder);

					Log.d(ACTIVITY_TAG, JsonHelper.reminderToJsonString(reminder));

					MyApplication.callAlarmScheduleService(getApplicationContext());
					Intent intent2 = new Intent(UIConstants.ACTION_REMINDERS_CHANGED);
					sendBroadcast(intent2);
				}

				finish();
			}
		});
	}


	private void showNumberPickerDialog() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.number_picker_dialog);
		final NumberPicker numberPicker1 = (NumberPicker) window.findViewById(R.id.numberPicker1);
		numberPicker1.setMaxValue(14);
		numberPicker1.setMinValue(0);
		numberPicker1.setValue(glucoseValueIntPart);
		final NumberPicker numberPicker2 = (NumberPicker) window.findViewById(R.id.numberPicker2);
		numberPicker2.setMaxValue(9);
		numberPicker2.setMinValue(0);
		numberPicker2.setValue(glucoseValueDecPart);
		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				glucoseValueIntPart = numberPicker1.getValue();
				glucoseValueDecPart = numberPicker2.getValue();
				String nString = String.format("%d.%d", glucoseValueIntPart, glucoseValueDecPart);
				glucoseValueTextView.setText(nString);
				dlg.dismiss();
			}
		});
	}
}
