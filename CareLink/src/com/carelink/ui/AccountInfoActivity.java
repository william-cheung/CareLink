package com.carelink.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.carelink.R;

public class AccountInfoActivity extends MyActivity {
	//private static final String TAG = "AccountInfoActivity";
	
	private static final int REQUEST_CODE = 1;
	
	private TextView phoneTextView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_account_info);
		setTitleTextResource(R.string.title_activity_account_info);
		super.onCreate(savedInstanceState);
		
		phoneTextView = (TextView) findViewById(R.id.textView_phoneNumber);
		phoneTextView.setText(LocalConfig.getCurrentUserPhone());
		findViewById(R.id.view_resetPhoneNumber).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ChangePhoneNumberActivity.class);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
		findViewById(R.id.view_resetPassword).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			phoneTextView.setText(LocalConfig.getCurrentUserPhone());
		}
	}
}
