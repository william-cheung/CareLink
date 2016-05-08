package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.carelink.R;
import com.carelink.model.Note;
import com.carelink.model.Reminder;
import com.carelink.timepicker.RadialPickerLayout;
import com.carelink.timepicker.TimePickerDialog;
import com.carelink.timepicker.TimePickerDialog.OnTimeSetListener;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat") 
public class RecordActivity extends MyActivity implements OnTimeSetListener {
	//private final String ACTIVITY_TAG = "RecordActivity";
	public static final String TIMEPICKER_TAG = "timepicker";
	public static final String DATEPICKER_TAG = "datepicker";
	public static final int REQUEST_NOTE = 1;

	private TextView recordDateTextView;
	private TextView recordTimeTextView;
	private Calendar calendar;
	
	private Class<?> noteClass; 
	private Note note = null;
	
	protected SharedPreferences defaultsPreferences = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		defaultsPreferences = MyApplication.getUserPreferences(
				UIConstants.PREFERENCES_NAME_DEFAULTS, MODE_PRIVATE);
		
		View addNoteView = findViewById(R.id.view_addNote);
		if (addNoteView != null) {
			addNoteView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), noteClass);
					intent.putExtra(UIConstants.EXTRA_NAME_NOTE, note);
					startActivityForResult(intent, REQUEST_NOTE);
				}
			});
		}
		
		calendar = Calendar.getInstance();	
		//Log.d(ACTIVITY_TAG, "MyApplication.calendar: " + MyApplication.getInstance().calendar.getTime());
		calendar.set(
				MyApplication.getInstance().calendar.get(Calendar.YEAR), 
				MyApplication.getInstance().calendar.get(Calendar.MONTH),
				MyApplication.getInstance().calendar.get(Calendar.DATE));
		//Log.d(ACTIVITY_TAG, "RecordActivity.calendar: " + calendar.getTime());
		
		recordDateTextView = (TextView) findViewById(R.id.textView_recordDate);
		if (recordDateTextView != null) {
			recordDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
		}
		
		recordTimeTextView = (TextView) findViewById(R.id.textView_recordTime);
		if (recordTimeTextView != null) {
			recordTimeTextView.setText(new SimpleDateFormat("hh:mm a").format(calendar.getTime()));
		}
		findViewById(R.id.view_editRecordDate).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showTimePickerDialog();
			}
		});
	}
	
	public void setDate(Date date) {
		calendar.setTime(date);
		recordDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
	}

	private void showTimePickerDialog() {
		final TimePickerDialog timePickerDialog = 
				TimePickerDialog.newInstance(this, 
						calendar.get(Calendar.HOUR_OF_DAY), 
						calendar.get(Calendar.MINUTE), 
						false, false);
		timePickerDialog.show(getFragmentManager(), TIMEPICKER_TAG);
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		recordTimeTextView.setText(new SimpleDateFormat("hh:mm a").format(calendar.getTime()));
	}
	
	protected void setDate(Calendar calendar) {
		this.calendar = calendar;
		recordDateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
		recordTimeTextView.setText(new SimpleDateFormat("hh:mm a").format(calendar.getTime()));
	}

	protected Calendar getDate() {
		return calendar;
	}
	
	protected Context getContext() {
		return this;
	}
	
	protected void setNoteClass(Class<?> noteClass) {
		this.noteClass = noteClass;
	}
	
	protected void setNote(Note note) {
		this.note = note;
	}
	
	protected Note getNote() {
		return note;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RecordActivity.REQUEST_NOTE && resultCode == RESULT_OK && data != null) {
			Log.d(getClass().getSimpleName(), "onActivityResult");
			Note note = (Note) data.getSerializableExtra(UIConstants.EXTRA_NAME_NOTE);
			if (note != null) {
				Log.d(getClass().getSimpleName(), note.toString());
				setNote(note);
			}
		}
	}
	
	protected Reminder updateReminderStatus(Reminder reminder, Date dateUpdating) {
		ArrayList<Integer> status = Utils.parseIntegerList(reminder.getStatus());
		// status of a reminder must be "" or "... 1"
		if (status.size() == 0) {
			reminder.setStatus("1");
			reminder.setDateStatusUpdated(dateUpdating);
		} else {
			int diff = Utils.diffInDays(dateUpdating, reminder.getDateStatusUpdated());
			// diff must not be 0 !
			if (diff > 0) {
				diff -= 1;
				for (int i = 0; i < diff; i++) {
					status.add(0);
				}
				status.add(1);
				reminder.setDateStatusUpdated(dateUpdating);
			} else { // diff < 0
				diff = -diff; 	// diff : dateUpdated - dateStarted
				if (diff >= status.size()) {
					for (int i = diff - status.size(); i > 0; i--) {
						status.add(0, 0);
					}
					status.add(0, 1);
				} else {
					status.set(status.size() - 1 - diff, 1);
				}
			}
			
			// Clean status of reminder
			if (status.size() > UIConstants.MAX_DAYS_SHOW_TASK_STAUS) {
				int i = status.size() - UIConstants.MAX_DAYS_SHOW_TASK_STAUS;
				for (int j = 0; j < i; j++) {
					status.remove(0);
				}
				while (status.get(0) == 0) {
					status.remove(0);
				}
			}
			reminder.setStatus(Utils.integersToString(status));
		}
		
		return reminder;
	}
}
