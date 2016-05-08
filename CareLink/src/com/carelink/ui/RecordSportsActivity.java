package com.carelink.ui;

import java.util.Calendar;

import com.carelink.R;
import com.carelink.model.SportRecord;
import com.carelink.widget.NumberPicker;
import com.carelink.widget.NumberPicker.OnValueChangeListener;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class RecordSportsActivity extends RecordActivity {
	private final String ACTIVITY_TAG = "RecordSportsActivity";

	private final int GET_SPORT_INDEX = 1; 

	private TextView sportNameTextView;
	private TextView durationTextView;

	private int rid = -1;
	private int sportIndex = -1;
	private int duration = 60;

	private boolean isNewRecord = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_sports);
		setTitleTextResource(R.string.title_activity_record_sports);
		setNoteClass(SportsNoteActivity.class);
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			SportRecord record = (SportRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				sportIndex = record.getSportIndex();
				duration = record.getDuration();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
				setNote(record.getNote());
			}

			isNewRecord = false;
		}

		View clickToSelectView = findViewById(R.id.view_clickToSelect);
		clickToSelectView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SelectSportActivity.class);
				intent.putExtra(UIConstants.EXTRA_NAME_SPORT_INDEX, sportIndex);
				startActivityForResult(intent, GET_SPORT_INDEX);
			}
		});
		sportNameTextView = (TextView) findViewById(R.id.textView_sportName);
		if (sportIndex != -1) {
			sportNameTextView.setText(SportRecord.getSportNameResId(sportIndex));
		}

		durationTextView = (TextView) findViewById(R.id.textView_duration);
		durationTextView.setText("" + duration);
		findViewById(R.id.view_editDuration).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog();
			}
		});

		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Bug !!!
				Log.d(ACTIVITY_TAG, "" + sportIndex);
				if (isNewRecord && sportIndex == -1) {
					showAlertDialog();
					return;
				}
				SportRecord record = new SportRecord(sportIndex, duration, getDate().getTime(), getNote());
				record.setId(rid);
				
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
				getContext().sendBroadcast(intent);
				finish();
			}
		});
	}

	private void showNumberPickerDialog() {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.number_picker_dialog2);

		final NumberPicker numberPicker = (NumberPicker) window.findViewById(R.id.numberPicker);
		final int minIndex = 1, maxIndex = 24, size = 24, step = 10;
		numberPicker.setMaxValue(maxIndex);
		numberPicker.setMinValue(minIndex);
		final String[] displayedValues = new String[size]; 
		for (int i = minIndex; i <= maxIndex; i++) {
			displayedValues[i - minIndex] = "" + i * step;
		}
		numberPicker.setDisplayedValues(displayedValues);
		numberPicker.setValue(duration / step);
		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				duration = Integer.parseInt(displayedValues[newVal - minIndex]);
			}
		});

		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				durationTextView.setText("" + duration);
				dlg.dismiss();
			}
		});
	}

	private void showAlertDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.alert_dialog_p);
		TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
		messageTextView.setText(R.string.message_warnning_no_sport_selected);
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_SPORT_INDEX && resultCode == RESULT_OK && data != null) {
			sportIndex = data.getIntExtra(UIConstants.EXTRA_NAME_SPORT_INDEX, -1);
			if (sportIndex != -1) {
				sportNameTextView.setText(SportRecord.getSportNameResId(sportIndex));
			}
		}
	}
}
