package com.carelink.ui;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.GetCaptchaService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.SignUpService;
import com.carelink.interaction.services.ServiceBase.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SignUpActivity extends Activity {
	private static final String TAG = "SignUpActivity";
	
	private Context context = null;
	
	private Button getCaptchaButton = null; 
	private boolean checkStatus = false;
	private CountDownTimer countDownTimer = null;
	
	private Button signUpButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		
		context = this;
		
		getCaptchaButton = (Button) findViewById(R.id.button_getCaptcha);
		final EditText phoneEditText = (EditText) findViewById(R.id.editText_phoneNumber);
		getCaptchaButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox(SignUpActivity.this, R.string.message_no_phone_number);
					return;
				}
				sendGetCaptchaRequest(phoneNumber);
			}
		});
		
		countDownTimer = new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				getCaptchaButton.setText(
						String.format(getString(R.string.get_captcha_waiting_format), millisUntilFinished / 1000));
			}

			@Override
			public void onFinish() {
				getCaptchaButton.setText(R.string.get_captcha);
				getCaptchaButton.setEnabled(true);
			}
		};
		
		final EditText captchaEditText = (EditText) findViewById(R.id.editText_captcha);
		final EditText passwordEditText = (EditText) findViewById(R.id.editText_password);
		final EditText retypePasswordEditText = (EditText) findViewById(R.id.editText_retypePassword);
		
		signUpButton = (Button) findViewById(R.id.button_signUp);
		signUpButton.setEnabled(false);
		signUpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				String captcha = captchaEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String retypePassword = retypePasswordEditText.getText().toString();
				
				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox(SignUpActivity.this, R.string.message_no_phone_number);
					return;
				}
				
				if (!password.equals(retypePassword)) {
					MyApplication.showMessageBox(SignUpActivity.this, R.string.message_password_not_match);
					return;
				}
				
				if (password.equals("")) {
					MyApplication.showMessageBox(SignUpActivity.this, R.string.message_no_password);
					return;
				}
				
				signUp(phoneNumber, captcha,  password);
				//startActivity(new Intent(getApplicationContext(), CompleteInfoActivity.class));
			}
		});
		
		TextView showAgreements = (TextView) findViewById(R.id.textView_agreements);
		showAgreements.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		final ImageView checkImageView = (ImageView) findViewById(R.id.imageView_btnCheck);
		checkImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (checkStatus == false) {
					checkImageView.setImageResource(R.drawable.btn_check_to_on);
					signUpButton.setEnabled(true);
					checkStatus = true;
				} else {
					checkImageView.setImageResource(R.drawable.btn_check_to_off);
					signUpButton.setEnabled(false);
					checkStatus = false;
				}
			}
		});
	}
	
	private void sendGetCaptchaRequest(String phoneNumber) {
		final AlertDialog dialog = new AlertDialog.Builder(SignUpActivity.this).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.alert_dialog_pn);
		TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
		messageTextView.setText(getString(R.string.message_send_vericode_to_phone_number) + " " + phoneNumber);
		final String phone = phoneNumber;
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getCaptchaButton.setEnabled(false);
				countDownTimer.start();
				Services.getInstance(SignUpActivity.this).sendRequest(
						new GetCaptchaService().setParams(phone), 
						new RequestCallback() {
					@Override
					public void onSuccess(Response response) {
						Log.d(TAG, "GetCaptchaRequest Success");
					}
					
					@Override
					public void onFailed(Message msg) {
						Log.d(TAG, "GetCaptchaRequest Failed " + msg);
						MyApplication.showMessageBox((Activity) context, R.string.message_request_captcha_failed);
						countDownTimer.cancel();
						getCaptchaButton.setText(R.string.get_captcha);
						getCaptchaButton.setEnabled(true);
					}
				});
				dialog.dismiss();
			}
		});
		window.findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	private void signUp(String phoneNumber, String captcha, String password) {
		signUpButton.setEnabled(false);
		final String phone = phoneNumber;
		final String passwd = password; 
		Services.getInstance(SignUpActivity.this).sendRequest(
				new SignUpService().setParams(phoneNumber, captcha, password), 
				new RequestCallback() {
			@Override
			public void onSuccess(Response resp) {
				//MyApplication.toast_l(getApplicationContext(), "SignUp Success");
				int uid = (Integer)resp.getData();
				/**
				 * Set Global Login Status
				 */
				LocalConfig.setToGlblMode();
				LocalConfig.setUidLastLoggedIn(uid);
				LocalConfig.setPhoneLastLoggedIn(phone);
				LocalConfig.setPasswordLastLoggedIn(passwd);
				LocalConfig.setUserLoginStatus(LocalConfig.STATUS_LOGGED_IN);
				LocalConfig.writeOut();
				
				/**
				 * Get uid's Login Status
				 */
				LocalConfig.setToUserMode(uid);
				LocalConfig.setUserLoginStatus(LocalConfig.STATUS_LOGGED_IN);
				LocalConfig.setCurrentUserPhone(phone);
				LocalConfig.writeOut();
				
				startActivity(new Intent(getApplicationContext(), CompleteInfoActivity.class));
				finish();
			}
			
			@Override
			public void onFailed(Message msg) {
				//MyApplication.toast_l(getApplicationContext(), "SignUp Failed");
				if (msg.getStatusCode() == Message.STATUS_PHONE_NUMBER_IS_USED) {
					String hint = String.format(getString(R.string.message_phone_number_in_use), phone);
					MyApplication.showMessageBox((Activity)context, hint);
				} else if (msg.getStatusCode() == Message.STATUS_CAPTCHA_VERIFY_ERROR) {
					MyApplication.showMessageBox((Activity)context, R.string.message_invalid_captcha);
				} else {
					MyApplication.showMessageBox((Activity)context, R.string.message_network_error);
				}
				signUpButton.setEnabled(true);
			}
		});
	}
}
