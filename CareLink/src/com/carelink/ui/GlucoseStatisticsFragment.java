package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.carelink.R;
import com.carelink.charts.model.ArcValue;
import com.carelink.charts.model.Axis;
import com.carelink.charts.model.AxisValue;
import com.carelink.charts.model.Line;
import com.carelink.charts.model.LineChartData;
import com.carelink.charts.model.PieChartData;
import com.carelink.charts.model.PointValue;
import com.carelink.charts.model.Viewport;
import com.carelink.charts.view.LineChartView;
import com.carelink.charts.view.PieChartView;
import com.carelink.database.RecordDatabase;
import com.carelink.model.GlucoseRecord;
import com.carelink.model.Record;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat") 
public class GlucoseStatisticsFragment extends Fragment {

	private static final String FRAGMENT_TAG = "GlucoseStatisticsFragment";

	private LineChartView lineChartView;
	private LineChartData lineChartData;
	private int lineColor;
	private PieChartView pieChartView;
	private PieChartData pieChartData;

	private TextView dropDownMenuButton1, dropDownMenuButton2;
	private int index1 = 0, index2 = 0;

	private TextView startDateTextView, endDateTextView;
	private Calendar startDate;
	private Calendar lineChartStartDate;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd");

	private int[] daysOffset = {7, 14, 30, 60};
	//private int[] nAxisValues = {7, 7, 6, 6};

	private static final float lowThreshold = 5.0f; // 3.9f;
	private static final float highThreshold = 10.0f; // 11.1f;
	private float average = 0.0f;
	private int nAbove, nBelow, nInRange;

	ArrayList<Record> glucoseRecords;
	ArrayList<GlucoseRecord> glucoseRecordsForDisplay;

	private TextView averageTextView;
	private TextView inRangeTextView;
	private TextView aboveTextView;
	private TextView belowTextView;

	private RecordsChangedBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lineColor = getActivity().getResources().getColor(R.color.line_chart_color);

		glucoseRecords = getAllLocalGlucoseRecords();
		glucoseRecordsForDisplay = new ArrayList<GlucoseRecord>();

		receiver = new RecordsChangedBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(UIConstants.ACTION_RECORDS_CHANGED);
		getActivity().registerReceiver(receiver, intentFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_statistics_glucose, container, false);

		dropDownMenuButton1 = (TextView) view.findViewById(R.id.dropDownMenu1);
		dropDownMenuButton1.setText(R.string.text_all);
		dropDownMenuButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDropDownMenu1();
			}
		});

		dropDownMenuButton2 = (TextView) view.findViewById(R.id.dropDownMenu2);
		dropDownMenuButton2.setText(R.string.one_week);
		dropDownMenuButton2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDropDownMenu2();
			}
		});

		startDateTextView = (TextView) view.findViewById(R.id.textView_startDate);
		//startDateTextView.setText(simpleDateFormat.format(startDate.getTime()));
		endDateTextView = (TextView) view.findViewById(R.id.textView_endDate);
		//endDateTextView.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));

		averageTextView = (TextView) view.findViewById(R.id.textView_average);
		inRangeTextView = (TextView) view.findViewById(R.id.textView_inRange);
		aboveTextView = (TextView) view.findViewById(R.id.textView_above);
		belowTextView = (TextView) view.findViewById(R.id.textView_below);

		pieChartView = (PieChartView) view.findViewById(R.id.pieChartView);
		pieChartView.setInteractive(false);
		lineChartView = (LineChartView) view.findViewById(R.id.lineChartView);

		onDataSetChanged();
		
		return view;
	}
	
	private ArrayList<Record> getAllLocalGlucoseRecords() {
		return RecordDatabase.getRecords(Record.TYPE_GLUCOSE);
	}
	
	private void onDataSetChanged() {
		startDate = calcStartDate(-(daysOffset[index2] - 1));
		startDateTextView.setText(simpleDateFormat.format(startDate.getTime()));
		endDateTextView.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));

		generateRecordsForDisplay(index1);
		generatePieChartData();
		pieChartView.setPieChartData(pieChartData);
		generateLineChartData(index2);
		lineChartView.setLineChartData(lineChartData);
	}

	@SuppressLint("InflateParams") @SuppressWarnings("deprecation")
	private void showDropDownMenu1() {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View view = layoutInflater.inflate(R.layout.drop_down_menu1, null);
		final PopupWindow popupWindow = new PopupWindow(view, 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(dropDownMenuButton1, 0, 10);

		final ArrayList<View> menuItems = new ArrayList<View>();
		menuItems.add(view.findViewById(R.id.menuItem1));
		menuItems.add(view.findViewById(R.id.menuItem2));
		menuItems.add(view.findViewById(R.id.menuItem3));
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < menuItems.size(); i++) {
					TextView item = (TextView)menuItems.get(i);
					if (v.getId() == item.getId()) {
						dropDownMenuButton1.setText(item.getText());
						index1 = i;
						
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

	@SuppressLint("InflateParams") @SuppressWarnings("deprecation")
	private void showDropDownMenu2() {
		LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		View view = layoutInflater.inflate(R.layout.drop_down_menu2, null);
		final PopupWindow popupWindow = new PopupWindow(view, 
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(dropDownMenuButton2, 0, 10);

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
						dropDownMenuButton2.setText(item.getText());
						index2 = i;
						
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

	private void generateRecordsForDisplay(int index) {
		glucoseRecordsForDisplay.clear();
		//System.out.println(glucoseRecords.size());
		for (int i = 0; i < glucoseRecords.size(); i++) {
			GlucoseRecord record = (GlucoseRecord) glucoseRecords.get(i);
			if (com.carelink.util.Utils.before(startDate.getTime(), record.getDate())) {
				if (index == 0) {
					glucoseRecordsForDisplay.add(record);
				} else if (index == 1 && beforeMeal(record)) {
					glucoseRecordsForDisplay.add(record);
				} else if (index == 2 && afterMeal(record)){
					glucoseRecordsForDisplay.add(record);
				}
			}
		}
	}

	private boolean beforeMeal(GlucoseRecord record) {
		int tag = record.getTag();
		return tag == GlucoseRecord.TAG_BEFORE_BREAKFAST 
				|| tag == GlucoseRecord.TAG_BEFORE_LUNCH 
				|| tag == GlucoseRecord.TAG_BEFORE_DINNER;
	}

	private boolean afterMeal(GlucoseRecord record) {
		int tag = record.getTag();
		return tag == GlucoseRecord.TAG_AFTER_BREAKFAST 
				|| tag == GlucoseRecord.TAG_AFTER_LUNCH 
				|| tag == GlucoseRecord.TAG_AFTER_DINNER;
	}

	private void generatePieChartData() {
		if (glucoseRecordsForDisplay.size() == 0) {
			List<ArcValue> values = new ArrayList<ArcValue>();
			values.add(new ArcValue(60, getActivity().getResources().getColor(R.color.light_gray)));
			values.add(new ArcValue(30, getActivity().getResources().getColor(R.color.light_gray)));
			values.add(new ArcValue(10,getActivity().getResources().getColor(R.color.light_gray)));
			pieChartData = new PieChartData(values);
			pieChartData.setHasCenterCircle(true);
			pieChartData.setCenterCircleScale(0.5f);

			averageTextView.setText(R.string.value_place_holder);
			inRangeTextView.setText(R.string.value_place_holder);
			aboveTextView.setText(R.string.value_place_holder);
			belowTextView.setText(R.string.value_place_holder);
			return;
		}

		nAbove = nBelow = nInRange = 0;
		float sum = 0.0f;
		for (int i = 0; i < glucoseRecordsForDisplay.size(); i++) {
			GlucoseRecord record = glucoseRecordsForDisplay.get(i);
			float value = record.getValue();
			if (value > highThreshold) nAbove++;
			else if (value < lowThreshold) nBelow++;
			else nInRange++;
			sum += value;
		}
		average = sum / glucoseRecordsForDisplay.size();

		//System.out.println("average:" + average + " size: " + glucoseRecordsForDisplay.size());

		averageTextView.setText(String.format("%.1f", average));
		inRangeTextView.setText(String.format("%d", nInRange));
		aboveTextView.setText(String.format("%d", nAbove));
		belowTextView.setText(String.format("%d", nBelow));

		List<ArcValue> values = new ArrayList<ArcValue>();
		values.add(new ArcValue(nInRange, getActivity().getResources().getColor(R.color.in_range)));
		values.add(new ArcValue(nAbove, getActivity().getResources().getColor(R.color.above)));
		values.add(new ArcValue(nBelow,getActivity().getResources().getColor(R.color.below)));
		pieChartData = new PieChartData(values);
		pieChartData.setHasCenterCircle(true);
		pieChartData.setCenterCircleScale(0.5f);
	}

	private void generateLineChartData(int index) {
		lineChartStartDate = Calendar.getInstance();
		if (glucoseRecordsForDisplay.size() == 0) {
			lineChartStartDate.setTime(startDate.getTime());
		} else {
			lineChartStartDate.setTime(
					Utils.getStartOfDay(glucoseRecordsForDisplay.get(0).getDate()));
		}

		List<AxisValue> axisValues = new ArrayList<AxisValue>();
		List<PointValue> values = new ArrayList<PointValue>();

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

		//int daysInterval = daysOffset[index] / nAxisValues[index];
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(lineChartStartDate.getTime());
		for (int i = 0; i < nAxisValues + 1; i++) {
			axisValues.add(new AxisValue(i, 
					simpleDateFormat.format(calendar.getTime()).toCharArray()));
			Log.d(FRAGMENT_TAG, calendar.getTime().toString());
			com.carelink.util.Utils.roll(calendar, (int)daysInterval);
		}

		long timeUnit = daysInterval * 86400000L; 
		for (int i = 0; i < glucoseRecordsForDisplay.size(); i++) {
			GlucoseRecord record = glucoseRecordsForDisplay.get(i);
			long diff = com.carelink.util.Utils.diffInMillis(record.getDate(), lineChartStartDate.getTime());
			float x = (float)((double)diff / (double) timeUnit); 
			values.add(new PointValue(x, record.getValue()));
		}

		Line line = new Line(values);
		line.setColor(lineColor).setCubic(false);
		//line.setShape(ValueShape.SQUARE);
		if (glucoseRecordsForDisplay.size() >= 30) {
			line.setHasLines(false);
		}

		List<Line> lines = new ArrayList<Line>();
		lines.add(line);

		lineChartData = new LineChartData(lines);
		lineChartData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
		lineChartData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

		lineChartView.setViewportCalculationEnabled(false);
		Viewport v = new Viewport(0, 15.0f, nAxisValues, 0);
		lineChartView.setMaximumViewport(v);
		lineChartView.setCurrentViewport(v, false);

		lineChartView.setOnValueTouchListener(new ValueTouchListener());
	}


	private class ValueTouchListener implements LineChartView.LineChartOnValueTouchListener {
		@SuppressLint("DefaultLocale") @Override
		public void onValueTouched(int selectedLine, int selectedValue, PointValue value) {
			GlucoseRecord record = glucoseRecordsForDisplay.get(selectedValue);

			String valueString = String.format("%.1f", record.getValue());

			String dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(record.getDate());

			int tagResId = -1;
			switch (record.getTag()) {
			case GlucoseRecord.TAG_AFTER_BREAKFAST:
				tagResId = R.string.text_glucose_tag4;
				break;
			case GlucoseRecord.TAG_AFTER_DINNER:
				tagResId = R.string.text_glucose_tag6;
				break;
			case GlucoseRecord.TAG_AFTER_LUNCH:
				tagResId = R.string.text_glucose_tag5;
				break;
			case GlucoseRecord.TAG_BEFORE_BREAKFAST:
				tagResId = R.string.text_glucose_tag0;
				break;
			case GlucoseRecord.TAG_BEFORE_DINNER:
				tagResId = R.string.text_glucose_tag2;
				break;
			case GlucoseRecord.TAG_BEFORE_LUNCH:
				tagResId = R.string.text_glucose_tag1;
				break;
			case GlucoseRecord.TAG_BEFORE_SLEEP:
				tagResId = R.string.text_glucose_tag3;
				break;
			case GlucoseRecord.TAG_RANDOM:
				tagResId = R.string.text_glucose_tag7;
				break;
			default:
				break;
			}
			String tagString = "";
			if (tagResId != -1) {
				tagString = getString(tagResId);
			}
			MyApplication.toast_s(getActivity(), valueString + "  |  " + dateString + "  |  " + tagString);
		}
		@Override
		public void onNothingTouched() {
		}
	}


	private class RecordsChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_RECORDS_CHANGED)) {
				Log.d(FRAGMENT_TAG, "records changed");
				int recordType = intent.getIntExtra(UIConstants.EXTRA_NAME_RECORD_TYPE, -1);
				if (recordType == Record.TYPE_GLUCOSE || recordType == -1) {
					glucoseRecords = getAllLocalGlucoseRecords();
					
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
