package com.carelink.ui;

import java.util.Calendar;

import com.carelink.R;
import com.carelink.database.ReminderDatabase;
import com.carelink.model.Drug;
import com.carelink.model.DrugRecord;
import com.carelink.model.Reminder;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RecordDrugsActivity extends RecordActivity {
	
	private static final int REQUEST_DRUG = 10;
	
	private int rid = -1;
	private Drug drug = null;
	private int dosage = -1;
	
	private boolean  startedForTask = false;
	private Reminder reminder = null;
	private Calendar startedCalendar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_drugs);
		setTitleTextResource(R.string.title_activity_record_drugs);
		setNoteClass(DrugsNoteActivity.class);
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			DrugRecord record = (DrugRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				drug = record.getDrug();
				dosage = record.getDosage();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
				setNote(record.getNote());
			}
		} 
		
		if (bundle != null) {
			reminder = (Reminder) bundle.get(UIConstants.EXTRA_NAME_REMINDER);
			startedCalendar = (Calendar) bundle.get(UIConstants.EXTRA_NAME_CALENDAR);
			if (reminder != null && startedCalendar != null) {
				startedForTask = true;
				
				drug = new Drug(reminder.getDrugName());
				dosage = reminder.getDosage();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startedCalendar.getTime());
				calendar.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
				calendar.set(Calendar.MINUTE, reminder.getMinute());
				setDate(calendar);
			}
		}
		
		TextView drugNameTextView = (TextView) findViewById(R.id.textView_drugName);
		if (drug != null) {
			drugNameTextView.setText(drug.getName());
		}
		
		TextView dosageTextView = (TextView) findViewById(R.id.textView_dosage);
		if (dosage != -1) {
			dosageTextView.setText("" + dosage);
		}
		
		findViewById(R.id.view_clickToSelect).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SelectDrugActivity.class);
				startActivityForResult(intent, REQUEST_DRUG);
			}
		});
		
		findViewById(R.id.view_editDosage).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(RecordDrugsActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView = (TextView) findViewById(R.id.textView_dosage);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_drug_dosage);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
				if (dosage != -1) {
					editText.setText(textView.getText());
					editText.setSelection(textView.getText().length());
				}
				final TextView textView3 = textView;
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String text = editText.getText().toString();
						if (!text.equals("")) {
							textView3.setText(text);
							dosage = Integer.parseInt(text);
						}
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (drug == null) {
					MyApplication.showMessageBox(RecordDrugsActivity.this, 
							R.string.message_warnning_no_drug_selected);
					return;
				}
				
				if (dosage == -1) {
					MyApplication.showMessageBox(RecordDrugsActivity.this, 
							R.string.message_warnning_no_dosage);
					return;
				}
				
				DrugRecord record = new DrugRecord(drug, dosage, getDate().getTime(), getNote());
				record.setId(rid);
				
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
				getContext().sendBroadcast(intent);
				
				if (startedForTask) {
					reminder = updateReminderStatus(reminder, startedCalendar.getTime());
					ReminderDatabase.update(reminder);

					// Log.d(ACTIVITY_TAG, JsonHelper.reminderToJsonString(reminder));
					MyApplication.callAlarmScheduleService(getApplicationContext());
					Intent intent2 = new Intent(UIConstants.ACTION_REMINDERS_CHANGED);
					sendBroadcast(intent2);
				}
				
				finish();
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DRUG && resultCode == RESULT_OK && data != null) {
			Drug drug = (Drug) data.getSerializableExtra(UIConstants.EXTRA_NAME_DRUG);
			if (drug != null) {
				this.drug = drug;
				TextView textView = (TextView) findViewById(R.id.textView_drugName);
				textView.setText(drug.getName());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
