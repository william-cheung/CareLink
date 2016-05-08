package com.carelink.ui;

import java.util.Calendar;

import com.carelink.R;
import com.carelink.database.DrugDatabase;
import com.carelink.database.InsulinDatabase;
import com.carelink.database.RingtoneDatabase;
import com.carelink.model.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MyApplication extends Application {
	private static MyApplication instance = null;

	public static MyApplication getInstance() {
		return instance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		
		DrugDatabase.init(getApplicationContext());
		InsulinDatabase.init(getApplicationContext()); 
		RingtoneDatabase.init(getApplicationContext()); 
		
		LocalConfig.init(getApplicationContext());
	}

	public User currentUser = null;

	public static SharedPreferences getUserPreferences(String name, int mode) {
		return getInstance().getSharedPreferences(name, mode);
	}
	
	public static SharedPreferences getUserPreferences(String name) {
		return getUserPreferences(name, MODE_PRIVATE);
	}

	/**
	 * The variable is used to save the date selected by user in Calendar Panel 
	 * in MainActivity(RecordMgmtFragment actually). It is always same as 
	 * MainActivity.calendar. Be careful! <b>NOT</b> to change its value elsewhere 
	 * except in MainActivity.java
	 */
	public Calendar calendar = Calendar.getInstance();
	

	/**
	 * If this variable is not null, then it holds the only getInstance of MainActivity
	 * alive in this Application
	 */
	public MainActivity instanceOfMainActivity = null;
	
	
	/**
	 * This method MUST not be called when there is no user logged in ! 
	 * <br/> <br/>
	 * Uses LocalConfig in its body, and LocalConfig is set to USER_MODE.
	 */
	public static void callAlarmScheduleService(Context context) {
		if (context != null) {
			Intent alarmServiceIntent = new Intent(context, AlarmServiceBroadcastReceiver.class);
			alarmServiceIntent.putExtra(UIConstants.EXTRA_NAME_UID, LocalConfig.getCurrentUid());
			context.sendBroadcast(alarmServiceIntent, null);
		} else {
			Log.d("MyApplication", "callAlarmScheduledService() : context is null");
		}
	}
	
	public static void showMessageBox(Activity activityContext, int messageResId) {
		showMessageBox(activityContext, getInstance().getString(messageResId), null);
	}
	
	public static void showMessageBox(Activity activityContext, int messageResId, OnDismissListener onDismissListener) {
		showMessageBox(activityContext, getInstance().getString(messageResId), onDismissListener);
	}
	
	public static void showMessageBox(Activity activityContext, String message) {
		showMessageBox(activityContext, message, null);
	}
	
	public static void showMessageBox(Activity activityContext, String message, OnDismissListener onDismissListener) {
		final AlertDialog dialog = new AlertDialog.Builder(activityContext).create();
		dialog.show();
		Window window = dialog.getWindow();
		window.setContentView(R.layout.alert_dialog_p);
		TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
		messageTextView.setText(message);
		window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		if (onDismissListener != null) {
			dialog.setOnDismissListener(onDismissListener);
		}
	}
	
	/**
	 * Show a standard toast that contain a text view, the duration is Toast.LENGTH_SHORT
	 */
	public static void toast_s(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Show a standard toast that contain a text view, the duration is Toast.LENGTH_LONG
	 */
	public static void toast_l(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static String getVersionName() {
		Context context = getInstance().getApplicationContext();
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean isNetworkAvailable() {
		Context context = getInstance().getApplicationContext();
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}
	
	public static boolean isWifiConnected() {
		Context context = getInstance().getApplicationContext();
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo != null) {
			return wifiNetworkInfo.isConnected();
		}
		return false;
	}
	
	public static boolean isInternetConnected() {
		if (isWifiConnected()) {
			Context context = getInstance().getApplicationContext();
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			SupplicantState state = wifiInfo.getSupplicantState();
			if (state == SupplicantState.COMPLETED) {
				return true;
			} 
			return false;
		} 
		return isNetworkAvailable();
	}
}
