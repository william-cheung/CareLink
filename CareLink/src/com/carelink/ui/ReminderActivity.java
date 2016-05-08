package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.carelink.R;
import com.carelink.database.RingtoneDatabase;
import com.carelink.model.Drug;
import com.carelink.model.GlucoseRecord;
import com.carelink.model.Insulin;
import com.carelink.model.Reminder;
import com.carelink.timepicker.RadialPickerLayout;
import com.carelink.timepicker.TimePickerDialog;
import com.carelink.timepicker.TimePickerDialog.OnTimeSetListener;
import com.carelink.util.Utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class ReminderActivity extends MyActivity implements OnTimeSetListener {

	public static final String ACTIVITY_TAG = "ReminderActivity";
	public static final String TIMEPICKER_TAG = "timepicker";

	private static final int REQUEST_DRUG 		= 1;
	private static final int REQUEST_INSULIN 	= 2;

	private int reminderType = -1;

	private Calendar reminderTime = Calendar.getInstance();
	private int snoozeTimeInterval = 10;
	private int snoozeTimeIntervalTemp = 10;
	private int snoozeTimes = 3;
	private int snoozeTimesTemp = 3;
	private String ringtonePath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
	private MediaPlayer mediaPlayer;
	private CountDownTimer ringtoneTimer;

	private String weekdayTags = "";

	private int glucoseTag = GlucoseRecord.TAG_RANDOM;
	
	
	private String drugName = "";
	private int dosage = -1;

	private TextView reminderTypeTextView = null;
	private TextView reminderTimeTextView = null;

	private TextView ringtoneNameTextView = null;
	
	private TextView glucoseTagTextView = null;
	private TextView drugNameTextView = null;
	private TextView dosageTextView = null;

	private View selectGlucoseTagView = null;
	private View selectDrugsView = null;
	private View editDosageView = null;
	private View blankSpaceView = null;

	@SuppressLint("SimpleDateFormat") @Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_reminder);
		setTitleTextResource(R.string.title_activity_add_reminder);
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			Reminder reminder = (Reminder) bundle.get(UIConstants.EXTRA_NAME_REMINDER);
			if (reminder != null) {
				reminderType = reminder.getType();
				reminderTime.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
				reminderTime.set(Calendar.MINUTE, reminder.getMinute());
				snoozeTimeInterval = reminder.getSnoozeInterval();
				snoozeTimes = reminder.getSnoozeTimes();
				weekdayTags = reminder.getWeekdayTags();
				ringtonePath = reminder.getRingtonePath();
				if (reminderType == Reminder.TYPE_MEASURE_GLUCOSE) {
					glucoseTag = reminder.getGlucoseTag();
				}
				if (reminderType == Reminder.TYPE_INSULIN || reminderType == Reminder.TYPE_TAKE_DRUGS) {
					drugName = reminder.getDrugName();
					dosage = reminder.getDosage();
				}
			}
		}

		selectGlucoseTagView = findViewById(R.id.view_selectGlucoseTag);
		selectDrugsView = findViewById(R.id.view_selectDrugs);
		editDosageView = findViewById(R.id.view_editDosage);
		blankSpaceView = findViewById(R.id.view_blankSpace);

		reminderTypeTextView = (TextView) findViewById(R.id.textView_reminderType);
		if (reminderType != -1) {
			switch (reminderType) {
			case Reminder.TYPE_MEASURE_GLUCOSE:
				reminderTypeTextView.setText(R.string.text_glucose_reminder);
				selectGlucoseTagView.setVisibility(View.VISIBLE);
				blankSpaceView.setVisibility(View.GONE);
				break;
			case Reminder.TYPE_MEASURE_HEART_PARAMS:
				reminderTypeTextView.setText(R.string.text_heart_params_reminder);
				break;
			case Reminder.TYPE_TAKE_DRUGS:
				reminderTypeTextView.setText(R.string.text_drugs_reminder);
				selectDrugsView.setVisibility(View.VISIBLE);
				editDosageView.setVisibility(View.VISIBLE);
				blankSpaceView.setVisibility(View.GONE);
				break;
			case Reminder.TYPE_INSULIN:
				reminderTypeTextView.setText(R.string.text_insulin_reminder);
				selectDrugsView.setVisibility(View.VISIBLE);
				editDosageView.setVisibility(View.VISIBLE);
				blankSpaceView.setVisibility(View.GONE);
				break;
			default:
				break;
			}
			TextView title = (TextView) findViewById(R.id.textView_title);
			if (title != null) {
				title.setText(getResources().getString(R.string.text_reminder_details));
			}
		}
		
		findViewById(R.id.view_selectReminderType).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_select_reminder_type);
				final ArrayList<View> menuItems = new ArrayList<View>();
				menuItems.add(window.findViewById(R.id.menuItem1));
				menuItems.add(window.findViewById(R.id.menuItem2));
				menuItems.add(window.findViewById(R.id.menuItem3));
				menuItems.add(window.findViewById(R.id.menuItem4));

				OnClickListener listener = new OnClickListener() {
					public void onClick(View v) {
						for (int i = 0; i < menuItems.size(); i++) {
							TextView item = (TextView)menuItems.get(i);
							if (v.getId() == item.getId()) {
								reminderTypeTextView.setText(item.getText());
								if (i == 0 || i == 1) {
									selectDrugsView.setVisibility(View.GONE);
									editDosageView.setVisibility(View.GONE);
									if (i == 0) {
										selectGlucoseTagView.setVisibility(View.VISIBLE);
										blankSpaceView.setVisibility(View.GONE);
									} else {
										blankSpaceView.setVisibility(View.VISIBLE);
									}
								} else {
									selectDrugsView.setVisibility(View.VISIBLE);
									editDosageView.setVisibility(View.VISIBLE);
									blankSpaceView.setVisibility(View.GONE);
								}

								if (i != 0) {
									selectGlucoseTagView.setVisibility(View.GONE);
								}

								switch (i) {
								case 0:
									reminderType = Reminder.TYPE_MEASURE_GLUCOSE;
									break;
								case 1:
									reminderType = Reminder.TYPE_MEASURE_HEART_PARAMS;
									break;
								case 2:
									reminderType = Reminder.TYPE_TAKE_DRUGS;
									break;
								case 3:
									reminderType = Reminder.TYPE_INSULIN;
									break;
								default:
									break;
								}
								dialog.dismiss();
							}
						}
					}
				};
				for (int i = 0; i < menuItems.size(); i++) {
					menuItems.get(i).setOnClickListener(listener);
				}
			}
		});
		
		
		glucoseTagTextView = (TextView) findViewById(R.id.textView_glucoseTag);
		final String[] glucoseTags = getResources().getStringArray(R.array.glucose_tags);
		String glucoseTagName = "";
		switch (glucoseTag) {
		case GlucoseRecord.TAG_RANDOM:				glucoseTagName = glucoseTags[0];	break;
		case GlucoseRecord.TAG_BEFORE_BREAKFAST:	glucoseTagName = glucoseTags[1];	break;
		case GlucoseRecord.TAG_AFTER_BREAKFAST:		glucoseTagName = glucoseTags[2];	break;
		case GlucoseRecord.TAG_BEFORE_LUNCH:		glucoseTagName = glucoseTags[3];	break;
		case GlucoseRecord.TAG_AFTER_LUNCH:			glucoseTagName = glucoseTags[4];	break;
		case GlucoseRecord.TAG_BEFORE_DINNER:		glucoseTagName = glucoseTags[5];	break;
		case GlucoseRecord.TAG_AFTER_DINNER:		glucoseTagName = glucoseTags[6];	break;
		case GlucoseRecord.TAG_BEFORE_SLEEP:		glucoseTagName = glucoseTags[7];	break;
		default:
			break;
		}
		if (glucoseTagTextView != null) {
			glucoseTagTextView.setText(glucoseTagName);
		}
		
		selectGlucoseTagView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
				dialog.show();

				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_list);

				final ArrayList<TextListItem> listItems = new ArrayList<TextListItem>();
				for (String string : glucoseTags) {
					listItems.add(new TextListItem(string));
				}
				TextListAdapter listAdapter = new TextListAdapter(getApplicationContext(), 
						listItems, R.layout.text_list_item);
				ListView listView = (ListView) window.findViewById(R.id.listView);
				listView.setAdapter(listAdapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						glucoseTagTextView.setText(listItems.get(position).getText());
						switch (position) {
						case 0:
							glucoseTag = GlucoseRecord.TAG_RANDOM;
							break;
						case 1:
							glucoseTag = GlucoseRecord.TAG_BEFORE_BREAKFAST;
							break;
						case 2:
							glucoseTag = GlucoseRecord.TAG_AFTER_BREAKFAST;
							break;
						case 3:
							glucoseTag = GlucoseRecord.TAG_BEFORE_LUNCH;
							break;
						case 4:
							glucoseTag = GlucoseRecord.TAG_AFTER_LUNCH;
							break;
						case 5:
							glucoseTag = GlucoseRecord.TAG_BEFORE_DINNER;
							break;
						case 6:
							glucoseTag = GlucoseRecord.TAG_AFTER_DINNER;
							break;
						case 7:
							glucoseTag = GlucoseRecord.TAG_BEFORE_SLEEP;
							break;
						default:
							break;
						}
						dialog.dismiss();
					}
				});
			}
		});
		

		drugNameTextView = (TextView) findViewById(R.id.textView_drugName);
		if (!drugName.equals("")) {
			drugNameTextView.setText(drugName);
		}
		
		findViewById(R.id.view_selectDrugs).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (reminderType == Reminder.TYPE_TAKE_DRUGS) {
					Intent intent = new Intent(getApplicationContext(), SelectDrugActivity.class);
					startActivityForResult(intent, REQUEST_DRUG);
				} else if (reminderType == Reminder.TYPE_INSULIN) {
					Intent intent = new Intent(getApplicationContext(), SelectInsulinActivity.class);
					startActivityForResult(intent, REQUEST_INSULIN);
				}
			}
		});
		
		dosageTextView = (TextView) findViewById(R.id.textView_dosage);
		if (dosage != -1) {
			dosageTextView.setText("" + dosage);
		}
		
		findViewById(R.id.view_editDosage).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView = (TextView) findViewById(R.id.textView_dosage);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_drug_dosage);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
				if (dosage != -1) {
					editText.setText(textView.getText());
					editText.setSelection(textView.getText().length());
				}
				final TextView textView3 = textView;
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String text = editText.getText().toString();
						if (!text.equals("")) {
							textView3.setText(text);
							dosage = Integer.parseInt(text);
						}
						dialog.dismiss();
					}
				});
			}
		});
			

		reminderTimeTextView = (TextView) findViewById(R.id.textView_reminderTime);
		reminderTimeTextView.setText(new SimpleDateFormat("hh:mm  a").format(reminderTime.getTime()));
		findViewById(R.id.view_selectReminderTime).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog();
			}
		});

		final ArrayList<TextView> weekdayTagTextViews = new ArrayList<TextView>();
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag1));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag2));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag3));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag4));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag5));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag6));
		weekdayTagTextViews.add((TextView) findViewById(R.id.textView_tag7));
		final boolean[] tagSelected = new boolean[weekdayTagTextViews.size()];	

		ArrayList<Integer> indices = Utils.parseIntegerList(weekdayTags);
		for (Integer index : indices) {
			tagSelected[index] = true;
			TextView tagTextView = weekdayTagTextViews.get(index);
			tagTextView.setBackgroundResource(R.drawable.tag_bg_selected);
			tagTextView.setTextColor(getResources().getColor(R.color.white));
		}

		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < weekdayTagTextViews.size(); i++) {
					TextView tag = weekdayTagTextViews.get(i);
					if (v.getId() == tag.getId()) {
						if (tagSelected[i]) {
							tagSelected[i] = false; 
							tag.setBackgroundResource(R.drawable.tag_bg_normal);
							tag.setTextColor(getResources().getColor(R.color.theme));
						} else {
							tagSelected[i] = true; 
							tag.setBackgroundResource(R.drawable.tag_bg_selected);
							tag.setTextColor(getResources().getColor(R.color.white));
						}
					}
				}
			}
		};
		for (TextView tag : weekdayTagTextViews) {
			tag.setOnClickListener(listener);
		}

		findViewById(R.id.view_snoozeDuration).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_snooze_duration);

				snoozeTimeIntervalTemp = snoozeTimeInterval;
				RadioGroup radioGroup1 = (RadioGroup) window.findViewById(R.id.radioGroup_snoozeInterval);
				switch(snoozeTimeInterval) {
				case 5: radioGroup1.check(R.id.radio_5min); break;
				case 10: radioGroup1.check(R.id.radio_10min); break;
				case 15: radioGroup1.check(R.id.radio_15min); break;
				default: break;
				}
				radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.radio_5min:
							snoozeTimeIntervalTemp = 5;
							break;
						case R.id.radio_10min:
							snoozeTimeIntervalTemp = 10;
							break;
						case R.id.radio_15min:
							snoozeTimeIntervalTemp = 15;
							break;
						default:
							break;
						}
					}
				});

				snoozeTimesTemp = snoozeTimes;
				RadioGroup radioGroup2 = (RadioGroup) window.findViewById(R.id.radioGroup_snoozeTimes);
				switch(snoozeTimes) {
				case 1: radioGroup2.check(R.id.radio_1time); break;
				case 3: radioGroup2.check(R.id.radio_3times); break;
				case 5: radioGroup2.check(R.id.radio_5times); break;
				default: break;
				}
				radioGroup2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.radio_1time:
							snoozeTimesTemp = 1;
							break;
						case R.id.radio_3times:
							snoozeTimesTemp = 3;
							break;
						case R.id.radio_5times:
							snoozeTimesTemp = 5;
							break;
						default:
							break;
						}
					}
				});

				window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						snoozeTimeInterval = snoozeTimeIntervalTemp;
						snoozeTimes = snoozeTimesTemp;
						((TextView)findViewById(R.id.textView_snoozeInterval)).setText("" + snoozeTimeInterval);
						((TextView)findViewById(R.id.textView_snoozeTimes)).setText("" + snoozeTimes);
						dialog.dismiss();
					}
				});
			}
		});

		ringtoneNameTextView = (TextView) findViewById(R.id.textView_ringTone);
		Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringtonePath));
		if (ringtone != null) {
			ringtoneNameTextView.setText(ringtone.getTitle(getApplicationContext()));
		}
				
		findViewById(R.id.view_selectRingTone).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
				dialog.show();

				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_list);

				final String[] ringtoneNames = RingtoneDatabase.getRingoneNames();
				final String[] ringtonePaths = RingtoneDatabase.getRingtonePaths();
				
				ArrayList<TextListItem> listItems = new ArrayList<TextListItem>();
				for (String string : ringtoneNames) {
					listItems.add(new TextListItem(string));
				}
				TextListAdapter listAdapter = new TextListAdapter(getApplicationContext(), 
						listItems, R.layout.ringtone_list_item);
				ListView listView = (ListView) window.findViewById(R.id.listView);
				listView.setAdapter(listAdapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						dialog.dismiss();

						ringtoneNameTextView.setText(ringtoneNames[position]);

						ringtonePath = ringtonePaths[position];
						if (mediaPlayer == null) {
							mediaPlayer = new MediaPlayer();
						} else {
							boolean isPlaying = false;
							try {
								isPlaying = mediaPlayer.isPlaying();
							} catch (Exception e) {
								mediaPlayer = new MediaPlayer();
							}
							if (isPlaying) {
								mediaPlayer.stop();
							}
							mediaPlayer.reset();
						}
						try {
							mediaPlayer.setVolume(0.2f, 0.2f);
							mediaPlayer.setDataSource(ReminderActivity.this, Uri.parse(ringtonePath));
							mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
							mediaPlayer.setLooping(false);
							mediaPlayer.prepare();
							mediaPlayer.start();

							// Force the mediaPlayer to stop after 3
							// seconds...
							if (ringtoneTimer != null) {
								ringtoneTimer.cancel();
							}
							ringtoneTimer = new CountDownTimer(3000, 3000) {
								@Override
								public void onTick(long millisUntilFinished) {
								}

								@Override
								public void onFinish() {
									try {
										if (mediaPlayer.isPlaying()) {
											mediaPlayer.stop();
											mediaPlayer.release();
										}
									} catch (Exception e) {
									}
								}
							};
							ringtoneTimer.start();
						} catch (Exception e) {
							try {
								if (mediaPlayer.isPlaying()) {
									mediaPlayer.stop();
									mediaPlayer.release();
								}
							} catch (Exception e2) {

							}
						}
					}
				});
			}
		});



		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (reminderType == -1) {
					final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
					dialog.show();
					Window window = dialog.getWindow();
					window.setContentView(R.layout.alert_dialog_p);
					TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
					messageTextView.setText(R.string.message_warnning_no_reminder_type_selected);
					window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					return;
				} 
				if ((reminderType == Reminder.TYPE_TAKE_DRUGS || reminderType == Reminder.TYPE_INSULIN) 
						&& drugName.equals("")) {
					final AlertDialog dialog = new AlertDialog.Builder(ReminderActivity.this).create();
					dialog.show();
					Window window = dialog.getWindow();
					window.setContentView(R.layout.alert_dialog_p);
					TextView messageTextView = (TextView) window.findViewById(R.id.textView_message);
					messageTextView.setText(R.string.message_warnning_no_drug_selected);
					window.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					return;
				}
				weekdayTags = "";
				for (int i = 0; i < tagSelected.length; i++) {
					if (tagSelected[i] == true) {
						weekdayTags += "" + i + " ";
					}
				}

				Reminder reminder = new Reminder(reminderType, 
						reminderTime.get(Calendar.HOUR_OF_DAY), reminderTime.get(Calendar.MINUTE),  weekdayTags, 
						snoozeTimeInterval, snoozeTimes, ringtonePath, Calendar.getInstance().getTime());
				if (reminderType == Reminder.TYPE_MEASURE_GLUCOSE) {
					reminder.setGlucoseTag(glucoseTag);
				}
				if (reminderType == Reminder.TYPE_TAKE_DRUGS || reminderType == Reminder.TYPE_INSULIN) {
					reminder.setDrugName(drugName);
					reminder.setDosage(dosage);
				}
				
				Intent intent = new Intent();
				intent.putExtra(UIConstants.EXTRA_NAME_REMINDER, reminder);
				ReminderActivity.this.setResult(RESULT_OK, intent);				
				finish();
			}
		});
	}

	private void showTimePickerDialog() {
		final TimePickerDialog timePickerDialog = 
				TimePickerDialog.newInstance(this, 
						reminderTime.get(Calendar.HOUR_OF_DAY), 
						reminderTime.get(Calendar.MINUTE), 
						false, false);
		timePickerDialog.show(getFragmentManager(), TIMEPICKER_TAG);
	}

	@SuppressLint("SimpleDateFormat") @Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		reminderTime.set(Calendar.MINUTE, minute);
		reminderTimeTextView.setText(new SimpleDateFormat("hh:mm  a").format(reminderTime.getTime()));
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && data != null) {
			if (requestCode == REQUEST_DRUG) {
				Drug drug = (Drug) data.getSerializableExtra(UIConstants.EXTRA_NAME_DRUG);
				if (drug != null) {
					drugName = drug.getName();
					drugNameTextView.setText(drugName);
				}
			} else if (requestCode == REQUEST_INSULIN) {
				Insulin drug = (Insulin) data.getSerializableExtra(UIConstants.EXTRA_NAME_INSULIN);
				if (drug != null) {
					drugName = drug.getName();
					drugNameTextView.setText(drugName);
				}
			}
		}
	}
}