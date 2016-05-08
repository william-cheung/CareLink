package com.carelink.ui;

import java.util.Calendar;

import com.carelink.R;
import com.carelink.database.ReminderDatabase;
import com.carelink.model.HeartParamsRecord;
import com.carelink.model.Reminder;
import com.carelink.widget.NumberPicker;
import com.carelink.widget.NumberPicker.OnValueChangeListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

@SuppressLint("UseValueOf") 
public class RecordHeartParamActivity extends RecordActivity {

	private final String KEY_SYSTOLIC_PRESSURE 	= "SYSTOLIC_PRESSURE";
	private final String KEY_DIASTOLIC_PRESSURE = "DIASTOLIC_PRESSURE";
	private final String KEY_HEART_RATE 		= "HEART_RATE";

	private int rid = -1;
	private int diastolicPressure = 80;
	private int systolicPressure = 120;
	private int heartRate = 75;

	private boolean  startedForTask = false;
	private Reminder reminder = null;
	private Calendar startedCalendar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_heart_param);
		setTitleTextResource(R.string.title_activity_record_heart_param);
		setNoteClass(HeartParamNoteActivity.class);
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			HeartParamsRecord record = (HeartParamsRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				diastolicPressure = record.getDiastolic();
				systolicPressure = record.getSystolic();
				heartRate = record.getHeartRate();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
				setNote(record.getNote());
			}
		} else if (defaultsPreferences != null) {
			int tmp = defaultsPreferences.getInt(KEY_SYSTOLIC_PRESSURE, -1);
			if (tmp != -1) systolicPressure = tmp;
			tmp = defaultsPreferences.getInt(KEY_DIASTOLIC_PRESSURE, -1);
			if (tmp != -1) diastolicPressure = tmp;
			tmp = defaultsPreferences.getInt(KEY_HEART_RATE, -1);
			if (tmp != -1) heartRate = tmp;
		}
		
		
		if (bundle != null) {
			reminder = (Reminder) bundle.get(UIConstants.EXTRA_NAME_REMINDER);
			startedCalendar = (Calendar) bundle.get(UIConstants.EXTRA_NAME_CALENDAR);
			if (reminder != null && startedCalendar != null) {
				startedForTask = true;
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startedCalendar.getTime());
				calendar.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
				calendar.set(Calendar.MINUTE, reminder.getMinute());
				setDate(calendar);
			}
		}
		
		findViewById(R.id.view_editDiastolic).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog1();
			}
		});

		findViewById(R.id.view_editSystolic).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog2();
			}
		});

		findViewById(R.id.view_editHeartRate).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog3();
			}
		});


		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				HeartParamsRecord record = new HeartParamsRecord(systolicPressure, diastolicPressure, heartRate, 
						getDate().getTime(), getNote());
				record.setId(rid);
				
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
				getContext().sendBroadcast(intent);

				if (defaultsPreferences != null) {
					defaultsPreferences.edit()
					.putInt(KEY_SYSTOLIC_PRESSURE, systolicPressure)
					.putInt(KEY_DIASTOLIC_PRESSURE, diastolicPressure)
					.putInt(KEY_HEART_RATE, heartRate)
					.commit();
				}
				
				if (startedForTask) {
					reminder = updateReminderStatus(reminder, startedCalendar.getTime());
					ReminderDatabase.update(reminder);

					//Log.d(ACTIVITY_TAG, JsonHelper.reminderToJsonString(reminder));

					MyApplication.callAlarmScheduleService(getApplicationContext());
					Intent intent2 = new Intent(UIConstants.ACTION_REMINDERS_CHANGED);
					sendBroadcast(intent2);
				}

				finish();
			}
		});

		((TextView)findViewById(R.id.textView_diastolicPressure)).setText("" + diastolicPressure);
		((TextView)findViewById(R.id.textView_systolicPressure)).setText("" + systolicPressure);
		((TextView)findViewById(R.id.textView_heartRate)).setText("" + heartRate);
	}

	private void showNumberPickerDialog1() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.number_picker_dialog2);
		final NumberPicker numberPicker = (NumberPicker) window.findViewById(R.id.numberPicker);
		final int minIndex = 1, maxIndex = 16, size = 16, step = 10;
		numberPicker.setMaxValue(maxIndex);
		numberPicker.setMinValue(minIndex);
		final String[] displayedValues = new String[size]; 
		for (int i = minIndex; i <= maxIndex; i++) {
			displayedValues[i - minIndex] = "" + i * step;
		}
		numberPicker.setDisplayedValues(displayedValues);
		numberPicker.setValue(diastolicPressure / step);
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				diastolicPressure = Integer.parseInt(displayedValues[newVal - minIndex]);
			}
		});

		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//diastolicPressure = numberPicker.getValue();
				((TextView)findViewById(R.id.textView_diastolicPressure)).setText("" + diastolicPressure);
				dlg.dismiss();
			}
		});
	}

	private void showNumberPickerDialog2() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.number_picker_dialog2);

		final NumberPicker numberPicker = (NumberPicker) window.findViewById(R.id.numberPicker);
		final int minIndex = 1, maxIndex = 20, size = 20, step = 10;
		numberPicker.setMaxValue(maxIndex);
		numberPicker.setMinValue(minIndex);
		final String[] displayedValues = new String[size]; 
		for (int i = minIndex; i <= maxIndex; i++) {
			displayedValues[i - minIndex] = "" + i * step;
		}
		numberPicker.setDisplayedValues(displayedValues);
		numberPicker.setValue(systolicPressure / step);
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				systolicPressure = Integer.parseInt(displayedValues[newVal - minIndex]);
			}
		});

		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//systolicPressure = numberPicker.getValue();
				((TextView)findViewById(R.id.textView_systolicPressure)).setText("" + systolicPressure);
				dlg.dismiss();
			}
		});
	}

	private void showNumberPickerDialog3() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.number_picker_dialog2);
		final NumberPicker numberPicker = (NumberPicker) window.findViewById(R.id.numberPicker);
		numberPicker.setMaxValue(120);
		numberPicker.setMinValue(20);
		numberPicker.setValue(heartRate);
		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				heartRate = numberPicker.getValue();
				((TextView)findViewById(R.id.textView_heartRate)).setText("" + heartRate);
				dlg.dismiss();
			}
		});
	}	
}
