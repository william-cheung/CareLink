package com.carelink.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.carelink.R;
import com.carelink.interaction.Message;
import com.carelink.interaction.Services;
import com.carelink.interaction.services.ServiceBase.RequestCallback;
import com.carelink.interaction.services.UpdateHealthProfileService;
import com.carelink.interaction.services.ServiceBase.Response;
import com.carelink.model.HealthProfile;
import com.carelink.model.User;
import com.carelink.widget.NumberPicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class HealthProfileActivity extends MyActivity {	
	private static final String TAG = "HealthProfileActivity";
	
	private HealthProfile healthProfile = null;

	@SuppressLint("SimpleDateFormat") @Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_back_save);
		setContentViewResource(R.layout.activity_health_profile);
		setTitleTextResource(R.string.title_activity_health_profile);
		super.onCreate(savedInstanceState);
		
		healthProfile = LocalConfig.getHealthProfile();

		final TextView nameTextView = (TextView) findViewById(R.id.textView_name);
		nameTextView.setText(healthProfile.getName());
		findViewById(R.id.view_editName).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_name);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
				editText.setText(nameTextView.getText());
				editText.setSelection(nameTextView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String name = editText.getText().toString();
						nameTextView.setText(name);
						healthProfile.setName(name);
						dialog.dismiss();
					}
				});
			}
		});
		
		final TextView phoneTextView = (TextView) findViewById(R.id.textView_phone);
		phoneTextView.setText(healthProfile.getPhone());
		findViewById(R.id.view_editPhone).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_phone);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
				editText.setText(phoneTextView.getText());
				editText.setSelection(phoneTextView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String phone = editText.getText().toString();
						phoneTextView.setText(phone);
						healthProfile.setPhone(phone);
						dialog.dismiss();
					}
				});
			}
		});

		final TextView genderTextView = (TextView) findViewById(R.id.textView_sex);
		genderTextView.setText(getGenderString(healthProfile.getGender()));
		findViewById(R.id.view_editSex).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_select_sex);
				RadioGroup radioGroup = (RadioGroup) window.findViewById(R.id.radioGroup_sex);
				if (healthProfile.getGender() == User.GENDER_MALE) {
					radioGroup.check(R.id.radioButton_male);
				} else if (healthProfile.getGender() == User.GENDER_FEMALE) {
					radioGroup.check(R.id.radioButton_female);
				}
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.radioButton_male) {
							healthProfile.setGender(User.GENDER_MALE);
						} else if (checkedId == R.id.radioButton_female) {
							healthProfile.setGender(User.GENDER_FEMALE);
						}
					}
				});
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						genderTextView.setText(getGenderString(healthProfile.getGender()));
						dialog.dismiss();
					}
				});
			}
		});
		
		final TextView birthDateTextView = (TextView) findViewById(R.id.textView_birthDate);
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		if (healthProfile.getBirthDate() != null) {
			birthDateTextView.setText(simpleDateFormat.format(healthProfile.getBirthDate()));
		}
		findViewById(R.id.view_editBirthDate).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_select_birth_date);
				
				int birthYear  = 1980, birthMonth = 1, birthDate  = 1;
				if (healthProfile.getBirthDate() != null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(healthProfile.getBirthDate());
					birthYear = calendar.get(Calendar.YEAR);
					birthMonth = calendar.get(Calendar.MONTH) + 1;
					birthDate = calendar.get(Calendar.DATE);
				}
				
				final NumberPicker yearPicker = (NumberPicker)window.findViewById(R.id.numberPicker_year);
				yearPicker.setMinValue(1900);
				yearPicker.setMaxValue(2020);
				yearPicker.setValue(birthYear);
				final NumberPicker monthPicker = (NumberPicker)window.findViewById(R.id.numberPicker_month);
				monthPicker.setMinValue(1);
				monthPicker.setMaxValue(12);
				monthPicker.setValue(birthMonth);
				final NumberPicker datePicker = (NumberPicker)window.findViewById(R.id.numberPicker_date);
				datePicker.setMinValue(1);
				datePicker.setMaxValue(31);
			    datePicker.setValue(birthDate);
			    
			    window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Calendar calendar = Calendar.getInstance();
						calendar.set(yearPicker.getValue(), monthPicker.getValue() - 1, datePicker.getValue());
						healthProfile.setBirthDate(calendar.getTime());
						birthDateTextView.setText(simpleDateFormat.format(healthProfile.getBirthDate()));
						dialog.dismiss();
					}
				});
			}
		});
		
		final TextView heightTextView = (TextView) findViewById(R.id.textView_height);
		if (healthProfile.getHeight() != -1) {
			heightTextView.setText("" + healthProfile.getHeight());
		}
		findViewById(R.id.view_editHeight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.number_picker_dialog2);
				
				int height = 165;
				if (healthProfile.getHeight() != -1) {
					height = healthProfile.getHeight();
				}
				
				final NumberPicker numberPicker = (NumberPicker)window.findViewById(R.id.numberPicker);
				numberPicker.setMinValue(100);
				numberPicker.setMaxValue(250);
				numberPicker.setValue(height);
			    
			    window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						healthProfile.setHeight(numberPicker.getValue());
						heightTextView.setText("" + healthProfile.getHeight());
						dialog.dismiss();
					}
				});
			}
		});
		
		
		final TextView weightTextView = (TextView) findViewById(R.id.textView_weight);
		if (healthProfile.getWeight() != -1) {
			weightTextView.setText("" + healthProfile.getWeight());
		}
		findViewById(R.id.view_editWeight).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.number_picker_dialog2);
				
				int weight = 60;
				if (healthProfile.getWeight() != -1) {
					weight = healthProfile.getWeight();
				}
				
				final NumberPicker numberPicker = (NumberPicker)window.findViewById(R.id.numberPicker);
				numberPicker.setMinValue(30);
				numberPicker.setMaxValue(180);
				numberPicker.setValue(weight);
			    window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						healthProfile.setWeight(numberPicker.getValue());
						weightTextView.setText("" + healthProfile.getWeight());
						dialog.dismiss();
					}
				});
			}
		});
		
		
		final String[] diabeteTypes = getResources().getStringArray(R.array.type_of_diabetes);
		final TextView typeTextView = (TextView) findViewById(R.id.textView_typeOfDiabetes);
		int index = -1;
		switch (healthProfile.getTypeOfDiabetes()) {
		case User.DIABETES_NONE: 			index = 0; 	break;
		case User.DIABETES_TYPE_I:			index = 1;	break;
		case User.DIABETES_TYPE_II:			index = 2; 	break;
		case User.DIABETES_GESTATIONAL:		index = 3;	break;
		case User.DIABETES_PRE:				index = 4; 	break;
		case User.DIABETES_LADA:			index = 5; 	break;
		default:										break;
		}
		if (index != -1) {
			typeTextView.setText(diabeteTypes[index]);
		}
		
		findViewById(R.id.view_editTypeOfDiabetes).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.dialog_list);

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
						typeTextView.setText(listItems.get(position).getTypeName());
						int typeOfDiabetes = healthProfile.getTypeOfDiabetes();
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
		
		
		final TextView yearsTextView = (TextView) findViewById(R.id.textView_yearsOfIllness);
		if (healthProfile.getYearsOfIllness() != -1) {
			yearsTextView.setText("" + healthProfile.getYearsOfIllness());
		}
		findViewById(R.id.view_editYearsOfIllness).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.setContentView(R.layout.number_picker_dialog2);
				final NumberPicker numberPicker = (NumberPicker)window.findViewById(R.id.numberPicker);
				numberPicker.setMinValue(0);
				numberPicker.setMaxValue(60);
				
				int yearsOfIllness = healthProfile.getYearsOfIllness();
				if (yearsOfIllness != -1) {
					numberPicker.setValue(yearsOfIllness);
				} else {
					numberPicker.setValue(0);
				}
			    window.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						healthProfile.setYearsOfIllness(numberPicker.getValue());
						yearsTextView.setText("" + healthProfile.getYearsOfIllness());
						dialog.dismiss();
					}
				});
			}
		});
		
		
		final TextView allergytextView = (TextView) findViewById(R.id.textView_allergyHistory);
		allergytextView.setText(healthProfile.getAllergyHistory());
		findViewById(R.id.view_editAllergyHistory).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_allergy_history);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
				editText.setText(allergytextView.getText());
				editText.setSelection(allergytextView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String allergyHistory = editText.getText().toString();
						allergytextView.setText(allergyHistory);
						healthProfile.setAllergyHistory(allergyHistory);
						dialog.dismiss();
					}
				});
			}
		});
		
		final TextView hospitalTextView = (TextView) findViewById(R.id.textView_hospital);
		hospitalTextView.setText(healthProfile.getHospitalName());
		findViewById(R.id.view_editHospital).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_current_hospital);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
				editText.setText(hospitalTextView.getText());
				editText.setSelection(hospitalTextView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String hospitalName = editText.getText().toString();
						hospitalTextView.setText(hospitalName);
						healthProfile.setHospitalName(hospitalName);
						dialog.dismiss();
					}
				});
			}
		});
		
		final TextView doctorTextView = (TextView) findViewById(R.id.textView_doctor);
		doctorTextView.setText(healthProfile.getDoctorName());
		findViewById(R.id.view_editDoctor).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final AlertDialog dialog = new AlertDialog.Builder(HealthProfileActivity.this).create();
				dialog.show();
				Window window = dialog.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				window.setContentView(R.layout.dialog_edit_box);
				TextView textView2 = (TextView) window.findViewById(R.id.textView);
				textView2.setText(R.string.text_current_doctor);
				final EditText editText = (EditText) window.findViewById(R.id.editText);
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
				editText.setText(doctorTextView.getText());
				editText.setSelection(doctorTextView.getText().length());
				window.findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String doctorName = editText.getText().toString();
						doctorTextView.setText(doctorName);
						healthProfile.setDoctorName(doctorName);
						dialog.dismiss();
					}
				});
			}
		});
		
		findViewById(R.id.view_save).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveHealthProfile(healthProfile);
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
	
	private void saveHealthProfile(HealthProfile profile) {
		if (profile == null) return;
		LocalConfig.setHealthProfile(profile);
		MyApplication.toast_l(getApplicationContext(), getString(R.string.message_save_success));
		Services.getInstance(getApplicationContext()).sendRequest(
				new UpdateHealthProfileService().setParams(profile), 
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
	}
	
	private String getGenderString(int gender) {
		if (gender == User.GENDER_MALE) {
			return getString(R.string.text_male);
		} else if (gender == User.GENDER_FEMALE) {
			return getString(R.string.text_female);
		}
		return "";
	}
}
