package com.carelink.ui;

import com.carelink.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MyActivity extends Activity {
	private final String ACTIVITY_TAG = "MyActivity";

	private int customTitleResID 	= -1;
	private int layoutResID 		= -1;
	private int titleResID 			= -1;
	
	private HomeKeyEventReceiver homeKeyEventReceiver = new HomeKeyEventReceiver();
	private IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	private OnHomePressedListener onHomePressedListener = null;
	private OnHomeLongPressedListener onHomeLongPressedListener = null;

	public void setCustomTitleResource(int customTitleResID) {
		this.customTitleResID = customTitleResID;
	}

	public void setContentViewResource(int layoutResID) {
		this.layoutResID = layoutResID;
	}

	public void setTitleTextResource(int titleResID) {
		this.titleResID = titleResID;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.CustomTheme);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(layoutResID);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, customTitleResID);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		View backButton = findViewById(R.id.button_back);
		if (backButton != null) {
			backButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
		}
		TextView title = (TextView) findViewById(R.id.textView_title);
		if (title != null) {
			if (titleResID != -1) {
				title.setText(titleResID);
			} else {
				title.setText("");
			}
		}
		
		registerReceiver(homeKeyEventReceiver, intentFilter);
	}
	
	private class HomeKeyEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra("reason");
				if (reason != null) {
					if (reason.equals("homekey")) {
						Log.d(ACTIVITY_TAG, "onHomePressed()");
						if (onHomePressedListener != null) {
							onHomePressedListener.onHomePressed();
						}
					} else if (reason.equals("recentapps")) {
						Log.d(ACTIVITY_TAG, "onHomeLongPressed()");
						if (onHomeLongPressedListener != null) {
							onHomeLongPressedListener.onHomeLongPressed();
						}
					}
				}
			}
		}
	}
	
	public interface OnHomePressedListener {
		public void onHomePressed();
	}
	
	public interface OnHomeLongPressedListener {
		public void onHomeLongPressed();
	}
	
	public void setOnHomePressedListener(OnHomePressedListener listener) {
		onHomePressedListener = listener;
	}
	
	public void setOnHomeLongPressedListener(OnHomeLongPressedListener listener) {
		onHomeLongPressedListener = listener;
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(homeKeyEventReceiver);
		super.onDestroy();
	}
}
