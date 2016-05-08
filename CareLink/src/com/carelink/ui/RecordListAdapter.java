package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.model.Record;
import com.carelink.model.Reminder;
import com.carelink.model.SportRecord;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("InflateParams") 
public class RecordListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<RecordListItem> recordListItems;

	/**
	 * @param context MUST be an Activity
	 * @param recordListItems
	 */
	public RecordListAdapter(Context context,
			ArrayList<RecordListItem> recordListItems) {
		super();
		this.context = context;
		this.recordListItems = recordListItems;
	}

	@Override
	public int getCount() {
		return recordListItems.size();
	}

	@Override
	public Object getItem(int position) {
		return recordListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("SimpleDateFormat") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		RecordListItem recordListItem = recordListItems.get(position);
		if (recordListItem.getItemType() == RecordListItem.LIST_ITEM_TYPE_DATE) {
			convertView = inflater.inflate(R.layout.record_list_item_date, null);
			DateRecordListItem dateItem = (DateRecordListItem) recordListItem;
			TextView dateTextView = (TextView) convertView.findViewById(R.id.textView_date);
			dateTextView.setText(dateItem.getDateString());
		} else if (recordListItem.getItemType() == RecordListItem.LIST_ITEM_TYPE_RECORD){
			convertView = inflater.inflate(R.layout.record_list_item, null);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_recordIcon);
			TextView textView = (TextView) convertView.findViewById(R.id.textView_recordType);
			TextView textView2 = (TextView) convertView.findViewById(R.id.textView_recordValue);
			TextView textView3 = (TextView) convertView.findViewById(R.id.textView_recordDate);
			//TextView textView4 = (TextView) convertView.findViewById(R.id.textView_feature);
			NormalRecordListItem recordItem = (NormalRecordListItem) recordListItem;
			Record record = recordItem.getRecord();
			switch (record.getType()) {
			case Record.TYPE_GLUCOSE:
				imageView.setImageResource(R.drawable.glucose);
				textView.setText(R.string.text_glucose_record);
				break;
			case Record.TYPE_HEART_PARAMS:
				imageView.setImageResource(R.drawable.heart_param);
				textView.setText(R.string.text_blood_pressure_record);
				break;
			case Record.TYPE_INSULIN:
				imageView.setImageResource(R.drawable.insulin);
				textView.setText(R.string.text_insulin_record);
				break;
			case Record.TYPE_DRUGS:
				imageView.setImageResource(R.drawable.drugs);
				textView.setText(R.string.text_drugs_record);
				break;
			case Record.TYPE_WEIGHT:
				imageView.setImageResource(R.drawable.weight);
				textView.setText(R.string.text_weight_record);
				break;
			case Record.TYPE_SPORTS:
				int sportIndex = ((SportRecord)(recordItem.getRecord())).getSportIndex();
				if (sportIndex != -1) {
					imageView.setImageResource(SportRecord.getSportIconResId(sportIndex));
				} else {
					imageView.setImageResource(R.drawable.sports);
				}
				textView.setText(R.string.text_sports_record);
				break;
			case Record.TYPE_DISCOMFORT:
				imageView.setImageResource(R.drawable.discomfort);
				textView.setText(R.string.text_discomfort_record);
				break;
			case Record.TYPE_OTHERS:
				imageView.setImageResource(R.drawable.other_record);
				textView.setText(R.string.text_other_record);
				break;
			default:
				break;
			}
			textView2.setText(recordItem.getDescription());
			textView3.setText(recordItem.getDateString());
		} else {
			convertView = inflater.inflate(R.layout.task_list_item, null);
			ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_taskIcon);
			TextView typeTextView = (TextView) convertView.findViewById(R.id.textView_taskType);
			TextView timeTextView = (TextView) convertView.findViewById(R.id.textView_taskTime);
			TaskListItem recordItem = (TaskListItem) recordListItem;
			switch (recordItem.getReminder().getType()) {
			case Reminder.TYPE_MEASURE_GLUCOSE:
				imageView.setImageResource(R.drawable.glucose);
				typeTextView.setText(R.string.text_glucose_record);
				break;
			case Reminder.TYPE_MEASURE_HEART_PARAMS:
				imageView.setImageResource(R.drawable.heart_param);
				typeTextView.setText(R.string.text_blood_pressure_record);
				break;
			case Reminder.TYPE_TAKE_DRUGS:
				imageView.setImageResource(R.drawable.drugs);
				typeTextView.setText(R.string.text_drugs_record);
				break;
			case Reminder.TYPE_INSULIN:
				imageView.setImageResource(R.drawable.insulin);
				typeTextView.setText(R.string.text_insulin_record);
				break;
			default:
				break;
			}
			final Calendar calendar = MyApplication.getInstance().calendar;
			Calendar calendar2 = Calendar.getInstance();
			calendar2.set(Calendar.HOUR_OF_DAY, recordItem.getReminder().getHourOfDay());
			calendar2.set(Calendar.MINUTE, recordItem.getReminder().getMinute());
			String timeString = new SimpleDateFormat("MM-dd").format(calendar.getTime());
			timeString += " " + (new SimpleDateFormat("hh:mm a").format(calendar2.getTime()));
			timeTextView.setText(timeString);

			final Reminder reminder = recordItem.getReminder();
			ImageView recordImageView = (ImageView) convertView.findViewById(R.id.imageButton_record);
			recordImageView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
					if (Utils.before(Calendar.getInstance().getTime(), calendar.getTime())) {
						MyApplication.showMessageBox((Activity)context, R.string.message_cannot_add_record_of_future);
						return;
					}
					
					Class<?> cls = null;
					switch (reminder.getType()) {
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
						Intent intent = new Intent(context, cls);
						intent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder);
						intent.putExtra(UIConstants.EXTRA_NAME_CALENDAR, MyApplication.getInstance().calendar);
						context.startActivity(intent);
					}
				}
			});

			if (reminder.isMarkedAsDone(calendar)) {
				recordImageView.setImageResource(R.drawable.status_done);
				recordImageView.setEnabled(false);
				convertView = inflater.inflate(R.layout.task_list_item_gone, null);
			}
		}
		return convertView;
	}

}
