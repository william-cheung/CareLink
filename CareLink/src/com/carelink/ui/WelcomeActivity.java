package com.carelink.ui;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.interaction.services.SignInService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		LocalConfig.setToGlblMode();
		int loginStatus = LocalConfig.getUserLoginStatus();
		if (loginStatus == LocalConfig.STATUS_LOGGED_IN) {
			Services.getInstance(getApplicationContext()).sendRequest(
					new SignInService().setParams(LocalConfig.getPhoneLastLoggedIn(), 
							LocalConfig.getPasswordLastLoggedIn()), 
					new RequestCallback() {
						@Override
						public void onSuccess(Response resp) {
							//MyApplication.toast_l(getApplicationContext(), "Auto-login Success");
							int uid = (Integer) resp.getData();
							LocalConfig.setToUserMode(uid);
							startActivity(new Intent(getApplicationContext(), MainActivity.class));
							WelcomeActivity.this.finish();
						}

						@Override
						public void onFailed(Message msg) {
							if (msg.getStatusCode() == Message.STATUS_NETWORK_ERROR) {
								MyApplication.toast_l(getApplicationContext(), getString(R.string.message_network_error));
								LocalConfig.setToGlblMode();
								int uid = LocalConfig.getUidLastLoggedIn();
								if (uid != -1) {
									LocalConfig.setToUserMode(uid);
									startActivity(new Intent(getApplicationContext(), MainActivity.class));
								} else {
									startActivity(new Intent(getApplicationContext(), SignInActivity.class));
								}
							} else {
								startActivity(new Intent(getApplicationContext(), SignInActivity.class));
							}
							WelcomeActivity.this.finish();
						}
					});
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			startActivity(new Intent(getApplicationContext(), SignInActivity.class));
			WelcomeActivity.this.finish();
		}
	}
}
