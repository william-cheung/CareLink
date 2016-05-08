package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.database.ReminderDatabase;
import com.carelink.model.Reminder;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class RemindersMgmtFragment extends Fragment{

	public static final int REQUEST_ADD_REMINDER = 1;
	public static final int REQUEST_SHW_REMINDER = 2;

	private ArrayList<ListItem> listItems = new ArrayList<ListItem>();
	private ListAdapter listAdapter;
	private ListView listView;

	private int positionClicked = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MyApplication.callAlarmScheduleService(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_reminder, container, false);
		view.findViewById(R.id.button_addReminder).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ReminderActivity.class);
				startActivityForResult(intent, REQUEST_ADD_REMINDER);
				positionClicked = -1;
			}
		});

		listItems = new ArrayList<ListItem>();
		listAdapter = new ListAdapter(getActivity(), listItems);
		listView = (ListView) view.findViewById(R.id.listView_reminders);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Reminder reminder = listItems.get(position).getReminder();
				Intent intent = new Intent(getActivity(), ReminderActivity.class);
				positionClicked = position;
				intent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder);
				startActivityForResult(intent, REQUEST_SHW_REMINDER);
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.alert_dialog_pn);
				TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
				messageTextView.setText(R.string.message_warnning_remove_reminder);
				final int pos = position;
				window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Reminder reminder = listItems.get(pos).getReminder();
						listItems.remove(pos);
						listAdapter.notifyDataSetChanged();
						//MyApplication.getInstance().reminders.remove(pos);
						ReminderDatabase.delete(reminder.getId());
						onRemindersChanged();
						
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
		
		loadReminders();
		listAdapter.notifyDataSetChanged();

		return view;
	}

	private class ListItem {
		private Reminder reminder;
		public ListItem(Reminder reminder) {
			this.reminder = reminder;
		}
		public Reminder getReminder() {
			return reminder;
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

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "SimpleDateFormat", "InflateParams" }) @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.reminder_list_item, null);
			}
			TextView reminderTypeTextView = (TextView) convertView.findViewById(R.id.textView_reminderType);
			TextView reminderTimeTextView = (TextView) convertView.findViewById(R.id.textView_reminderTime);
			TextView weekdayTagsTextView = (TextView) convertView.findViewById(R.id.textView_weekdayTags);
			TextView dateCreatedTextView = (TextView) convertView.findViewById(R.id.textView_dateCreated);
			ListItem item = items.get(position);
			Reminder reminder = item.getReminder();
			switch (reminder.getType()) {
			case Reminder.TYPE_MEASURE_GLUCOSE:
				reminderTypeTextView.setText(R.string.text_glucose_reminder);
				break;
			case Reminder.TYPE_MEASURE_HEART_PARAMS:
				reminderTypeTextView.setText(R.string.text_heart_params_reminder);
				break;
			case Reminder.TYPE_TAKE_DRUGS:
				reminderTypeTextView.setText(R.string.text_drugs_reminder);
				break;
			case Reminder.TYPE_INSULIN:
				reminderTypeTextView.setText(R.string.text_insulin_reminder);
				break;
			default:;
			}
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
			calendar.set(Calendar.MINUTE, reminder.getMinute());
			reminderTimeTextView.setText(new SimpleDateFormat("hh:mm a").format(calendar.getTime()));

			weekdayTagsTextView.setText(translateWeekdayTags(reminder.getWeekdayTags()));
			dateCreatedTextView.setText(new SimpleDateFormat("yyyy-MM-dd").format(reminder.getDateCreated()));
			return convertView;
		}

	}

	private String translateWeekdayTags(String weekdayTags) {
		Resources resources = getActivity().getResources();
		ArrayList<Integer> weekdays = Utils.parseIntegerList(weekdayTags);
		if (weekdays.size() == 0) {
			return resources.getString(R.string.text_today);
		} else if (weekdays.size() == 7) {
			return resources.getString(R.string.text_every_day);
		} else {
			int[] weekdayStrResId = {
					R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday, R.string.friday,
					R.string.saturday, R.string.sunday,
			};
			String retString = resources.getString(weekdayStrResId[weekdays.get(0)]);
			for (int i = 1; i < weekdays.size(); i++) {
				retString += "¡¢" + resources.getString(weekdayStrResId[weekdays.get(i)]);
			}
			return retString;
		}
	}
	
	private void loadReminders() {
		ArrayList<Reminder> reminders = ReminderDatabase.getAllReminders();
		listItems.clear();
		for (Reminder reminder : reminders) {
			if (reminder.getWeekdayTags().equals("") && 
					!Utils.isSameDay(reminder.getDateCreated(), Calendar.getInstance().getTime())) {
				continue;
			}
			listItems.add(new ListItem(reminder));
		}
	}
	
	private void onRemindersChanged() {
		MyApplication.callAlarmScheduleService(getActivity());
		
		Intent intent = new Intent(UIConstants.ACTION_REMINDERS_CHANGED);
		getActivity().sendBroadcast(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			Reminder reminder = (Reminder) data.getSerializableExtra(UIConstants.EXTRA_NAME_REMINDER);
			if (reminder != null) {
				if (requestCode == REQUEST_ADD_REMINDER) {
					listItems.add(new ListItem(reminder));
					reminder.setId((int) ReminderDatabase.insert(reminder));
				} else if (requestCode == REQUEST_SHW_REMINDER){
					reminder.setId(listItems.get(positionClicked).getReminder().getId());
					listItems.remove(positionClicked);
					listItems.add(positionClicked, new ListItem(reminder));
					ReminderDatabase.update(reminder);
				}
				
				listAdapter.notifyDataSetChanged();
				onRemindersChanged();
			}
		}
	}
	

}
