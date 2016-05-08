package com.carelink.ui;

import java.util.ArrayList;

import com.carelink.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams") 
public class RecordOptionsActivity extends MyActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setTitleTextResource(R.string.title_activity_record_options);
		setContentViewResource(R.layout.activity_record_options);
		super.onCreate(savedInstanceState);

		ArrayList<RecordOptionsItem> recordOptionsItems = new ArrayList<RecordOptionsItem>();
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.glucose, R.string.text_record_option_glucose, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordGlucoseActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.heart_param, R.string.text_record_option_blood_pressure, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordHeartParamActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.drugs, R.string.text_record_option_drugs, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordDrugsActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.insulin, R.string.text_record_option_insulin, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordInsulinActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.weight, R.string.text_record_option_weight, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordWeightActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.sports, R.string.text_record_option_sports, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordSportsActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.discomfort, R.string.text_record_option_discomfort, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordDiscomfortActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		recordOptionsItems.add(new RecordOptionsItem(R.drawable.other_record, R.string.text_record_option_others, new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), RecordOthersActivity.class);
				startActivity(intent);
				finish();
			}
		}));
		ListView recordOptionsListView = (ListView) findViewById(R.id.listView_recordOptions);
		recordOptionsListView.setAdapter(new RecordOptionsListAdapter(getApplicationContext(), recordOptionsItems));
	}


	public class RecordOptionsItem {
		private int iconRid;
		private int textRid;
		private OnClickListener onClickListener;

		public RecordOptionsItem(int iconRid, int textRid, OnClickListener onClickListener) {
			this.iconRid = iconRid;
			this.textRid = textRid;
			this.onClickListener = onClickListener;
		}

		public int getIconRid() {
			return iconRid;
		}
		public int getTextRid() {
			return textRid;
		}

		public OnClickListener getOnClickListener() {
			return onClickListener;
		}	
	}

	private class RecordOptionsListAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<RecordOptionsItem> recordOptionsItems;

		public RecordOptionsListAdapter(Context context,
				ArrayList<RecordOptionsItem> recordOptionsItems) {
			super();
			this.context = context;
			this.recordOptionsItems = recordOptionsItems;
		}

		@Override
		public int getCount() {
			return recordOptionsItems.size();
		}

		@Override
		public Object getItem(int position) {
			if (position < getCount()) {
				return recordOptionsItems.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ViewHolder") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.record_options_item, null);
			}
			RecordOptionsItem recordOptionsItem = recordOptionsItems.get(position);
			ImageView optionIconImageView = (ImageView) convertView.findViewById(R.id.imageView_optionIcon);
			optionIconImageView.setImageResource(recordOptionsItem.getIconRid());
			TextView optionTextView = (TextView) convertView.findViewById(R.id.textView_optionText);
			optionTextView.setText(recordOptionsItem.getTextRid());
			convertView.setOnClickListener(recordOptionsItem.getOnClickListener());

			return convertView;
		}

	}
}
