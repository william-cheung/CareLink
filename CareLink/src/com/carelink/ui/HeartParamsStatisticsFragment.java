package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.carelink.R;
import com.carelink.charts.model.ArcValue;
import com.carelink.charts.model.Axis;
import com.carelink.charts.model.AxisValue;
import com.carelink.charts.model.Line;
import com.carelink.charts.model.LineChartData;
import com.carelink.charts.model.PieChartData;
import com.carelink.charts.model.PointValue;
import com.carelink.charts.model.ValueShape;
import com.carelink.charts.model.Viewport;
import com.carelink.charts.view.LineChartView;
import com.carelink.charts.view.LineChartView.LineChartOnValueTouchListener;
import com.carelink.charts.view.PieChartView;
import com.carelink.database.RecordDatabase;
import com.carelink.model.HeartParamsRecord;
import com.carelink.model.Record;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

@SuppressLint({ "SimpleDateFormat", "UseValueOf" }) 
public class HeartParamsStatisticsFragment extends Fragment {

	private static final String FRAGMENT_TAG = "HeartParamsStatisticsFragment";

	private TextView dropDownMenuButton;
	private int dropDownMenuItemIndex = 0;
	private TextView startDateTextView, endDateTextView;
	private Calendar startDate;
	private Calendar lineChartStartDate;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd");

	private int[] daysOffset = {6, 13, 29, 59};
	//private int[] nAxisValues = {7, 7, 6, 6};

	private static final float[] lowThreshold = {90, 60, 60};
	private static final float[] highThreshold = {140, 90, 100};
	private static final float[] maxPossibleValue = {160, 120, 120};
	private float averageSystolic 	= 0.0f;
	private float averageDiastolic 	= 0.0f;
	private float averageHeartRate 	= 0.0f;

	private int nAbove, nBelow, nInRange;

	private ArrayList<Record> heartParamsRecords;
	private ArrayList<Integer[]> recordsForDisplay;
	private ArrayList<Date> recordDates;

	private TextView averageSystolicTextView;
	private TextView averageDiastolicTextView;
	private TextView averageHeartRateTextView;
	private TextView inRangeTextView;
	private TextView aboveTextView;
	private TextView belowTextView;

	private LineChartView lineChartView;
	private LineChartData lineChartData;
	private int lineColor;
	private LineChartView lineChartView2;
	private LineChartData lineChartData2;
	private PieChartView pieChartView;
	private PieChartData pieChartData;

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
		View view = inflater.inflate(R.layout.fragment_statistics_heart_params, container, false);

		dropDownMenuButton = (TextView) view.findViewById(R.id.dropDownMenu);
		dropDownMenuButton.setText(R.string.one_week);
		dropDownMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDropDownMenu();
			}
		});

		startDateTextView = (TextView) view.findViewById(R.id.textView_startDate);
		endDateTextView = (TextView) view.findViewById(R.id.textView_endDate);

		averageSystolicTextView = (TextView) view.findViewById(R.id.textView_averageSystolic);
		averageDiastolicTextView = (TextView) view.findViewById(R.id.textView_averageDiastolic);
		averageHeartRateTextView = (TextView) view.findViewById(R.id.textView_averageHeartRate);
		inRangeTextView = (TextView) view.findViewById(R.id.textView_inRange);
		aboveTextView = (TextView) view.findViewById(R.id.textView_above);
		belowTextView = (TextView) view.findViewById(R.id.textView_below);

		pieChartView = (PieChartView) view.findViewById(R.id.pieChartView);
		pieChartView.setInteractive(false);
		lineChartView = (LineChartView) view.findViewById(R.id.lineChartView_bloodPressure);
		lineChartView2 = (LineChartView) view.findViewById(R.id.lineChartView_heartRate);
		lineColor = getActivity().getResources().getColor(R.color.line_chart_color);

		heartParamsRecords = getAllLocalHeartParamsRecords();
		recordsForDisplay = new ArrayList<Integer[]>();
		recordDates = new ArrayList<Date>();

		onDataSetChanged();

		return view;
	}

	private ArrayList<Record> getAllLocalHeartParamsRecords() {
		return RecordDatabase.getRecords(Record.TYPE_HEART_PARAMS);
	}
	
	private void onDataSetChanged() {
		startDate = calcStartDate(-daysOffset[dropDownMenuItemIndex]);
		startDateTextView.setText(simpleDateFormat.format(startDate.getTime()));
		endDateTextView.setText(simpleDateFormat.format(Calendar.getInstance().getTime()));
		
		generateRecordsForDisplay();
		generatePieChartData();
		pieChartView.setPieChartData(pieChartData);
		generateLineChartData1(dropDownMenuItemIndex);
		lineChartView.setLineChartData(lineChartData);
		lineChartView2.setLineChartData(lineChartData2);
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
		ret.add(Calendar.DATE, offset);
		ret.setTime(Utils.getStartOfDay(ret.getTime()));
		return ret;
	}

	private void generateRecordsForDisplay() {
		recordsForDisplay.clear();
		recordDates.clear();
		System.out.println(heartParamsRecords.size());
		for (int i = 0; i < heartParamsRecords.size(); i++) {
			HeartParamsRecord record = (HeartParamsRecord) heartParamsRecords.get(i);
			if (com.carelink.util.Utils.before(startDate.getTime(), record.getDate())) {
				Integer[] values = new Integer[3];
				values[0] = record.getSystolic();
				values[1] = record.getDiastolic();
				values[2] = record.getHeartRate();
				recordsForDisplay.add(values);
				recordDates.add(record.getDate());
			}
		}
	}

	private void generatePieChartData() {
		if (recordsForDisplay.size() == 0) {
			List<ArcValue> values = new ArrayList<ArcValue>();
			values.add(new ArcValue(60, getActivity().getResources().getColor(R.color.light_gray)));
			values.add(new ArcValue(30, getActivity().getResources().getColor(R.color.light_gray)));
			values.add(new ArcValue(10,getActivity().getResources().getColor(R.color.light_gray)));
			pieChartData = new PieChartData(values);
			pieChartData.setHasCenterCircle(true);
			pieChartData.setCenterCircleScale(0.5f);

			averageSystolicTextView.setText(R.string.value_place_holder);
			averageDiastolicTextView.setText(R.string.value_place_holder);
			averageHeartRateTextView.setText(R.string.value_place_holder);
			inRangeTextView.setText(R.string.value_place_holder);
			aboveTextView.setText(R.string.value_place_holder);
			belowTextView.setText(R.string.value_place_holder);

			return;
		}

		nAbove = nBelow = nInRange = 0;
		float[] sums = {0.0f, 0.0f, 0.0f};
		for (int i = 0; i < recordsForDisplay.size(); i++) {
			Integer[] values = recordsForDisplay.get(i);
			for (int j = 0; j < 3; j++) {
				sums[j] += values[j];
			}
			if (values[0] > highThreshold[0] || values[1] > highThreshold[1]) nAbove++;
			else if (values[0] < lowThreshold[0] || values[1] < lowThreshold[1]) nBelow++;
			else nInRange++;
		}
		averageSystolic  = sums[0] / recordsForDisplay.size();
		averageDiastolic = sums[1] / recordsForDisplay.size();
		averageHeartRate = sums[2] / recordsForDisplay.size();

		averageSystolicTextView.setText(String.format("%.1f", averageSystolic));
		averageDiastolicTextView.setText(String.format("%.1f", averageDiastolic));
		averageHeartRateTextView.setText(String.format("%.1f", averageHeartRate));
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

	private void generateLineChartData1(int index) {
		lineChartStartDate = Calendar.getInstance();
		if (recordsForDisplay.size() == 0) {
			lineChartStartDate.setTime(startDate.getTime());
		} else {
			lineChartStartDate.setTime(Utils.getStartOfDay(recordDates.get(0)));
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

		//int daysInterval = daysOffset[index] / nAxisValues[index];
		List<AxisValue> axisValues = new ArrayList<AxisValue>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(lineChartStartDate.getTime());
		for (int i = 0; i < nAxisValues + 1; i++) {
			axisValues.add(new AxisValue(i, 
					simpleDateFormat.format(calendar.getTime()).toCharArray()));
			com.carelink.util.Utils.roll(calendar, (int)daysInterval);
		}

		List<PointValue> systolicValues = new ArrayList<PointValue>();
		List<PointValue> diastolicValues = new ArrayList<PointValue>();
		List<PointValue> heartRateValues = new ArrayList<PointValue>();

		long timeUnit = daysInterval * 86400000L; 
		for (int i = 0; i < recordsForDisplay.size(); i++) {
			Integer[] values = recordsForDisplay.get(i);
			long diff = com.carelink.util.Utils.diffInMillis(recordDates.get(i), lineChartStartDate.getTime());
			float x = (float)((double)diff / (double) timeUnit); 
			systolicValues.add(new PointValue(x, values[0]));
			diastolicValues.add(new PointValue(x, values[1]));
			heartRateValues.add(new PointValue(x, values[2]));
		}

		Line line1 = new Line(systolicValues);
		line1.setColor(lineColor).setCubic(false);
		line1.setShape(ValueShape.SQUARE);
		if (recordsForDisplay.size() >= 30) {
			line1.setHasLines(false);
		}
		Line line2 = new Line(diastolicValues);
		line2.setColor(lineColor).setCubic(false);
		if (recordsForDisplay.size() >= 30) {
			line2.setHasLines(false);
		}

		List<Line> lines = new ArrayList<Line>();
		lines.add(line1);
		lines.add(line2);

		lineChartData = new LineChartData(lines);
		lineChartData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
		lineChartData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

		lineChartView.setViewportCalculationEnabled(false);
		Viewport v = new Viewport(0, maxPossibleValue[0], nAxisValues, 0);
		lineChartView.setMaximumViewport(v);
		lineChartView.setCurrentViewport(v, false);
		lineChartView.setOnValueTouchListener(new LineChartOnValueTouchListener() {
			public void onValueTouched(int selectedLine, int selectedValue,
					PointValue value) {
				String dateString = 
						new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(recordDates.get(selectedValue));
				if (selectedLine == 0) {
					MyApplication.toast_s(getActivity(), 
							"" + recordsForDisplay.get(selectedValue)[0] + "  |  " + dateString);
				} else if (selectedLine == 1) {
					MyApplication.toast_s(getActivity(), 
							"" + recordsForDisplay.get(selectedValue)[1] + "  |  " + dateString);
				}
			}
			@Override
			public void onNothingTouched() {
			}
		});

		Line line3 = new Line(heartRateValues);
		line3.setColor(lineColor).setCubic(false);
		if (recordsForDisplay.size() >= 30) {
			line3.setHasLines(false);
		}

		List<Line> lines2 = new ArrayList<Line>();
		lines2.add(line3);
		lineChartData2 = new LineChartData(lines2);
		lineChartData2.setAxisXBottom(new Axis(axisValues).setHasLines(true));
		lineChartData2.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

		lineChartView2.setViewportCalculationEnabled(false);
		Viewport v2 = new Viewport(0, maxPossibleValue[2], nAxisValues, 0);
		lineChartView2.setMaximumViewport(v2);
		lineChartView2.setCurrentViewport(v2, false);

		lineChartView2.setOnValueTouchListener(new LineChartOnValueTouchListener() {
			public void onValueTouched(int selectedLine, int selectedValue,
					PointValue value) {
				String dateString = 
						new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(recordDates.get(selectedValue));
				MyApplication.toast_s(getActivity(), 
						"" + recordsForDisplay.get(selectedValue)[2] + "  |  " + dateString);
			}
			public void onNothingTouched() {
			}
		});
	}

	private class RecordsChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(UIConstants.ACTION_RECORDS_CHANGED)) {
				Log.d(FRAGMENT_TAG, "records changed");
				int recordType = intent.getIntExtra(UIConstants.EXTRA_NAME_RECORD_TYPE, -1);
				if (recordType == Record.TYPE_HEART_PARAMS || recordType == -1) {
					heartParamsRecords = getAllLocalHeartParamsRecords();
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
