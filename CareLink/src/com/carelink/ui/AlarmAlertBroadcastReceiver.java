package com.carelink.ui;

import com.carelink.model.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmAlertBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent alarmServiceIntent = new Intent(
				context,
				AlarmServiceBroadcastReceiver.class);
		context.sendBroadcast(alarmServiceIntent, null);
		
		StaticWakeLock.lockOn(context);
		
		// Start Main Activity
		Bundle bundle = intent.getExtras();
		final Reminder reminder = (Reminder) bundle.getSerializable(UIConstants.EXTRA_NAME_REMINDER);
		Intent alarmAlertActivityIntent = new Intent(context, MainActivity.class);
		alarmAlertActivityIntent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder);
		alarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(alarmAlertActivityIntent);
	}
}
