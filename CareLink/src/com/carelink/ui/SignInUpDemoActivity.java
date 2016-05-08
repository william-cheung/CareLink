package com.carelink.ui;

import com.carelink.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SignInUpDemoActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in_up_demo);
		
		findViewById(R.id.button_signIn).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), SignInActivity.class));
			}
		});
		
		findViewById(R.id.button_signUp).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), SignUpActivity.class));
			}
		});
	}
}
