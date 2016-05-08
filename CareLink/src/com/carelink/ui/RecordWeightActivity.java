package com.carelink.ui;

import java.util.Calendar;

import com.carelink.R;
import com.carelink.model.WeightRecord;
import com.carelink.widget.NumberPicker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class RecordWeightActivity extends RecordActivity {
	//private final String ACTIVITY_TAG = "RecordWeightActivity"; 

	private final String KEY_WEIGHT = "WEIGHT";

	private int rid = -1;
	private int weight = 60;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_weight);
		setTitleTextResource(R.string.title_activity_record_weight);
		setNoteClass(WeightNoteActivity.class);
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			WeightRecord record = (WeightRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				weight = record.getWeight();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
				setNote(record.getNote());
			}
		} else if (defaultsPreferences != null) {
			int tmp = defaultsPreferences.getInt(KEY_WEIGHT, -1);
			if (tmp != -1) weight = tmp;
		}

		findViewById(R.id.view_editWeight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showNumberPickerDialog();
			}
		});

		((TextView)findViewById(R.id.textView_weight)).setText("" + weight);

		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				WeightRecord record = new WeightRecord(weight, getDate().getTime(), getNote());
				record.setId(rid);
				
				//Log.d(ACTIVITY_TAG, JsonHelper.recordToJsonString(record));
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
				getContext().sendBroadcast(intent);

				if (defaultsPreferences != null) {
					defaultsPreferences.edit()
					.putInt(KEY_WEIGHT, weight)
					.commit();
				}

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
		numberPicker.setMaxValue(150);
		numberPicker.setMinValue(0);
		numberPicker.setValue(weight);
		View doneButton = window.findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				weight = numberPicker.getValue();
				((TextView)findViewById(R.id.textView_weight)).setText("" + weight);
				dlg.dismiss();
			}
		});
	}
}
