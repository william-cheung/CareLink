package com.carelink.ui;

import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.model.DiscomfortRecord;
import com.carelink.util.Utils;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class RecordDiscomfortActivity extends RecordActivity {
	private static final String TAG = "RecordDiscomfortActivity"; 
	
	private int rid = -1;
	private String tags = " ";
	private String extraInfo = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_discomfort);
		setTitleTextResource(R.string.title_activity_record_discomfort);
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			DiscomfortRecord record = (DiscomfortRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				tags = record.getTags();
				extraInfo = record.getText();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(record.getDate());
				setDate(calendar);
			}
		}
		
		ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
		scrollView.setHorizontalScrollBarEnabled(false);
		scrollView.setVerticalScrollBarEnabled(false);
		
		final ArrayList<TextView> tagTextViews = new ArrayList<TextView>();
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag0));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag1));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag2));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag3));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag4));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag5));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag6));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag7));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag8));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag9));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag10));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag11));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag12));
		tagTextViews.add((TextView) findViewById(R.id.discomfort_tag13));
		final boolean[] tagSelected = new boolean[tagTextViews.size()];	
		
		ArrayList<Integer> indices = Utils.parseIntegerList(tags);
		for (Integer index : indices) {
			tagSelected[index] = true;
			TextView tagTextView = tagTextViews.get(index);
			tagTextView.setBackgroundResource(R.drawable.tag_bg_selected);
			tagTextView.setTextColor(getResources().getColor(R.color.white));
		}
		
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < tagTextViews.size(); i++) {
					TextView tag = tagTextViews.get(i);
					if (v.getId() == tag.getId()) {
						if (tagSelected[i]) {
							tagSelected[i] = false; 
							tag.setBackgroundResource(R.drawable.tag_bg_normal);
							tag.setTextColor(getResources().getColor(R.color.theme));
						} else {
							tagSelected[i] = true; 
							tag.setBackgroundResource(R.drawable.tag_bg_selected);
							tag.setTextColor(getResources().getColor(R.color.white));
						}
					}
				}
			}
		};
		for (TextView tag : tagTextViews) {
			tag.setOnClickListener(listener);
		}
		
		final EditText extraInfoEditText = (EditText) findViewById(R.id.editText_extraInfo);
		Log.d(TAG, "" + extraInfo);
		//extraInfoEditText.setText(extraInfo);
		//extraInfoEditText.setSelection(extraInfo.length());
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tags = "";
				for (int i = 0; i < tagSelected.length; i++) {
					if (tagSelected[i] == true) {
						tags += "" + i + " ";
					}
				}
				extraInfo = extraInfoEditText.getText().toString();
				if (tags.equals("") && extraInfo.equals("")) {
					showAlertDialog();
					return;
				}
				DiscomfortRecord record = new DiscomfortRecord(tags, extraInfo, getDate().getTime());
				record.setId(rid);
				Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
				intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
				getContext().sendBroadcast(intent);
				finish();
			}
		});
	}
	
	private void showAlertDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.alert_dialog_p);
		TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
		messageTextView.setText(R.string.message_warnning_empty_record);
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
}
