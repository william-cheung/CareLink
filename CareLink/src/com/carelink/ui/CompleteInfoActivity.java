package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.UpdateHealthProfileService;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.model.HealthProfile;
import com.carelink.model.User;
import com.carelink.widget.NumberPicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class CompleteInfoActivity extends Activity {	
	
	private static final String TAG = "CompleteInfoActivity";
	
	private HealthProfile healthProfile = new HealthProfile();

	private int gender = User.GENDER_UNKNOWN;
	private int birthYear = -1, birthMonth = 1, birthDate = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_info);
		
		findViewById(R.id.view_editName).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				final TextView textView = (TextView) findViewById(R.id.textView_name);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_name);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
				editText.setText(textView.getText());
				editText.setSelection(textView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String string = editText.getText().toString();
						healthProfile.setName(string);
						textView.setText(string);
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.view_editTypeOfDiabetes).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();

				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_list);

				String[] diabeteTypes = getResources().getStringArray(R.array.type_of_diabetes);
				final ArrayList<ListItem> listItems = new ArrayList<ListItem>();
				for (String string : diabeteTypes) {
					listItems.add(new ListItem(string));
				}
				ListAdapter listAdapter = new ListAdapter(getApplicationContext(), listItems);
				ListView listView = (ListView) window.findViewById(R.id.listView);
				listView.setAdapter(listAdapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						TextView typeTextView = (TextView) findViewById(R.id.textView_typeOfDiabetes);
						typeTextView.setText(listItems.get(position).getTypeName());
						int typeOfDiabetes = User.DIABETES_NONE;
						switch (position) {
						case 0:
							typeOfDiabetes = User.DIABETES_NONE;
							break;
						case 1:
							typeOfDiabetes = User.DIABETES_TYPE_I;
							break;
						case 2:
							typeOfDiabetes = User.DIABETES_TYPE_II;
							break;
						case 3:
							typeOfDiabetes = User.DIABETES_GESTATIONAL;
							break;
						case 4:
							typeOfDiabetes = User.DIABETES_PRE;
							break;
						case 5:
							typeOfDiabetes = User.DIABETES_LADA;
							break;
						default:
							break;
						}
						healthProfile.setTypeOfDiabetes(typeOfDiabetes);
						dialog.dismiss();
					}
				});
			}
		});

		findViewById(R.id.view_editSex).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_select_sex);
				RadioGroup radioGroup = (RadioGroup) window.findViewById(R.id.radioGroup_sex);
				if (gender == User.GENDER_MALE) {
					radioGroup.check(R.id.radioButton_male);
				} else if (gender == User.GENDER_FEMALE) {
					radioGroup.check(R.id.radioButton_female);
				}
				
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.radioButton_male) {
							gender = User.GENDER_MALE;
						} else if (checkedId == R.id.radioButton_female) {
							gender = User.GENDER_FEMALE;
						}
					}
				});
				
				final TextView textView = (TextView) findViewById(R.id.textView_sex);
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (gender == User.GENDER_MALE) {
							textView.setText(R.string.text_male);
						} else if (gender == User.GENDER_FEMALE) {
							textView.setText(R.string.text_female);
						}
						healthProfile.setGender(gender);
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.view_editBirthDate).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_select_birth_date);
				final NumberPicker yearPicker = (NumberPicker)window.findViewById(R.id.numberPicker_year);
				yearPicker.setMinValue(1900);
				yearPicker.setMaxValue(2020);
				yearPicker.setValue(1975);
				final NumberPicker monthPicker = (NumberPicker)window.findViewById(R.id.numberPicker_month);
				monthPicker.setMinValue(1);
				monthPicker.setMaxValue(12);
				monthPicker.setValue(1);
				final NumberPicker datePicker = (NumberPicker)window.findViewById(R.id.numberPicker_date);
				datePicker.setMinValue(1);
				datePicker.setMaxValue(31);
			    datePicker.setValue(1);
			    final TextView textView = (TextView) findViewById(R.id.textView_birthDate);
			    window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					@SuppressLint("SimpleDateFormat") 
					public void onClick(View v) {
						birthYear = yearPicker.getValue();
						birthMonth = monthPicker.getValue();
						birthDate = datePicker.getValue();
						Calendar birthCalendar = Calendar.getInstance();
						birthCalendar.set(birthYear, birthMonth - 1, birthDate);
						textView.setText(new SimpleDateFormat("yyyy.MM.dd").format(birthCalendar.getTime()));
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.YEAR, birthYear);
						calendar.set(Calendar.MONTH, birthMonth - 1);
						calendar.set(Calendar.DATE, birthDate);
						healthProfile.setBirthDate(calendar.getTime());
						dialog.dismiss();
					}
				});
			}
		});
		findViewById(R.id.view_editHeight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.number_picker_dialog2);
				final NumberPicker numberPicker = (NumberPicker)window.findViewById(R.id.numberPicker);
				numberPicker.setMinValue(100);
				numberPicker.setMaxValue(250);
				numberPicker.setValue(160);
			    final TextView textView = (TextView) findViewById(R.id.textView_height);
			    window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int height = numberPicker.getValue();
						textView.setText("" + height);
						healthProfile.setHeight(height);
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.view_editWeight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(CompleteInfoActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.number_picker_dialog2);
				final NumberPicker numberPicker = (NumberPicker)window.findViewById(R.id.numberPicker);
				numberPicker.setMinValue(30);
				numberPicker.setMaxValue(180);
				numberPicker.setValue(60);
			    final TextView textView = (TextView) findViewById(R.id.textView_weight);
			    window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						int weight = numberPicker.getValue();
						textView.setText("" + weight);
						healthProfile.setWeight(weight);
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.button_meterTutor).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
			}
		});
		
		findViewById(R.id.button_enter_app).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				healthProfile.setPhone(LocalConfig.getCurrentUserPhone());
				LocalConfig.setHealthProfile(healthProfile);
				
				// MyApplication.toast_l(getApplicationContext(), getString(R.string.message_save_success));
				Services.getInstance(getApplicationContext()).sendRequest(
						new UpdateHealthProfileService().setParams(healthProfile), 
						new RequestCallback() {
							@Override
							public void onSuccess(Response response) {
								Log.d(TAG, "UpdateHealthProfile Success!");
							}
							@Override
							public void onFailed(Message msg) {
								Log.d(TAG, "UpdateHealthProfile Failed! " + msg.toString());
							}
						});
				
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
			}
		});
	}
	
	private class ListItem {
		private String typeName;
		public ListItem(String typeName) {
			this.typeName = typeName;
		}
		public String getTypeName() {
			return typeName;
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

		@SuppressLint("InflateParams") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.ringtone_list_item, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.textView);
			ListItem item = items.get(position);
			textView.setText(item.getTypeName());
			return convertView;
		}
	}
}