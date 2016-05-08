package com.carelink.ui;

import com.carelink.R;
import com.carelink.model.OtherRecord;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RecordOthersActivity extends RecordActivity {
	
	public int rid = -1;
	public String info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_record_others);
		setTitleTextResource(R.string.title_activity_record_others);
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			OtherRecord record = (OtherRecord) bundle.get(UIConstants.EXTRA_NAME_RECORD);
			if (record != null) {
				rid = record.getId();
				info = record.getText();
			}
		}
		
		final EditText infoEditText = (EditText) findViewById(R.id.editText_info);
		infoEditText.setText(info);
		infoEditText.setSelection(infoEditText.length());
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				info = infoEditText.getText().toString();
				if (info.equals("")) {
					showAlertDialog();
					return;
				}
				OtherRecord record = new OtherRecord(info, getDate().getTime());
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
