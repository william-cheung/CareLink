package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.carelink.R;
import com.carelink.charts.model.Axis;
import com.carelink.charts.model.AxisValue;
import com.carelink.charts.model.Line;
import com.carelink.charts.model.LineChartData;
import com.carelink.charts.model.PointValue;
import com.carelink.charts.model.Viewport;
import com.carelink.charts.view.LineChartView;
import com.carelink.charts.view.LineChartView.LineChartOnValueTouchListener;
import com.carelink.database.RecordDatabase;
import com.carelink.model.Record;
import com.carelink.model.SportRecord;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat") 
public class SportsStatisticsFragment extends Fragment {

	private TextView dropDownMenuButton;
	private int dropDownMenuItemIndex = 0;
	private TextView startDateTextView, endDateTextView;
	private Calendar startDate;
	private Calendar lineChartStartDate;

	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd");

	private int[] daysOffset = {6, 13, 29, 59};
	//private int[] nAxisValues = {7, 7, 6, 6};

	private TextView averageTextView;

	private ArrayList<Record> sportsRecords;
	private ArrayList<SportRecord> recordsForDisplay;

	private LineChartView lineChartView;
	private LineChartData lineChartData;
	private int lineColor;

	private RecordsChangedBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		receiver = new RecordsChangedBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(UIConstants.ACTION_RECORDS_CHANGED);
		getActivity().registerReceiver(receiver, intentFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_statistics_sports, container, false);

		dropDownMenuButton = (TextView) view.findViewById(R.id.dropDownMenu);
		dropDownMenuButton.setText(R.string.one_week);
		dropDownMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDropDownMenu();
			}
		});


		startDateTextView = (TextView) view.findViewById(R.id.textView_startDate);
		endDateTextView = (TextView) view.findViewById(R.id.textView_endDate);

		averageTextView = (TextView) view.findViewById(R.id.textView_average);

		lineChartView = (LineChartView) view.findViewById(R.id.lineChartView);
		lineColor = getActivity().getResources().getColor(R.color.line_chart_color);

		sportsRecords = getAllLocalSportsRecord();
		recordsForDisplay = new ArrayList<SportRecord>();

		onDataSetChanged();

		return view;
	}

	private ArrayList<Record> getAllLocalSportsRecord() {
		return RecordDatabase.getRecords(Record.TYPE_SPORTS);
	}
	
	private void onDataSetChanged() {
		startDate = calcStartDate(-daysOffset[dropDownMenuItemIndex]);
		startDateTextView.setText(simpleDateFormat.format(startDate.getTime()));
		endDateTextView.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));
		
		generateRecordsForDisplay();
		calcAndDiaplayAverage();
		generateLineChartData(dropDownMenuItemIndex);
		lineChartView.setLineChartData(lineChartData);
	}

	@SuppressLint("InflateParams") @SuppressWarnings("deprecation")
	private void showDropDownMenu() {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View view = layoutInflater.inflate(R.layout.drop_down_menu2, null);
		final PopupWindow popupWindow = new PopupWindow(view, 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(dropDownMenuButton, 0, 10);

		final ArrayList<View> menuItems = new ArrayList<View>();
		menuItems.add(view.findViewById(R.id.menuItem1));
		menuItems.add(view.findViewById(R.id.menuItem2));
		menuItems.add(view.findViewById(R.id.menuItem3));
		//menuItems.add(view.findViewById(R.id.menuItem4));
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < menuItems.size(); i++) {
					TextView item = (TextView)menuItems.get(i);
					if (v.getId() == item.getId()) {
						dropDownMenuButton.setText(item.getText());
						dropDownMenuItemIndex = i;
						
						onDataSetChanged();
						
						popupWindow.dismiss();
					}
				}
			}
		};
		for (int i = 0; i < menuItems.size(); i++) {
			menuItems.get(i).setOnClickListener(listener);
		}
	}

	private Calendar calcStartDate(int offset) {
		Calendar ret = Calendar.getInstance();
		//com.carelink.util.Utils.roll(ret, offset);
		ret.add(Calendar.DATE, offset);
		ret.setTime(Utils.getStartOfDay(ret.getTime()));
		return ret;
	}

	private void generateRecordsForDisplay() {
		recordsForDisplay.clear();
		//System.out.println(glucoseRecords.size());
		for (int i = 0; i < sportsRecords.size(); i++) {
			SportRecord record = (SportRecord) sportsRecords.get(i);
			if (com.carelink.util.Utils.before(startDate.getTime(), record.getDate())) {
				recordsForDisplay.add(record);
				//System.out.println(record.getDate());
			}
		}
	}

	private void calcAndDiaplayAverage() {
		if (recordsForDisplay.size() == 0) {
			averageTextView.setText(R.string.value_place_holder);
		} else {
			float average = calcAverage();
			averageTextView.setText(String.format("%.1f", average));
		}
	}

	private float calcAverage() {
		float sum = 0.0f;
		for (int i = 0; i < recordsForDisplay.size(); i++) {
			sum += recordsForDisplay.get(i).getDuration();
		}
		return sum / recordsForDisplay.size();
	}

	private void generateLineChartData(int index) {
		lineChartStartDate = Calendar.getInstance();
		if (recordsForDisplay.size() == 0) {
			lineChartStartDate.setTime(startDate.getTime());
		} else {
			lineChartStartDate.setTime(
					Utils.getStartOfDay(recordsForDisplay.get(0).getDate()));
		}

		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.setTime(Utils.getStartOfDay(tomorrow.getTime()));
		int diffDays =  (int) ((Utils.diffInMillis(tomorrow.getTime(), lineChartStartDate.getTime())) / 86400000L);
		int daysInterval, nAxisValues;
		if (diffDays < 7) {
			daysInterval = 1;
			nAxisValues = diffDays;
		} else {
			daysInterval = (int) Math.ceil(diffDays / 7.0);
			nAxisValues = (int) Math.ceil(diffDays / (daysInterval + 0.0));
		}

		List<AxisValue> axisValues = new ArrayList<AxisValue>();
		//int daysInterval = daysOffset[index] / nAxisValues[index];
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(lineChartStartDate.getTime());
		for (int i = 0; i < nAxisValues + 1; i++) {
			axisValues.add(new AxisValue(i, 
					simpleDateFormat.format(calendar.getTime()).toCharArray()));
			com.carelink.util.Utils.roll(calendar, (int)daysInterval);
		}


		List<PointValue> values = new ArrayList<PointValue>();
		long timeUnit = daysInterval * 86400000L; 
		for (int i = 0; i < recordsForDisplay.size(); i++) {
			SportRecord record = recordsForDisplay.get(i);
			long diff = com.carelink.util.Utils.diffInMillis(record.getDate(), lineChartStartDate.getTime());
			float x = (float)((double)diff / (double) timeUnit); 
			values.add(new PointValue(x, record.getDuration()));
		}

		Line line = new Line(values);
		line.setColor(lineColor).setCubic(false);
		if (recordsForDisplay.size() >= 30) {
			line.setHasLines(false);
		}

		List<Line> lines = new ArrayList<Line>();
		lines.add(line);

		lineChartData = new LineChartData(lines);
		lineChartData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
		lineChartData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

		lineChartView.setViewportCalculationEnabled(false);
		Viewport v = new Viewport(0, 120.0f, nAxisValues, 0);
		lineChartView.setMaximumViewport(v);
		lineChartView.setCurrentViewport(v, false);
		
		lineChartView.setOnValueTouchListener(new LineChartOnValueTouchListener() {
			public void onValueTouched(int selectedLine, int selectedValue,
					PointValue value) {
				SportRecord record = recordsForDisplay.get(selectedValue);
				String dateString = 
						new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(record.getDate());
				MyApplication.toast_s(getActivity(), 
						"" + record.getDuration() + "  |  " + dateString 
						+ "  |  " + getString(UIConstants.SPORTS[record.getSportIndex()][1]));
			}
			public void onNothingTouched() {
			}
		});
	}

	private class RecordsChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_RECORDS_CHANGED)) {
				//Log.d(FRAGMENT_TAG, "BroadcastReceiver.onReceive()");
				int recordType = intent.getIntExtra(UIConstants.EXTRA_NAME_RECORD_TYPE, -1);
				if (recordType == Record.TYPE_SPORTS || recordType == -1) {
					sportsRecords = getAllLocalSportsRecord();
					
					onDataSetChanged();
				}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}
}
