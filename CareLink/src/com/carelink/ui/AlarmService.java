package com.carelink.ui;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.carelink.database.ReminderDatabase;
import com.carelink.model.Reminder;
import com.carelink.util.JsonHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AlarmService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(this.getClass().getSimpleName(),"onCreate()");
		super.onCreate();		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(this.getClass().getSimpleName(),"onStartCommand()");
		Bundle bundle = intent.getExtras();
		Reminder reminder = null;
		if (bundle != null) {
			int uid = bundle.getInt(UIConstants.EXTRA_NAME_UID, -1);
			if (uid != -1) {
				reminder = getNextReminder(uid);
			}
		}
		
		Log.d(this.getClass().getSimpleName(), "next : " + JsonHelper.reminderToJsonString(reminder));
		if(null != reminder){
			schedule(reminder);
		} else {
			Intent myIntent = new Intent(getApplicationContext(), AlarmAlertBroadcastReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);			
			AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			
			alarmManager.cancel(pendingIntent);
		}
		return START_NOT_STICKY;
	}
	
	private Reminder getNextReminder(int uid){
		Set<Reminder> reminderQueue = new TreeSet<Reminder> (new Comparator<Reminder>() {
			@Override
			public int compare(Reminder lhs, Reminder rhs) {
				if (lhs.getHourOfDay() != rhs.getHourOfDay()) {
					return lhs.getHourOfDay() - rhs.getHourOfDay();
				}
				return lhs.getMinute() - rhs.getMinute();
			}
		});
			
		reminderQueue.clear();
		ReminderDatabase.init(getApplicationContext(), uid);
		List<Reminder> reminders = ReminderDatabase.getAllReminders();
		Log.d(this.getClass().getSimpleName(), "reminders : " + reminders.size());
		for(Reminder reminder : reminders) {
			Log.d(this.getClass().getSimpleName(), JsonHelper.reminderToJsonString(reminder));
			if(reminder.isActive()) {
				Log.d(this.getClass().getSimpleName(), "active : true");
				reminderQueue.add(reminder);
			} else {
				Log.d(this.getClass().getSimpleName(), "active : false");
			}
		}
		if(reminderQueue.iterator().hasNext()){
			return reminderQueue.iterator().next();
		} else {
			return null;
		}
	}
	
	private void schedule(Reminder reminder) {
		Intent myIntent = new Intent(getApplicationContext(), AlarmAlertBroadcastReceiver.class);
		myIntent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
		calendar.set(Calendar.MINUTE, reminder.getMinute());
		calendar.set(Calendar.SECOND, 0);
		Log.d(getClass().getName(), calendar.getTime().toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);					
	}
}
