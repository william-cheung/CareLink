package com.carelink.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.GetCaptchaService;
import com.carelink.interaction.services.ResetPhoneNumberService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;

public class ChangePhoneNumberActivity extends MyActivity {
private static final String TAG = "ChangePhoneNumberActivity";
	
	private Button getCaptchaButton = null; 
	private CountDownTimer countDownTimer = null;
	
	private Context context = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_change_phone_number);
		setTitleTextResource(R.string.title_activity_change_phone_number);
		super.onCreate(savedInstanceState);
		
		context = this;
		
		getCaptchaButton = (Button) findViewById(R.id.button_getCaptcha);
		final EditText phoneEditText = (EditText) findViewById(R.id.editText_phoneNumber);
		getCaptchaButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox((Activity) context, R.string.message_no_phone_number);
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
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = phoneEditText.getText().toString();
				String captcha = captchaEditText.getText().toString();

				if (phoneNumber.equals("")) {
					MyApplication.showMessageBox((Activity) context, R.string.message_no_phone_number);
					return;
				}
				
				resetPhoneNumber(phoneNumber, captcha);
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
				Services.getInstance(getApplicationContext()).sendRequest(
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
	
	private void resetPhoneNumber(String phoneNumber, String captcha) {
		final String phone = phoneNumber;
		Services.getInstance(getApplicationContext()).sendRequest(
				new ResetPhoneNumberService().setParams(phoneNumber, LocalConfig.getCurrentUserPhone(), captcha), 
				new RequestCallback() {
			@Override
			public void onSuccess(Response response) {
				Log.d(TAG, "ResetPhone Success! " + phone);
				LocalConfig.setCurrentUserPhone(phone);
				LocalConfig.writeOut();
				
				MyApplication.showMessageBox((Activity) context, R.string.message_reset_phone_success, 
						new OnDismissListener() {
							public void onDismiss(DialogInterface dialog) {
								ChangePhoneNumberActivity.this.setResult(RESULT_OK);
								finish();
							}
						});
			}
			
			@Override
			public void onFailed(Message msg) {
				Log.d(TAG, "ResetPhone Failed! " + msg.toString());
				if (msg.getStatusCode() == Message.STATUS_PHONE_NUMBER_IS_USED) {
					MyApplication.showMessageBox((Activity) context, R.string.message_exisiting_phone_number);
				} else if (msg.getStatusCode() == Message.STATUS_CAPTCHA_VERIFY_ERROR) {
					MyApplication.showMessageBox((Activity) context, R.string.message_invalid_captcha);
				} else {
					MyApplication.showMessageBox((Activity) context, R.string.message_network_error);
				}
			}
		});
	}
}
