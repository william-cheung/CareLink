package com.carelink.ui;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.GetCaptchaService;
import com.carelink.interaction.services.ResetPasswordService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ResetPasswordActivity extends Activity {
	private static final String TAG = "ResetPasswordActivity";
	
	private Button getCaptchaButton = null; 
	private CountDownTimer countDownTimer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		getCaptchaButton = (Button) findViewById(R.id.button_getCaptcha);
		final EditText phoneEditText = (EditText) findViewById(R.id.editText_phoneNumber);
		getCaptchaButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_no_phone_number);
					return;
				}
				sendGetCaptchaRequest(phoneNumber);
			}
		});
		
		countDownTimer = new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				getCaptchaButton.setText(
						String.format(getString(R.string.get_captcha_waiting_format2), millisUntilFinished / 1000));
			}

			@Override
			public void onFinish() {
				getCaptchaButton.setText(R.string.click_to_get_captcha);
				getCaptchaButton.setEnabled(true);
			}
		};
		
		final EditText captchaEditText = (EditText) findViewById(R.id.editText_captcha);
		final EditText passwordEditText = (EditText) findViewById(R.id.editText_password);
		final EditText retypePasswordEditText = (EditText) findViewById(R.id.editText_retypePassword);
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				String captcha = captchaEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String retypePassword = retypePasswordEditText.getText().toString();

				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_no_phone_number);
					return;
				}
				
				if (!password.equals(retypePassword)) {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_password_not_match);
					return;
				}
				
				if (password.equals("")) {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_no_password);
					return;
				}
				
				resetPassword(phoneNumber, captcha, password);
			}
		});
	}
	
	
	private void sendGetCaptchaRequest(String phoneNumber) {
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
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
				Services.getInstance(ResetPasswordActivity.this).sendRequest(
						new GetCaptchaService().setParams(phone), 
						new RequestCallback() {
					@Override
					public void onSuccess(Response response) {
						Log.d(TAG, "GetCaptcha Success!");
					}
					
					@Override
					public void onFailed(Message msg) {
						Log.d(TAG, "GetCaptcha Failed! " + msg.toString());
						MyApplication.toast_s(getApplicationContext(), getString(R.string.message_request_captcha_failed));
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
	
	private void resetPassword(String phoneNumber, String captcha, String password) {
		Services.getInstance(getApplicationContext()).sendRequest(
				new ResetPasswordService().setParams(phoneNumber, captcha, password), 
				new RequestCallback() {
			@Override
			public void onSuccess(Response response) {
				Log.d(TAG, "ResetPassword Success!");
				MyApplication.toast_l(getApplicationContext(), getString(R.string.message_reset_password_success));
				finish();
			}
			
			@Override
			public void onFailed(Message msg) {
				Log.d(TAG, "ResetPassword Failed! " + msg.toString());
				if (msg.getStatusCode() == Message.STATUS_CAPTCHA_VERIFY_ERROR) {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_invalid_captcha);
				} else {
					MyApplication.showMessageBox(ResetPasswordActivity.this, R.string.message_network_error);
				}
			}
		});
	}
}
