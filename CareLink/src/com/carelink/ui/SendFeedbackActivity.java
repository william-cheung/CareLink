package com.carelink.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.SendFeedbackService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;

public class SendFeedbackActivity extends MyActivity {

	private EditText messageEditText = null;
	private Button sendButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_send_feedback);
		setTitleTextResource(R.string.title_activity_send_feedback);
		super.onCreate(savedInstanceState);

		messageEditText = (EditText) findViewById(R.id.editText_message);
		sendButton = (Button)findViewById(R.id.button_send);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message = messageEditText.getText().toString();
				if (message.equals("")) {
					MyApplication.showMessageBox(SendFeedbackActivity.this, R.string.message_invalid_feedback);
				} else {
					Services.getInstance(getApplicationContext()).sendRequest(
							new SendFeedbackService().setParams(message), 
							new RequestCallback() {
								public void onSuccess(Response response) {
									Log.d("SendFeedbackActivity", "SendFeedback Success!");
								}
								public void onFailed(Message msg) {
									Log.d("SendFeedbackActivity", "SendFeedback Failed! " + msg.toString());
								}
							});
					finish();
				}
			}
		});
	}
}
