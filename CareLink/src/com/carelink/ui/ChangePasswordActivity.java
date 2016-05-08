package com.carelink.ui;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.ResetPasswordService;
import com.carelink.interaction.services.SignInService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChangePasswordActivity extends MyActivity {
	private static final String TAG = "ChangePasswordActivity";
	
	private Button okButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_change_password);
		setTitleTextResource(R.string.title_activity_change_password);
		super.onCreate(savedInstanceState);
		
		final EditText oldPasswordEditText = (EditText) findViewById(R.id.editText_oldPassword);
		final EditText newPasswordEditText = (EditText) findViewById(R.id.editText_newPassword);
		final EditText retypePasswordEditText = (EditText) findViewById(R.id.editText_retypeNewPassword);
		okButton = (Button)findViewById(R.id.button_ok);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String oldPassword = oldPasswordEditText.getText().toString();
				final String newPassword = newPasswordEditText.getText().toString();
				String retypePassword = retypePasswordEditText.getText().toString();

				
				if (!newPassword.equals(retypePassword)) {
					MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_new_password_not_match);
					return;
				}
				
				if (newPassword.equals("")) {
					MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_no_password);
					return;
				}
				
				if (Services.getInstance(getApplicationContext()).isOnLine()) {
					resetPassword(oldPassword, newPassword);
				} else {
					LocalConfig.setToGlblMode();
					Services.getInstance(getApplicationContext()).sendRequest(
							new SignInService().setParams(LocalConfig.getPhoneLastLoggedIn(), LocalConfig.getPasswordLastLoggedIn()), 
							new RequestCallback() {
								public void onSuccess(Response response) {
									Log.d(TAG, "sign in succeed");
									int uid = (Integer)response.getData();
									LocalConfig.setToUserMode(uid);
									resetPassword(oldPassword, newPassword);
								}
								public void onFailed(Message msg) {
									Log.d(TAG, "sign in failed " + msg);
									LocalConfig.setToUserMode(LocalConfig.getUidLastLoggedIn());
									MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_change_password_failed);
								}
							});
				}
			}
		});
	}
	
	private void resetPassword(String oldPassword, String newPassword) {
		okButton.setEnabled(false);
		Services.getInstance(getApplicationContext()).sendRequest(
				new ResetPasswordService().setParams(oldPassword, newPassword), 
				new RequestCallback() {
			@Override
			public void onSuccess(Response response) {
				Log.d(TAG, "ResetPassword Success!");
				MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_reset_password_success, 
						new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
			}
			
			@Override
			public void onFailed(Message msg) {
				Log.d(TAG, "ResetPassword Failed! " + msg.toString());
				if (msg.getStatusCode() == Message.STATUS_WRONG_PASSWORD) {
					MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_invalid_old_password);
				} else {
					MyApplication.showMessageBox(ChangePasswordActivity.this, R.string.message_network_error);
				}
				okButton.setEnabled(true);
			}
		});
	}
}
