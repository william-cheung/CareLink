package com.carelink.ui;

import java.util.Calendar;
import java.util.Date;

import com.carelink.model.HealthProfile;
import com.carelink.util.JsonHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("Assert") 
public class LocalConfig {
	//private static final String TAG = "LocalConfig";
	
	private static final String PREFERENCES_NAME_ACCOUNT		= "ACCOUNT";
	private static final String KEY_UID_LAST_LOGGED_IN			= "uid_last_logged_in";
	private static final String KEY_PHONE_LAST_LOGGED_IN		= "phone_last_logged_in";
	private static final String KEY_PASSWORD_LAST_LOGGED_IN		= "password_last_logged_in";
	private static final String KEY_LOGIN_STATUS				= "login_status";
	
	private static final String KEY_CURRENT_UID					= "current_uid";
	private static final String KEY_CURRENT_USER_PHONE			= "current_user_phone";
	//private static final String KEY_CURRENT_USER_PASSWORD		= "current_user_password";
	private static final String KEY_SYNC_SERVER_DATE			= "sync_server_date";
	
	public static final int STATUS_LOGGED_IN	= 1;
	public static final int STATUS_LOGGED_OUT 	= 0;
	public static final int STATUS_FIRST_LOGIN	= -1;
	
	//private static final String SECRET_KEY = "carelink";
	private static boolean isUserMode = false;
	
	private static LocalConfig instance = null;
	
	private static Context context = null;
	
	private SharedPreferences accountPreferences = null;
	private Editor accountPrefsEditor = null;
	
	public static void init(Context context) {
		if (instance == null) {
			instance = new LocalConfig(context);
			setToGlblMode();
		}
	}
	
	private LocalConfig(Context context) {
		LocalConfig.context = context;
	}
	
	private static LocalConfig getInstance() {
		return instance;
	}
	
	public static void setToGlblMode() {
		isUserMode = false;
		getInstance().accountPreferences = context.getSharedPreferences(PREFERENCES_NAME_ACCOUNT, Context.MODE_PRIVATE);
		getInstance().accountPrefsEditor = getInstance().accountPreferences.edit();
	}
	public static void setToUserMode(int uid) {
		isUserMode = true;
		String name = PREFERENCES_NAME_ACCOUNT + "-" + uid;
		getInstance().accountPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		getInstance().accountPrefsEditor = getInstance().accountPreferences.edit();
		getInstance().accountPrefsEditor.putInt(KEY_CURRENT_UID, uid);
		getInstance().accountPrefsEditor.commit();
	}
	
	public static void writeOut() {
		getInstance().accountPrefsEditor.commit();
	}
	
	/* ---------------------------- Operations in GlobalMode ------------------------------- */
	
	public static int getUserLoginStatus() {
		assert !isUserMode;
		return getInstance().accountPreferences.getInt(KEY_LOGIN_STATUS, -1);
	}
	
	public static int getUidLastLoggedIn() {
		assert !isUserMode;
		return getInstance().accountPreferences.getInt(KEY_UID_LAST_LOGGED_IN, -1);
	}
	
	public static String getPhoneLastLoggedIn() {
		assert !isUserMode;
		return getInstance().accountPreferences.getString(KEY_PHONE_LAST_LOGGED_IN, "");
	}
	
	public static String getPasswordLastLoggedIn() {
		assert !isUserMode;
		String password = getInstance().accountPreferences.getString(KEY_PASSWORD_LAST_LOGGED_IN, "");
//		Log.d(TAG, "getPasswordLastLoggedIn: encrypted " + password);
//		
//		if (!password.equals("")) {
//			try {
//				password = Utils.decrypt(password, SECRET_KEY);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		Log.d(TAG, "getPasswordLastLoggedIn: decrypted " + password);
		return password;
	}
	
	public static void setPasswordLastLoggedIn(String password) {
		assert !isUserMode;
//		Log.d(TAG, "setPasswordLastLoggedIn: " + password);
//		try {
//			password = Utils.encrypt(password, SECRET_KEY);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		Log.d(TAG, "setPasswordLastLoggedIn: encrypted " + password);
		getInstance().accountPrefsEditor.putString(KEY_PASSWORD_LAST_LOGGED_IN, password);
	}
	
	public static void setUserLoginStatus(int status) {
		assert !isUserMode;
		getInstance().accountPrefsEditor.putInt(KEY_LOGIN_STATUS, status);
	}
	
	public static void setUidLastLoggedIn(int uid) {
		assert !isUserMode;
		getInstance().accountPrefsEditor.putInt(KEY_UID_LAST_LOGGED_IN, uid);
	}
	
	public static void setPhoneLastLoggedIn(String phone) {
		assert !isUserMode;
		getInstance().accountPrefsEditor.putString(KEY_PHONE_LAST_LOGGED_IN, phone);
	}
	
	
	/* ---------------------------- Operations in UserMode ------------------------------- */
	
	public static int getCurrentUid() {
		assert isUserMode;
		return getInstance().accountPreferences.getInt(KEY_CURRENT_UID, -1);
	}
	
	public static String getCurrentUserPhone() {
		assert isUserMode;
		return getInstance().accountPreferences.getString(KEY_CURRENT_USER_PHONE, "");
	}
	
	public static void setCurrentUserPhone(String phone) {
		assert isUserMode;
		getInstance().accountPrefsEditor.putString(KEY_CURRENT_USER_PHONE, phone);
	}
	
//	public static void setCurrentUserPassword(String password) {
//		getInstance().accountPrefsEditor.putString(KEY_CURRENT_USER_PASSWORD, password);
//	}
	
//	public static String getCurrentUserPassword() {
//		return getInstance().accountPreferences.getString(KEY_CURRENT_USER_PASSWORD, "");
//	}
	
	public static HealthProfile getHealthProfile() {
		assert isUserMode;
		SharedPreferences profilePreferences = getInstance().accountPreferences;
		HealthProfile profile = new HealthProfile();
		profile.setName(profilePreferences.getString("name", ""));
		profile.setGender(profilePreferences.getInt("gender", -1));
		int birthYear = profilePreferences.getInt("birth_year", -1);
		int birthMonth = profilePreferences.getInt("birth_month", -1);
		int birthDate = profilePreferences.getInt("birth_date", -1);
		if (birthYear != -1 && birthMonth != -1  && birthDate != -1) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(birthYear, birthMonth, birthDate);
			profile.setBirthDate(calendar.getTime());
		}
		profile.setPhone(profilePreferences.getString("phone", ""));
		profile.setHeight(profilePreferences.getInt("height", -1));
		profile.setWeight(profilePreferences.getInt("weight", -1));
		profile.setTypeOfDiabetes(profilePreferences.getInt("type_of_diabetes", -1));
		profile.setYearsOfIllness(profilePreferences.getInt("years_of_illness", -1));
		profile.setAllergyHistory(profilePreferences.getString("allergy_history", ""));
		profile.setHospitalName(profilePreferences.getString("hospital", ""));
		profile.setDoctorName(profilePreferences.getString("doctor", ""));
		return profile;
	}
	
	public static void setHealthProfile(HealthProfile profile) {
		assert isUserMode;
		SharedPreferences profilePreferences = getInstance().accountPreferences;
		if (profilePreferences != null && profile != null) {
			Editor editor = profilePreferences.edit();
			editor.putString("name", profile.getName());
			editor.putInt("gender", profile.getGender());
			if (profile.getBirthDate() != null) {
				Calendar birthDate = Calendar.getInstance();
				birthDate.setTime(profile.getBirthDate());
				editor.putInt("birth_year", birthDate.get(Calendar.YEAR));
				editor.putInt("birth_month", birthDate.get(Calendar.MONTH));
				editor.putInt("birth_date", birthDate.get(Calendar.DATE));
			}
			editor.putString("phone", profile.getPhone());
			editor.putInt("height", profile.getHeight());
			editor.putInt("weight", profile.getWeight());
			editor.putInt("type_of_diabetes", profile.getTypeOfDiabetes());
			editor.putInt("years_of_illness", profile.getYearsOfIllness());
			editor.putString("allergy_history", profile.getAllergyHistory());
			editor.putString("hospital", profile.getHospitalName());
			editor.putString("doctor", profile.getDoctorName());
			editor.commit();
		}
	}
	
	public static Date getSyncServerDate() {
		assert isUserMode;
		String dateString = getInstance().accountPreferences.getString(KEY_SYNC_SERVER_DATE, "null");
		if (dateString.equals("null")) {
			return null;
		} else {
			return JsonHelper.jsonStringToDate(dateString);
		}
 	}
	
	public static void setSyncServerDate(Date date) {
		assert isUserMode;
		String dateString = JsonHelper.dateToJsonString(date);
		getInstance().accountPrefsEditor.putString(KEY_SYNC_SERVER_DATE, dateString);
		getInstance().accountPrefsEditor.commit();
	}
}
