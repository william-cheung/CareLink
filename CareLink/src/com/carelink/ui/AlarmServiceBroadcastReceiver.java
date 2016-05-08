package com.carelink.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlarmServiceBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AlarmServiceBroadcastReciever", "onReceive()");
		Intent serviceIntent = new Intent(context, AlarmService.class);
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			int uid = bundle.getInt(UIConstants.EXTRA_NAME_UID, -1);
			if (uid != -1) {
				serviceIntent.putExtra(UIConstants.EXTRA_NAME_UID, uid);
			}
		}
		context.startService(serviceIntent);
	}
}
