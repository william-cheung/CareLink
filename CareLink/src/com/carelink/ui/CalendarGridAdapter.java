package com.carelink.ui;

import java.util.ArrayList;

import com.carelink.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalendarGridAdapter extends BaseAdapter {	
	private Context context;
	private ArrayList<CalendarGridItem> calendarGridItems;
	private int selectedPosition = -1;
	private OnSelectionListener onSelectionListener;

	public CalendarGridAdapter(Context context,
			ArrayList<CalendarGridItem> calendarGridItems) {
		super();
		this.context = context;
		this.calendarGridItems = calendarGridItems;
	}

	@Override
	public int getCount() {
		return calendarGridItems.size();
	}

	@Override
	public Object getItem(int position) {
		return calendarGridItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({ "InflateParams", "UseValueOf" }) @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)
					context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.calendar_grid_item, null);
		}
		CalendarGridItem calendarGridItem = calendarGridItems.get(position);
		TextView dateTextView = (TextView) convertView.findViewById(R.id.textView_date);
		int date = calendarGridItem.getDate();
		dateTextView.setText(new Integer(date).toString());	
		if (position == selectedPosition) {
			dateTextView.setBackgroundResource(R.drawable.date_selected_bg);
			dateTextView.setTextColor(context.getResources().getColor(R.color.white));
		} else {
			dateTextView.setBackgroundResource(R.color.default_background);
			int tag = calendarGridItem.getTag();
			if (tag == CalendarGridItem.TODAY) {
				dateTextView.setBackgroundResource(R.drawable.today_bg);
				dateTextView.setTextColor(context.getResources().getColor(R.color.white));
			} else if (tag == CalendarGridItem.CURR_MONTH) {
				dateTextView.setTextColor(context.getResources().getColor(R.color.dark_gray));
			} else {
				dateTextView.setTextColor(context.getResources().getColor(R.color.light_gray));
			}
		}
		final int pos = position;
		convertView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int tag = calendarGridItems.get(pos).getTag();
				if (tag == CalendarGridItem.CURR_MONTH || tag == CalendarGridItem.TODAY) {
					setSelection(pos);
					notifyDataSetChanged();
					onSelectionListener.onSelection();
				}
			}
		});
		return convertView;
	}

	public void setSelection(int position) {
		selectedPosition = position;
	}
	
	public int getSelection() {
		return selectedPosition;
	}
	
	public void setOnSelectionListener(OnSelectionListener listener) {
		onSelectionListener = listener;
	}
	
	public interface OnSelectionListener {
		public void onSelection();
	}
}


