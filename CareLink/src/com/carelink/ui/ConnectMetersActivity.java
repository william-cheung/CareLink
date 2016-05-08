package com.carelink.ui;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.carelink.R;
import com.carelink.database.RecordDatabase;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.UploadRecordsService;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.model.GlucoseRecord;
import com.carelink.model.Record;
import com.carelink.util.JsonHelper;
import com.device.GlucoseMeter;

public class ConnectMetersActivity extends MyActivity { 
	private static final String DEBUG_TAG = "ConnectMetersActivity";

	private SyncMeterTask syncMeterTask = null; 
	private int meterType = -1;

	private SharedPreferences preferences = null;

	private Object[][] meters = {
			{GlucoseMeter.ONETOUCH_ULTRA, 		"OneTouch Ultra"		},
			{GlucoseMeter.ONETOUCH_ULTRAEASY,	"OneTouch UltraEasy"	},
			{GlucoseMeter.ONETOUCH_ULTRA2,		"OneTouch Ultra2"		},
			{GlucoseMeter.ONETOUCH_ULTRAMINI,	"OneTouch UltraMini"	},
			{GlucoseMeter.ONETOUCH_SELECT,		"OneTouch Select"		},
			{GlucoseMeter.FREESTYLE_FREEDOM,	"Freestyle Freedom"		},
	};
	private final int METER_TYPE_INDEX = 0;
	private final int METER_NAME_INDEX = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_connect_meters);
		setTitleTextResource(R.string.title_activity_connect_meters);
		super.onCreate(savedInstanceState);

		final TextView meterNameTextView = (TextView) findViewById(R.id.textView_meterName);
		preferences = MyApplication.getUserPreferences(UIConstants.PREFERENCES_NAME_SETTINGS);
		if (preferences != null) {
			meterType = preferences.getInt(UIConstants.SETTINGS_KEY_METER, -1);
			if (meterType != -1) {
				for (int i = 0; i < meters.length; i++) {
					if (meterType == (Integer)meters[i][METER_TYPE_INDEX]) {
						meterNameTextView.setText((String)meters[i][METER_NAME_INDEX]);
					}
				}
			}
		}

		findViewById(R.id.view_selectMeter).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//startActivity(new Intent(ConnectMetersActivity.this, SelectMeterActivity.class));
				final AlertDialog dialog = new AlertDialog.Builder(ConnectMetersActivity.this).create();
				dialog.show();

				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_list);

				final ArrayList<ListItem> listItems = new ArrayList<ListItem>();
				for (Object[] meter : meters) {
					listItems.add(new ListItem((String)meter[METER_NAME_INDEX]));
				}
				ListAdapter listAdapter = new ListAdapter(getApplicationContext(), listItems);
				ListView listView = (ListView) window.findViewById(R.id.listView);
				listView.setAdapter(listAdapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						meterNameTextView.setText(listItems.get(position).getMeterName());
						meterType = (Integer)meters[position][METER_TYPE_INDEX];

						if (preferences != null) {
							Editor editor = preferences.edit();
							editor.putInt(UIConstants.SETTINGS_KEY_METER, meterType);
							editor.commit();
						}

						dialog.dismiss();
					}
				});
			}
		});

		findViewById(R.id.view_syncMeter).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (meterType == -1) {
					final AlertDialog dialog = new AlertDialog.Builder(ConnectMetersActivity.this).create();
					dialog.show();
					Window window = dialog.getWindow();
					window.setContentView(R.layout.alert_dialog_p);
					TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
					messageTextView.setText(R.string.message_warnning_no_meter_selected);
					window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					return;
				}

				final AlertDialog dialog = new AlertDialog.Builder(ConnectMetersActivity.this).create();
				dialog.setCancelable(false);
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_sync_meter);
				final ProgressBar progressBar = (ProgressBar) window.findViewById(R.id.progressBar);
				final TextView statusTextView = (TextView) window.findViewById(R.id.textView_status);
				final View buttonsView = window.findViewById(R.id.view_buttons);
				buttonsView.setVisibility(View.GONE);
				final Button positiveButton = (Button) window.findViewById(R.id.button_positive);
				final View negativeButtonView = window.findViewById(R.id.view_negativeButton);
				final Button negativeButton = (Button) window.findViewById(R.id.button_negative);
				negativeButtonView.setVisibility(View.GONE);
				statusTextView.setText(R.string.status_enabling_bluetooth);
				syncMeterTask = new SyncMeterTask(ConnectMetersActivity.this, meterType, new SyncMeterCallBack() {
					@Override
					public void preReadData() {
						statusTextView.setText(R.string.status_reading_data);
					}

					@Override
					public void prePoweronMeter() {
					}

					@Override
					public void preEnableBluetooth() {
						statusTextView.setText(R.string.status_enabling_bluetooth);
					}

					@Override
					public void preConnectBluetooth() {
						//statusTextView.setText(R.string.status_connecting_meter);
						statusTextView.setText(R.string.status_connecting_blutooth_device);
					}

					@Override
					public void onReadDataSuccess(Record[] records) {
						progressBar.setVisibility(View.GONE);
						String statusFormat = getResources().getString(R.string.status_read_succesfully_format);
						String status = String.format(statusFormat, records.length);
						statusTextView.setText(status);
						buttonsView.setVisibility(View.VISIBLE);
						positiveButton.setText(R.string.operation_ok);
						final Record[] frecords = records;
						positiveButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if (frecords.length > 0) {
									statusTextView.setText(R.string.status_question_clear_data);
									positiveButton.setText(R.string.operation_yes);
									positiveButton.setOnClickListener(new OnClickListener() {
										public void onClick(View v) {
											if (syncMeterTask != null) {
												syncMeterTask.clearData();
												syncMeterTask.release();
											}
											dialog.dismiss();
										}
									});
									negativeButtonView.setVisibility(View.VISIBLE);
									negativeButton.setText(R.string.operation_no);
									negativeButton.setOnClickListener(new OnClickListener() {
										public void onClick(View v) {
											if (syncMeterTask != null) {
												syncMeterTask.release();
											}
											dialog.dismiss();
										}
									});
								} else {
									if (syncMeterTask != null) {
										syncMeterTask.release();
									}
									dialog.dismiss();
								}
							}
						});	
						
						if (records.length > 0) {
							saveRecords(records);
						}
					}

					@Override
					public void onReadDataFailed() {
						progressBar.setVisibility(View.GONE);
						statusTextView.setText(R.string.status_failed_to_read_data);
						buttonsView.setVisibility(View.VISIBLE);
						positiveButton.setText(R.string.operation_ok);
						positiveButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});	

					}

					@Override
					public void onPoweronMeterSuccess() {
					}

					@Override
					public void onPoweronMeterFailed() {
						progressBar.setVisibility(View.GONE);
						statusTextView.setText(R.string.status_unable_to_connect_meter);
						buttonsView.setVisibility(View.VISIBLE);
						positiveButton.setText(R.string.operation_ok);
						positiveButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});	
					}

					@Override
					public void onEnableBluetoothSuccess() {
					}

					@Override
					public void onEnableBluetoothFailed() {
						Log.d("SyncMeter", "onEnableBluetoothFailed");
						progressBar.setVisibility(View.GONE);
						statusTextView.setText(R.string.status_falied_to_enable_bluetooth);
						buttonsView.setVisibility(View.VISIBLE);
						positiveButton.setText(R.string.operation_ok);
						positiveButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});	
					}

					@Override
					public void onConnectBluetoothSuccess() {
					}

					@Override
					public void onConnectBluetoothFailed() {
						progressBar.setVisibility(View.GONE);
						//statusTextView.setText(R.string.status_unable_to_connect_meter);
						statusTextView.setText(R.string.status_failed_to_connect_blutooth_device);
						buttonsView.setVisibility(View.VISIBLE);
						positiveButton.setText(R.string.operation_ok);
						positiveButton.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});	
					}
				});
				syncMeterTask.execute();
			}
		});
	}

	private class ListItem {
		private String meterName;
		public ListItem(String meterName) {
			this.meterName = meterName;
		}
		public String getMeterName() {
			return meterName;
		}
	}

	private class ListAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<ListItem> items;

		public ListAdapter(Context context, ArrayList<ListItem> items) {
			super();
			this.context = context;
			this.items = items;
		}

		public int getCount() { return items.size(); }

		public Object getItem(int position) { return items.get(position); }

		public long getItemId(int position) { return position; }

		@SuppressLint("InflateParams") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.ringtone_list_item, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			ListItem item = items.get(position);
			textView.setText(item.getMeterName());
			return convertView;
		}
	}
	
	private void saveRecords(Record[] records) {
		ArrayList<Record> uploadingRecords = new ArrayList<Record>();
		for (Record record : records) {
			Log.d(DEBUG_TAG, JsonHelper.recordToJsonString(record));
			if (RecordDatabase.isRecordsInLocal(record.getDate())) {
				if (!checkDuplicate((GlucoseRecord) record)) {
					RecordDatabase.insert(record);
					uploadingRecords.add(record);
				}
			}
		}
		
		if (uploadingRecords.size() > 0) {
			uploadRecords(uploadingRecords);
			notifyRecordsChanged(Record.TYPE_GLUCOSE);
		}
	}
	
	private boolean checkDuplicate(GlucoseRecord record) {
		ArrayList<Record> records = RecordDatabase.getRecords(record.getDate());
		for (Record rec : records) {
			if (rec.getType() == Record.TYPE_GLUCOSE) {
				GlucoseRecord gRecord = (GlucoseRecord) rec;
				if (equalsGlucoseValue(record.getValue(), gRecord.getValue())) {
					if (isRoughlySameTime(record.getDate(), gRecord.getDate())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean equalsGlucoseValue(float v1, float v2) {
		int x1 = Math.round(v1 * 10);
		int x2 = Math.round(v2 * 10);
		if (x1 == x2) {
			return true;
		}
		return false;
	}
	
	private boolean isRoughlySameTime(Date date1, Date date2) {
		long timeLimitInMillis = 30 * 60 * 1000; // 30 minute
		if (Math.abs(date1.getTime() - date2.getTime()) < timeLimitInMillis) {
			return true;
		}
		return false;
	}
	
	private void uploadRecords(ArrayList<Record> recordList) {
		Record[] records = new Record[recordList.size()];
		for (int i = 0; i < recordList.size(); i++) {
			records[i] = recordList.get(i);
		}
		Services.getInstance(this).sendRequest(new UploadRecordsService().setParams(records), 
				new RequestCallback() {
			public void onSuccess(Response response) {
				Log.d(DEBUG_TAG, "UploadRecords Success!");
			}
			public void onFailed(Message msg) {
				Log.d(DEBUG_TAG, "UploadRecords Falied! " + msg.toString());
			}
		});
	}
	
	private void notifyRecordsChanged(int recordType) {
		 // Send Message to RecordsMgmtFragment
		Intent intent = new Intent(UIConstants.ACTION_ADD_RECORDS);
		sendBroadcast(intent);
		
		// Send Message to STATISTIC FRAGMENTS
		Intent intent2 = new Intent(UIConstants.ACTION_RECORDS_CHANGED);
		intent.putExtra(UIConstants.EXTRA_NAME_RECORD_TYPE, recordType);
		sendBroadcast(intent2);
	}
}
