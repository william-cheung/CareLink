package com.carelink.ui;

import android.os.Bundle;

import com.carelink.R;

public class SelectMeterActivity extends MyActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setTitleTextResource(R.string.title_activity_select_meter);
		setContentViewResource(R.layout.activity_select_meter);
		super.onCreate(savedInstanceState);
	}
}
