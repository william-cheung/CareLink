package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.database.RecordDatabase;
import com.carelink.database.ReminderDatabase;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.DownloadRecordsService;
import com.carelink.interaction.services.SignInService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.model.Record;
import com.carelink.model.Reminder;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

@SuppressLint("SimpleDateFormat") 
public class RecordsMgmtFragment extends Fragment {	
	public static final String FRAGMENT_TAG = "RecordsMgmtFragment";
	private AddRecordsBroadcastReceiver addRecordsBroadcastReceiver;
	private RemindersChangedBroadcastReceiver remindersChangedBroadcastReceiver;

	private ArrayList<Record> records;
	private ArrayList<RecordListItem> recordListItems;
	private RecordListAdapter recordListAdapter;

	/**
	 * Used to indicate an existing record and the position of the record clicked
	 */
	private int positionClicked = -1;
	private int nTasks = 0;

	/**
	 * This calendar should always be set to "yyyy-MM-dd 12:00:00:000 AM"
	 */
	private Calendar calendar = Calendar.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addRecordsBroadcastReceiver = new AddRecordsBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UIConstants.ACTION_ADD_RECORDS);
		getActivity().registerReceiver(addRecordsBroadcastReceiver, intentFilter);

		remindersChangedBroadcastReceiver = new RemindersChangedBroadcastReceiver();
		IntentFilter intentFilter2 = new IntentFilter(UIConstants.ACTION_REMINDERS_CHANGED);
		getActivity().registerReceiver(remindersChangedBroadcastReceiver, intentFilter2);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_records_mgmt, container, false);

		records = new ArrayList<Record>();
		recordListItems = new ArrayList<RecordListItem>();
		//calendar.setTime(((MainActivity)getActivity()).getCalendar().getTime());\
		calendar.setTime(Utils.getStartOfDay(calendar.getTime()));
		Log.d(FRAGMENT_TAG, "onCreateView(): " + getFormatedDate(calendar));
		loadListItems(calendar);

		recordListAdapter = new RecordListAdapter(getActivity(), recordListItems);
		final ListView recordsListView = (ListView) view.findViewById(R.id.listView_records);
		recordsListView.setAdapter(recordListAdapter);
		recordsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				RecordListItem recordListItem = recordListItems.get(position);
				if (recordListItem.getItemType() == RecordListItem.LIST_ITEM_TYPE_RECORD) {
					Record record = ((NormalRecordListItem)recordListItem).getRecord();
					Intent intent = null;
					switch (record.getType()) {
					case Record.TYPE_GLUCOSE:
						intent = new Intent(getActivity(), RecordGlucoseActivity.class);
						break;
					case Record.TYPE_HEART_PARAMS:
						intent = new Intent(getActivity(), RecordHeartParamActivity.class);
						break;
					case Record.TYPE_INSULIN:
						intent = new Intent(getActivity(), RecordInsulinActivity.class);
						break;
					case Record.TYPE_DRUGS:
						intent = new Intent(getActivity(), RecordDrugsActivity.class);
						break;
					case Record.TYPE_SPORTS:
						intent = new Intent(getActivity(), RecordSportsActivity.class);
						break;
					case Record.TYPE_WEIGHT:
						intent = new Intent(getActivity(), RecordWeightActivity.class);
						break;
					case Record.TYPE_DISCOMFORT:
						intent = new Intent(getActivity(), RecordDiscomfortActivity.class);
						break;
					case Record.TYPE_OTHERS:
						intent = new Intent(getActivity(), RecordOthersActivity.class);
						break;
					default:
						break;
					}
					if (intent != null) {
						positionClicked = position;
						intent.putExtra(UIConstants.EXTRA_NAME_RECORD, record);
						//Log.d(FRAGMENT_TAG, JsonHelper.recordToJsonString(record));
						startActivity(intent);
					}
				}
			}
		});
		recordsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.alert_dialog_pn);
				TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
				messageTextView.setText(R.string.message_warnning_remove_record);
				final int pos = position;
				window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Record record = ((NormalRecordListItem)recordListItems.get(pos)).getRecord();
						RecordDatabase.delete(record);
						//deleteRemoteRecord(record);

						records.remove(pos - nTasks);
						recordListItems.remove(pos);
						recordListAdapter.notifyDataSetChanged();

						notifyRecordsChanged(record.getType());

						dialog.dismiss();
					}
				});
				window.findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				return false;
			}
		});

		((MainActivity)getActivity()).setOnDateSetListener(new MainActivity.OnDateSetListener() {
			@Override
			public void onDateSet(Calendar calendar) {
				RecordsMgmtFragment.this.calendar.setTime(calendar.getTime());
				loadListItems(calendar);
			}
		});

		view.findViewById(R.id.button_addRecord).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				positionClicked = -1;

				if (Utils.before(Calendar.getInstance().getTime(), calendar.getTime())) {
					MyApplication.showMessageBox(getActivity(), R.string.message_cannot_add_record_of_future);
					return;
				} else if (!RecordDatabase.isRecordsInLocal(calendar.getTime())) {
					String message = getString(R.string.message_cannot_add_record_long_time_ago_format);
					MyApplication.showMessageBox(getActivity(), String.format(message, RecordDatabase.UPPER_LIMIT_IN_DAYS));
					return;
				}

				startActivity(new Intent(getActivity(), RecordOptionsActivity.class));
			}
		});

		final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				Log.d(FRAGMENT_TAG, "onFling");
				final float SWIPE_MIN_XDIST = 120;
				final float SWIPE_MAX_YDIST = 80;
				if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_YDIST) {
					if (e1.getX() - e2.getX() > SWIPE_MIN_XDIST) {
						Log.d(FRAGMENT_TAG, "Fling left");
						calendar.add(Calendar.DATE, 1);
						notifyDateChanged();
						return true;
					} else if (e2.getX() - e1.getX() > SWIPE_MIN_XDIST) {
						Log.d(FRAGMENT_TAG, "Fling right");
						calendar.add(Calendar.DATE, -1);
						notifyDateChanged();
						return true;
					}
				}
				return false;
			}
		});

		view.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

		recordsListView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});

		return view;
	}

	private void loadListItems(Calendar calendar) {
		if (records != null) {
			records.clear();
		}
		recordListItems.clear();

		loadReminders(calendar);
		loadRecords(calendar);

		if (recordListAdapter != null) {
			recordListAdapter.notifyDataSetChanged();
		}
	}

	private void loadReminders(Calendar calendar) {
		nTasks = 0;
		ArrayList<Reminder> reminders = ReminderDatabase.getAllReminders();
		for (Reminder reminder : reminders) {
			//Log.d(FRAGMENT_TAG, JsonHelper.reminderToJsonString(reminder));
			boolean[] isWeekdaySelected = new boolean[7];	
			ArrayList<Integer> indices = Utils.parseIntegerList(reminder.getWeekdayTags());
			if (indices.size() == 0) {
				if (Utils.isSameDay(reminder.getDateCreated(), calendar.getTime())) {
					recordListItems.add(new TaskListItem(reminder));
					nTasks += 1;
				} 
			} else {
				for (Integer index : indices) {
					isWeekdaySelected[index] = true;
				}
				int weekday = calendar.get(Calendar.DAY_OF_WEEK);
				weekday = (weekday - 2 + 7) % 7;
				if (isWeekdaySelected[weekday] 
						&& (Utils.isSameDay(reminder.getDateCreated(), calendar.getTime()) 
								|| Utils.before(reminder.getDateCreated(), calendar.getTime()))
								&& (Utils.diffInDays(Utils.tomorrow(), calendar.getTime()) 
										<= UIConstants.MAX_DAYS_SHOW_TASK_STAUS)) {
					recordListItems.add(new TaskListItem(reminder));
					nTasks += 1;
				} 
			}
		}
	}

	private void loadRecords(Calendar calendar) {
		records = RecordDatabase.getRecords(calendar.getTime());
		//records = RecordDatabase.getAllRecords();
		if (records != null) {
			for (Record record : records) {
				recordListItems.add(new NormalRecordListItem(record));
				//Log.d(FRAGMENT_TAG, JsonHelper.recordToJsonString(record));
			}
		} else {
			Log.d(FRAGMENT_TAG, "Loading remote records ...");
			if (Services.getInstance(getActivity()).isOnLine()) {
				loadRemoteRecords();
			} else {
				LocalConfig.setToGlblMode();
				Services.getInstance(getActivity()).sendRequest(
						new SignInService().setParams(LocalConfig.getPhoneLastLoggedIn(), LocalConfig.getPasswordLastLoggedIn()), 
						new RequestCallback() {
							public void onSuccess(Response response) {
								Log.d(FRAGMENT_TAG, "sign in succeed");
								int uid = (Integer)response.getData();
								LocalConfig.setToUserMode(uid);
								loadRemoteRecords();
							}
							public void onFailed(Message msg) {
								Log.d(FRAGMENT_TAG, "sign in failed " + msg);
								LocalConfig.setToUserMode(LocalConfig.getUidLastLoggedIn());
								MyApplication.toast_s(getActivity(), getString(R.string.message_network_error));
							}
						});
			}
		}	
	}

	private void loadRemoteRecords() {
		Services.getInstance(getActivity()).sendRequest(
				new DownloadRecordsService().setParamsByDate(calendar.getTime()), 
				new RequestCallback() {
					public void onSuccess(Response response) {
						Log.d(FRAGMENT_TAG, "DownloadRecords Success!");
						Record[] records = (Record[]) response.getData();
						for (Record record : records) {
							recordListItems.add(new NormalRecordListItem(record));
						}
					}
					public void onFailed(Message msg) {
						Log.d(FRAGMENT_TAG, "DownloadRecords Failed! " + msg.toString());
						MyApplication.toast_s(getActivity(), getString(R.string.message_network_error));
					}
				});
	}

	private void notifyDateChanged() {
		Intent intent = new Intent(UIConstants.ACTION_DATE_CHANGED);
		intent.putExtra(UIConstants.EXTRA_NAME_CALENDAR, calendar);
		getActivity().sendBroadcast(intent);
	}

	private void notifyRecordsChanged(int recordType) {
		Intent intent = new Intent(UIConstants.ACTION_RECORDS_CHANGED);
		intent.putExtra(UIConstants.EXTRA_NAME_RECORD_TYPE, recordType);
		getActivity().sendBroadcast(intent);
	}

	private class AddRecordsBroadcastReceiver extends BroadcastReceiver {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_ADD_RECORDS)) {
				Record record = (Record) intent.getSerializableExtra(UIConstants.EXTRA_NAME_RECORD);
				if (record != null) {
					//boolean forAnExistingRecord = intent.getBooleanExtra(UIConstants.EXTRA_NAME_BOOLEAN, false);
					String oldGuid = "";
					if (positionClicked != -1 && positionClicked >= nTasks) {
						Record record2 = records.get(positionClicked - nTasks);
						oldGuid = record2.getGuid();
						Log.d(FRAGMENT_TAG, "notifyRecordsChanged 1, position Clicked : " + positionClicked);
						notifyRecordsChanged(record2.getType());
						records.remove(positionClicked - nTasks);
						recordListItems.remove(positionClicked);
						positionClicked = -1;
					}

					int i;
					for (i = nTasks; i < recordListItems.size(); i++) {
						if (Utils.before(record.getDate(), recordListItems.get(i).getDate())) {
							break;
						}
					}

					if (record.getId() != -1) {
						record.setGuid(oldGuid);
						RecordDatabase.update(record);
					} else {
						record.setId((int) RecordDatabase.insert(record));
					}
					//uploadRecordToRemote(record);

					records.add(i - nTasks, record);
					recordListItems.add(i, new NormalRecordListItem(record));
					recordListAdapter.notifyDataSetChanged();

					Log.d(FRAGMENT_TAG, "notifyRecordsChanged 2");
					notifyRecordsChanged(record.getType());
				} else {
					Log.d(FRAGMENT_TAG, "RecordsChanged: record == null" );
					// Broadcast From ConnectMetersActivity or SignInActivity
					loadListItems(calendar);
				}
			}
		}
	}

	//	private void uploadRecordToRemote(Record record) {
	//		Record[] records = new Record[1];
	//		records[0] = record;
	//		Services.getInstance(getActivity()).sendRequest(
	//				new UploadRecordsService().setParams(records), 
	//				new RequestCallback() {
	//					public void onSuccess(Response response) {
	//						Log.d(FRAGMENT_TAG, "UploadRecords Success!");
	//					}
	//					public void onFailed(Message msg) {
	//						Log.d(FRAGMENT_TAG, "UploadRecords Failed! " + msg.toString());
	//					}
	//				});
	//	}

	//	private void deleteRemoteRecord(Record record) {
	//		Record[] records = new Record[1];
	//		records[0] = record;
	//		Services.getInstance(getActivity()).sendRequest(
	//				new DeleteRecordsService().setParams(records), 
	//				new RequestCallback() {
	//					public void onSuccess(Response response) {
	//						Log.d(FRAGMENT_TAG, "DeleteRecords Success!");
	//					}
	//				
	//					@Override
	//					public void onFailed(Message msg) {
	//						Log.d(FRAGMENT_TAG, "DeleteRecords Failed! " + msg.toString());
	//					}
	//				});
	//	}

	private class RemindersChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_REMINDERS_CHANGED)) {
				Log.d(FRAGMENT_TAG, "reminders changed");
				for (int i = 0; i < nTasks; i++) {
					recordListItems.remove(0);
				}

				//				if (Utils.before(Calendar.getInstance().getTime(), calendar.getTime())) {
				//					return;
				//				}
				nTasks = 0;
				ArrayList<Reminder> reminders = ReminderDatabase.getAllReminders();
				for (Reminder reminder : reminders) {
					boolean[] isWeekdaySelected = new boolean[7];	
					ArrayList<Integer> indices = Utils.parseIntegerList(reminder.getWeekdayTags());
					if (indices.size() == 0) {
						if (Utils.isSameDay(reminder.getDateCreated(), calendar.getTime())) {
							recordListItems.add(nTasks, 
									new TaskListItem(reminder));
							nTasks += 1;
						}
					} else {
						for (Integer index : indices) {
							isWeekdaySelected[index] = true;
						}
						int weekday = calendar.get(Calendar.DAY_OF_WEEK);
						weekday = (weekday - 2 + 7) % 7;
						if (isWeekdaySelected[weekday] 
								&& (Utils.isSameDay(reminder.getDateCreated(), calendar.getTime()) 
										|| Utils.before(reminder.getDateCreated(), calendar.getTime()))
										&& (Utils.diffInDays(Utils.tomorrow(), calendar.getTime()) 
												<= UIConstants.MAX_DAYS_SHOW_TASK_STAUS)) {
							recordListItems.add(nTasks, 
									new TaskListItem(reminder));
							nTasks += 1;
						}
					}
				}
				recordListAdapter.notifyDataSetChanged();
			}
		}
	}

	private String getFormatedDate(Calendar calendar) {
		return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()); 
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(addRecordsBroadcastReceiver);
		getActivity().unregisterReceiver(remindersChangedBroadcastReceiver);
		super.onDestroy();
	}
}
