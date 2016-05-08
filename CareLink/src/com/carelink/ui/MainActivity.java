package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.carelink.R;
import com.carelink.database.RecordDatabase;
import com.carelink.database.ReminderDatabase;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.SignInService;
import com.carelink.interaction.services.UpdateHealthProfileService;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.interaction.services.UploadRecordsService;
import com.carelink.model.HealthProfile;
import com.carelink.model.Record;
import com.carelink.model.Reminder;
import com.carelink.ui.CalendarGridAdapter.OnSelectionListener;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

@SuppressLint({ "CommitTransaction", "SimpleDateFormat" }) 
public class MainActivity extends MyActivity {

	private final String ACTIVITY_TAG = "MainActivity";

	private TextView dateTextView;

	private PopupWindow popupCalendar = null;
	private PopupWindow auxWindow = null;

	private Calendar calendar;
	private ArrayList<CalendarGridItem> calendarGridItems = new ArrayList<CalendarGridItem>();;
	private CalendarGridAdapter calendarGridAdapter = null; 
	private TextView yearTextView, monthTextView;
	private boolean isShowCalendar = false;
	private ImageView navigationImageView = null;
	private Button okButton = null;

	private Calendar todayCal; 

	private OnDateSetListener onDateSetListener;

	private boolean isFirstFragment = true;

	private Fragment recordsMgmtFragment = null;
	private Fragment reminderFragment = null;
	private Fragment statisticsFragment = null;
	private Fragment percenterFragment = null;
	private Fragment curFragment;

	private DateChangedBroadcastReceiver dateChangedBroadcastReceiver = null;

	private CloseActivityBroadcastReceiver closeActivityBroadcastReceiver = null;

	private ConnectivityChangeBroadcastReceiver connectivityChangeBroadcastReceiver = null;

	private Vibrator vibrator;
	private MediaPlayer mediaPlayer;

	@SuppressLint("SimpleDateFormat") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_date);
		setContentViewResource(R.layout.activity_main);
		super.onCreate(savedInstanceState);

		// Initialize Databases of The Current User
		RecordDatabase.init(getApplicationContext(), LocalConfig.getCurrentUid());
		ReminderDatabase.init(getApplicationContext(), LocalConfig.getCurrentUid());

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			Reminder reminder = (Reminder) bundle.getSerializable(UIConstants.EXTRA_NAME_REMINDER);
			if (reminder != null) {
				showReminderAlarmDialog(reminder);

				// Closing All Other MainActivity Instances
				Intent closeMainActivityIntent = new Intent(UIConstants.ACTION_CLOSE_MAIN_ACTIVITY);
				MyApplication.getInstance().instanceOfMainActivity = this;
				sendBroadcast(closeMainActivityIntent);
			}
		}

		calendar = Calendar.getInstance();
		calendar.setTime(Utils.getStartOfDay(calendar.getTime()));
		todayCal = Calendar.getInstance();
		todayCal.setTime(Utils.getStartOfDay(todayCal.getTime()));

		MyApplication.getInstance().calendar.setTime(calendar.getTime());
		initPopupCalendar();

		navigationImageView = (ImageView) findViewById(R.id.imageView_navigation);
		findViewById(R.id.view_titleBar).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
				if (isShowCalendar == false) {
					refreshCalendarView();
					auxWindow.showAsDropDown(findViewById(R.id.view_titleBar), -auxWindow.getHeight(), 0);
					popupCalendar.showAsDropDown(findViewById(R.id.view_titleBar), -popupCalendar.getHeight(), 0);
					isShowCalendar = true;
					navigationImageView.setImageResource(R.drawable.up_triangle);
				} else {
					popupCalendar.dismiss();
				}
			}
		});
		dateTextView = (TextView) findViewById(R.id.textView_date);
		dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(todayCal.getTime()));


		findViewById(R.id.view_today).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				todayCal.setTime(Utils.getStartOfDay(Calendar.getInstance().getTime()));

				dateTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(todayCal.getTime()));

				calendar.setTime(todayCal.getTime());
				MyApplication.getInstance().calendar.setTime(calendar.getTime());

				refreshCalendarView();

				if (onDateSetListener != null) {
					Log.d(ACTIVITY_TAG, "Back to today : " + todayCal.getTime());
					onDateSetListener.onDateSet(todayCal);
				}
			}
		});

		recordsMgmtFragment = new RecordsMgmtFragment();
		reminderFragment = new RemindersMgmtFragment();
		statisticsFragment = new StatisticsFragment();
		percenterFragment = new PercenterFragment();

		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, recordsMgmtFragment);
		fragmentTransaction.add(R.id.fragment_container, reminderFragment);
		fragmentTransaction.add(R.id.fragment_container, statisticsFragment);
		fragmentTransaction.add(R.id.fragment_container, percenterFragment);
		fragmentTransaction.hide(reminderFragment);
		fragmentTransaction.hide(statisticsFragment);
		fragmentTransaction.hide(percenterFragment);
		fragmentTransaction.commit();
		curFragment = recordsMgmtFragment;

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup_tabBar);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Fragment fragment = null;
				switch(checkedId){
				case R.id.tab_button_calendar:
					//fragment = new CalendarFragment();
					if (!isFirstFragment) {
						TextView titleTextView = (TextView) findViewById(R.id.textView_title);
						if (titleTextView != null) {
							titleTextView.setText("");
						}
					}
					fragment = recordsMgmtFragment;
					findViewById(R.id.view_titleBar).setVisibility(View.VISIBLE);
					isFirstFragment = true;
					break;
				case R.id.tab_button_reminder:
					fragment = reminderFragment;
					if (isFirstFragment) {
						findViewById(R.id.view_titleBar).setVisibility(View.GONE);
						getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_default);
						isFirstFragment = false;
					}
					((TextView) findViewById(R.id.textView_title)).setText(R.string.title_reminder_management);
					break;
				case R.id.tab_button_statistics:
					fragment = statisticsFragment;
					if (isFirstFragment) {
						findViewById(R.id.view_titleBar).setVisibility(View.GONE);
						getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_default);
						isFirstFragment = false;
					}
					((TextView) findViewById(R.id.textView_title)).setText(R.string.title_statistics);
					break;
				case R.id.tab_button_percenter:
					fragment = percenterFragment;
					if (isFirstFragment) {
						findViewById(R.id.view_titleBar).setVisibility(View.GONE);
						getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_default);
						isFirstFragment = false;
					}
					((TextView) findViewById(R.id.textView_title)).setText(R.string.title_percenter);
					break;
				} 
				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					fragmentTransaction.hide(curFragment);
					fragmentTransaction.show(fragment);
					fragmentTransaction.commit();
					curFragment = fragment;
				}
			}
		});

		setOnHomePressedListener(new OnHomePressedListener() {
			@Override
			public void onHomePressed() {
			}
		});
		setOnHomeLongPressedListener(new OnHomeLongPressedListener() {
			@Override
			public void onHomeLongPressed() {
			}
		});

		IntentFilter intentFilter = new IntentFilter(UIConstants.ACTION_DATE_CHANGED);
		dateChangedBroadcastReceiver = new DateChangedBroadcastReceiver();
		registerReceiver(dateChangedBroadcastReceiver, intentFilter);

		closeActivityBroadcastReceiver = new CloseActivityBroadcastReceiver();
		registerReceiver(closeActivityBroadcastReceiver, new IntentFilter(UIConstants.ACTION_CLOSE_MAIN_ACTIVITY));

		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		//intentFilter2.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
		connectivityChangeBroadcastReceiver = new ConnectivityChangeBroadcastReceiver();
		registerReceiver(connectivityChangeBroadcastReceiver, intentFilter2);
	}


	@SuppressLint("InflateParams") @SuppressWarnings("deprecation")
	private void initPopupCalendar() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View aux_layout = layoutInflater.inflate(R.layout.calender_window_outside, null);
		auxWindow = new PopupWindow(aux_layout, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		auxWindow.setBackgroundDrawable(new BitmapDrawable());
		auxWindow.setAnimationStyle(R.style.PopupWindowAnim2);
		aux_layout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (popupCalendar != null) {
					popupCalendar.dismiss();
				}
			}
		});

		View calendarView = layoutInflater.inflate(R.layout.popup_window_calendar, null);
		popupCalendar = new PopupWindow(calendarView, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		popupCalendar.setBackgroundDrawable(new BitmapDrawable());
		//popupCalendar.setAnimationStyle(R.style.PopupWindowAnim);
		popupCalendar.setOnDismissListener(new OnDismissListener() {
			public void onDismiss() {
				auxWindow.dismiss();
				isShowCalendar = false;
				navigationImageView.setImageResource(R.drawable.down_triangle);
				okButton.setEnabled(false);

				calendarGridAdapter.setSelection(-1);
				calendarGridAdapter.notifyDataSetChanged();
			}
		});

		yearTextView = (TextView) calendarView.findViewById(R.id.textView_year);
		monthTextView = (TextView) calendarView.findViewById(R.id.textView_month);

		calendarGridAdapter = new CalendarGridAdapter(getApplicationContext(), calendarGridItems);
		GridView calendarGridView = (GridView) calendarView.findViewById(R.id.gridView_calendar);
		calendarGridView.setAdapter(calendarGridAdapter);
		calendarGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int tag = calendarGridItems.get(arg2).getTag();
				if (tag == CalendarGridItem.CURR_MONTH || tag == CalendarGridItem.TODAY) {
					calendarGridAdapter.setSelection(arg2);
					calendarGridAdapter.notifyDataSetChanged();
				}
			}
		});

		refreshCalendarView();

		calendarView.findViewById(R.id.imageView_prev).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (calendar.get(Calendar.MONTH) == 0) {
					calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-1);
				} 
				calendar.roll(Calendar.MONTH, false);
				refreshCalendarView();

				okButton.setEnabled(false);
			}
		});
		calendarView.findViewById(R.id.imageView_next).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (calendar.get(Calendar.MONTH) == 11) {
					calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
				} 
				calendar.roll(Calendar.MONTH, true);
				refreshCalendarView();

				okButton.setEnabled(false);
			}
		});
		okButton = (Button) calendarView.findViewById(R.id.button_ok);
		okButton.setEnabled(false);
		okButton.setOnClickListener(new OnClickListener() {
			@SuppressLint("SimpleDateFormat") 
			public void onClick(View v) {
				int selection = calendarGridAdapter.getSelection();
				if (selection != -1) {
					int date = calendarGridItems.get(selection).getDate();

					calendar.set(Calendar.DATE, date);
					MyApplication.getInstance().calendar.setTime(calendar.getTime());

					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
					popupCalendar.dismiss();

					if (onDateSetListener != null) {
						onDateSetListener.onDateSet(calendar);
					}
				}
			}
		});

		calendarGridAdapter.setOnSelectionListener(new OnSelectionListener() {
			public void onSelection() {
				okButton.setEnabled(true);
			}
		}); 
	}

	@SuppressLint("UseValueOf") 
	private void refreshCalendarView() {
		yearTextView.setText(new Integer(calendar.get(Calendar.YEAR)).toString());
		monthTextView.setText(new Integer(calendar.get(Calendar.MONTH) + 1).toString());
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DATE, 1);
		int firstWeekday = calendar.get(Calendar.DAY_OF_WEEK);
		firstWeekday -= 1; 
		if (firstWeekday == 0) firstWeekday = 7;
		calendar.roll(Calendar.MONTH, false);
		int daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.roll(Calendar.MONTH, true);		
		calendarGridItems.clear();
		for (int i = 1; i < firstWeekday; i++) {
			calendarGridItems.add(new CalendarGridItem(daysInPrevMonth - firstWeekday + i + 1, CalendarGridItem.PREV_MONTH));
		}

		todayCal = Calendar.getInstance();
		todayCal.setTime(Utils.getStartOfDay(todayCal.getTime()));
		for (int i = 1; i <= daysInMonth; i++) {
			if (calendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) 
					&& calendar.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
					&& i == todayCal.get(Calendar.DATE)) {
				calendarGridItems.add(new CalendarGridItem(i, CalendarGridItem.TODAY));
			} else {
				calendarGridItems.add(new CalendarGridItem(i, CalendarGridItem.CURR_MONTH));
			}
		}
		int padding = (firstWeekday + daysInMonth - 1) % 7;
		if (padding != 0) padding = 7 - padding;
		for (int i = 1; i <= padding; i++) {
			calendarGridItems.add(new CalendarGridItem(i, CalendarGridItem.NEXT_MONTH));
		}

		calendarGridAdapter.setSelection(-1);
		calendarGridAdapter.notifyDataSetChanged();
	}


	public interface OnDateSetListener {
		public void onDateSet(Calendar calendar);
	}

	public final Calendar getCalendar() {
		return calendar;
	}

	@Override
	public void onBackPressed() {
		if (isShowCalendar) {
			isShowCalendar = false;
			if (popupCalendar != null) {
				popupCalendar.dismiss();
			}
		} else {
			//super.onBackPressed();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
		this.onDateSetListener = onDateSetListener;
	}


	private class DateChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_DATE_CHANGED)) {
				Calendar calendar = (Calendar) intent.getSerializableExtra(UIConstants.EXTRA_NAME_CALENDAR);
				if (calendar != null) {
					MainActivity.this.calendar.setTime(calendar.getTime());
					MyApplication.getInstance().calendar.setTime(calendar.getTime());
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
					refreshCalendarView();

					if (onDateSetListener != null) {
						onDateSetListener.onDateSet(calendar);
					}
				} else {
					//System.out.println("Record is null");
				}
			}
		}
	}

	private void startAlarm(String ringtonePath) {
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		long[] pattern = { 1000, 200, 200, 200 };
		vibrator.vibrate(pattern, 0);
		mediaPlayer = new MediaPlayer();
		try {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
			int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
			float volume = curVolume * 1.0f / maxVolume;
			mediaPlayer.setVolume(volume, volume);
			mediaPlayer.setDataSource(this,
					Uri.parse(ringtonePath));
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setLooping(true);
			mediaPlayer.prepare();
			mediaPlayer.start();

		} catch (Exception e) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}

	private void stopAlarm() {
		if (vibrator != null) {
			vibrator.cancel();
		} 
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	} 

	private void showReminderAlarmDialog(Reminder reminder) {
		startAlarm(reminder.getRingtonePath());

		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		dialog.setCancelable(false);

		Window window = dialog.getWindow();
		if (reminder.getType() == Reminder.TYPE_TAKE_DRUGS || reminder.getType() == Reminder.TYPE_INSULIN) {
			window.setContentView(R.layout.dialog_reminder_alarm_drug);
		} else {
			window.setContentView(R.layout.dialog_reminder_alarm);
		}
		TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
		switch (reminder.getType()) {
		case Reminder.TYPE_MEASURE_GLUCOSE:
			messageTextView.setText(R.string.message_reminder_glucose);
			break;
		case Reminder.TYPE_MEASURE_HEART_PARAMS:
			messageTextView.setText(R.string.message_reminder_heart_params);
			break;
		case Reminder.TYPE_TAKE_DRUGS:
			messageTextView.setText(R.string.message_reminder_drugs);
			break;
		case Reminder.TYPE_INSULIN:
			messageTextView.setText(R.string.message_reminder_insulin);
			break;
		default:
			messageTextView.setText("Error: Invalid Reminder Type");
			break;
		}

		if (reminder.getType() == Reminder.TYPE_TAKE_DRUGS || reminder.getType() == Reminder.TYPE_INSULIN) {
			TextView detailsTextView = (TextView) window.findViewById(R.id.textView_details);
			detailsTextView.setText(reminder.getDescription());
		}

		final Reminder reminder2 = reminder;
		window.findViewById(R.id.button_record).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Class<?> cls = null;
				switch (reminder2.getType()) {
				case Reminder.TYPE_MEASURE_GLUCOSE:
					cls = RecordGlucoseActivity.class;
					break;
				case Reminder.TYPE_MEASURE_HEART_PARAMS:
					cls = RecordHeartParamActivity.class;
					break;
				case Reminder.TYPE_TAKE_DRUGS:
					cls = RecordDrugsActivity.class;
					break;
				case Reminder.TYPE_INSULIN:
					cls = RecordInsulinActivity.class;
					break;
				default:
					break;
				}
				if (cls != null) {
					Intent intent = new Intent(getApplicationContext(), cls);
					intent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder2);
					intent.putExtra(UIConstants.EXTRA_NAME_CALENDAR, Calendar.getInstance());
					startActivity(intent);
				}
				dialog.dismiss();
			}
		});
		window.findViewById(R.id.button_close).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				stopAlarm();
			}
		});
	}

	private class CloseActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_CLOSE_MAIN_ACTIVITY)) {
				Log.d(ACTIVITY_TAG, "Closing Activity");
				if (MainActivity.this != MyApplication.getInstance().instanceOfMainActivity) {
					finish();
				}
			}
		}
	}

	private static boolean syncServerLock = true;
	private class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(getClass().getSimpleName(), "OnReceive() : action " + intent.getAction() + ", lock " + syncServerLock);

			if (syncServerLock) {
				syncServerLock = false;
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isAvailable()) {
					Log.d(getClass().getSimpleName(), "Network Available: True");
					Log.d(getClass().getSimpleName(), "Internet Access: " + MyApplication.isInternetConnected());

					Calendar calendar = Calendar.getInstance();
					Date syncServerDate = LocalConfig.getSyncServerDate();
					Log.d(ACTIVITY_TAG, "Sync Server Date: " + syncServerDate);
					if (syncServerDate == null || !Utils.isSameDay(syncServerDate, calendar.getTime())) {
						if (MyApplication.isInternetConnected()) {
							syncServer();
						} else {
							SystemClock.sleep(5*60*1000); // wait 5min
							if (MyApplication.isInternetConnected()) {
								syncServer();
							} else {
								syncServerLock = true;
							}
						}
					} else {
						syncServerLock = true;
					}
				} else {
					syncServerLock = true;
				}
			}
			
			Log.d(getClass().getSimpleName(), "OnReceive() : exit");
		}
	}

	private void syncServer() {
		Log.d(ACTIVITY_TAG, "Sync Server");
		
		if (!Services.getInstance(getApplicationContext()).isOnLine()) {
			LocalConfig.setToGlblMode();
			Services.getInstance(getApplicationContext()).sendRequest(
					new SignInService().setParams(LocalConfig.getPhoneLastLoggedIn(), LocalConfig.getPasswordLastLoggedIn()), 
					new RequestCallback() {
						public void onSuccess(Response response) {
							Log.d(ACTIVITY_TAG, "sign in succeed");
							int uid = (Integer)response.getData();
							LocalConfig.setToUserMode(uid);
							uploadData();
						}
						public void onFailed(Message msg) {
							Log.d(ACTIVITY_TAG, "sign in failed " + msg);
							LocalConfig.setToUserMode(LocalConfig.getUidLastLoggedIn());
						}
					});
		} else {
			uploadData();
		}
	}
	
	private void uploadData() {
		Log.d(ACTIVITY_TAG, "uploading data ...");
		
		RecordDatabase.clean();
		ArrayList<Record> records = RecordDatabase.getAllRecords();
		Record[] recArray = new Record[records.size()];
		records.toArray(recArray);
		Services.getInstance(getApplicationContext()).sendRequest(
				new UploadRecordsService().setParams(recArray, RecordDatabase.UPPER_LIMIT_IN_DAYS), 
				new RequestCallback() {
					@Override
					public void onSuccess(Response response) {
						Log.d(ACTIVITY_TAG, "upload records succeed");
						LocalConfig.setSyncServerDate(Calendar.getInstance().getTime());
						syncServerLock = true;
					}

					@Override
					public void onFailed(Message msg) {
						Log.d(ACTIVITY_TAG, "upload records failed " + msg);
						syncServerLock = true;
					}
				});

		HealthProfile profile = LocalConfig.getHealthProfile();
		Services.getInstance(getApplicationContext()).sendRequest(
				new UpdateHealthProfileService().setParams(profile), new RequestCallback() {
					public void onSuccess(Response response) {
						Log.d(ACTIVITY_TAG, "upload health profile succeed");
					}
					public void onFailed(Message msg) {
						Log.d(ACTIVITY_TAG, "upload health profile filed " + msg);
					}
				});
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(dateChangedBroadcastReceiver);
		unregisterReceiver(closeActivityBroadcastReceiver);
		unregisterReceiver(connectivityChangeBroadcastReceiver);
		super.onDestroy();
	}
}
