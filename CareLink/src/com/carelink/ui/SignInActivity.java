package com.carelink.ui;

import com.carelink.R;
import com.carelink.database.RecordDatabase;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.DownloadRecordsService;
import com.carelink.interaction.services.GetUserInfoService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.interaction.services.SignInService;
import com.carelink.model.Record;
import com.carelink.model.User;
import com.carelink.util.JsonHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignInActivity extends Activity {
	private static final String DEBUG_TAG = "SignInActivity";
	
	private Context context = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		
		context = this;
		
		findViewById(R.id.textView_signUp).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), SignUpActivity.class));
			}
		});
		findViewById(R.id.textView_forgetPassword).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplication(), ResetPasswordActivity.class));
			}
		});

		LocalConfig.setToGlblMode();
		LocalConfig.setUserLoginStatus(LocalConfig.STATUS_LOGGED_OUT);
		LocalConfig.writeOut();
		
		final EditText phoneEditText = (EditText)findViewById(R.id.editText_telephone);
		final EditText passwordEditText = (EditText)findViewById(R.id.editText_password);
		String lastPhone = LocalConfig.getPhoneLastLoggedIn();
		if (!lastPhone.equals("")) {
			phoneEditText.setText(lastPhone);
			passwordEditText.requestFocus();
		}

		final Button signInButton = (Button) findViewById(R.id.button_signIn);
		signInButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String phone = phoneEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				
				if (phone.equals("") || password.equals("")) {
					MyApplication.showMessageBox((Activity)context, R.string.message_no_phone_or_password);
					return;
				}

				signInButton.setEnabled(false);
				Services.getInstance(getApplicationContext()).sendRequest(
						new SignInService().setParams(phone, password), 
						new RequestCallback() {
							@Override
							public void onSuccess(Response resp) {
								//MyApplication.toast_l(getApplicationContext(), "Login Success");
								int uid = (Integer)resp.getData();
								/**
								 * Set Global Login Status
								 */
								LocalConfig.setToGlblMode();
								LocalConfig.setUidLastLoggedIn(uid);
								LocalConfig.setPhoneLastLoggedIn(phone);
								LocalConfig.setPasswordLastLoggedIn(password);
								LocalConfig.setUserLoginStatus(LocalConfig.STATUS_LOGGED_IN);
								LocalConfig.writeOut();
								
								/**
								 * Get uid's Login Status
								 */
								LocalConfig.setToUserMode(uid);
								if (LocalConfig.getUserLoginStatus() == LocalConfig.STATUS_FIRST_LOGIN) {
									downloadUserInfo();
									downloadRemoteRecords();
								}
								LocalConfig.setUserLoginStatus(LocalConfig.STATUS_LOGGED_IN);
								LocalConfig.setCurrentUserPhone(phone);
								LocalConfig.writeOut();
								startActivity(new Intent(getApplication(), MainActivity.class));
								finish();
							}
							
							@Override
							public void onFailed(Message msg) {
								Log.d(DEBUG_TAG, "Sign In Failed " + msg);
								//MyApplication.toast_l(getApplicationContext(), "Login Failed");
								if (msg.getStatusCode() == Message.STATUS_LOGIN_WRONG_ACCOUNT) {
									MyApplication.showMessageBox((Activity)context, R.string.message_invalid_phone_or_password);
								} else {
									MyApplication.showMessageBox((Activity)context, R.string.message_network_error);
								}
								
								signInButton.setEnabled(true);
							}
						});
			}
		});
	}
	
	
	private void downloadUserInfo() {
		Services.getInstance(context).sendRequest(
				new GetUserInfoService(), 
				new RequestCallback() {
					public void onSuccess(Response response) {
						Log.d(DEBUG_TAG, "DownloadUserInfo Success!");
						User user = (User)response.getData();
						if (user != null && user.getHealthProfile() != null) {
							LocalConfig.setHealthProfile(user.getHealthProfile());
						}
					}
					public void onFailed(Message msg) {
						Log.d(DEBUG_TAG, "DownloadUserInfo Failed! " + msg.toString());
					}
				});
	}
	
	private void downloadRemoteRecords() {
		Log.d(getClass().getSimpleName(), "downloadRemoteRecords");
		Services.getInstance(context).sendRequest(
				new DownloadRecordsService().setParamsByDays(RecordDatabase.UPPER_LIMIT_IN_DAYS), 
				new RequestCallback() {
					public void onSuccess(Response response) {
						RecordDatabase.clear();
						Record[] records = (Record[])response.getData();
						for (Record record : records) {
							RecordDatabase.insert(record);
							Log.d("SignInActivity", JsonHelper.recordToJsonString(record));
						}
						notifyRecordsChanged();
 					}
					public void onFailed(Message msg) {
						Log.d(DEBUG_TAG, msg.toString());
					}
				});
	}
	
	private void notifyRecordsChanged() {
		 // Send Message to RecordsMgmtFragment
		Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
		sendBroadcast(intent);
		
		// Send Message to STATISTIC FRAGMENTS
		Intent intent2 = new Intent(UIConstants.ACTION_RECORDS_CHANGED);
		sendBroadcast(intent2);
	}
}
