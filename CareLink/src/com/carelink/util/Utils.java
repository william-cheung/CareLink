package com.carelink.util;

import android.annotation.SuppressLint;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public abstract class Utils {
	public static void roll(Calendar calendar, int days) {
		int date = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		//System.out.println("roll : " + calendar.getTime() + " days : " + days);
		if (days < 0) {
			if (-days >= date) {
				if (month == 0) calendar.set(Calendar.YEAR, year-1);
				month = (month - 1 + 12) % 12;
				//System.out.println("month : " + month);
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.MONTH, month);
				//System.out.println("after set : "  + calendar.get(Calendar.MONTH));
				int daysPreMon = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				calendar.set(Calendar.DATE, daysPreMon);
				roll(calendar, days + date);
			} else {
				calendar.set(Calendar.DATE, date + days);
			}
		} else if (days > 0) {
			int daysInMon = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			int daysRem = daysInMon - date;
			if (days <= daysRem) {
				calendar.set(Calendar.DATE, date + days);
			} else {
				if (month == 11) calendar.set(Calendar.YEAR, year+1);
				month = (month + 1 + 12) % 12;
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.DATE, 1);
				roll(calendar, days - daysRem - 1);
			}
		}
	}

	/**
	 * Check if date1 before date2
	 * @param date1
	 * @param date2
	 */
	public static boolean before(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		return calendar1.compareTo(calendar2) < 0;
	}

	public static boolean isSameDay(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR)) {
			return false;
		} else if (calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH)) {
			return false;
		} else if (calendar1.get(Calendar.DATE) != calendar2.get(Calendar.DATE)) {
			return false;
		}
		return true;
	}

	public static long diffInMillis(Date date1, Date date2) {		
		return date1.getTime() - date2.getTime();
	}

	/**
	 * 	diffInMillis(getStartOfDay(date1), getStartOfDay(date2)) / 86400000
	 */
	public static int diffInDays(Date date1, Date date2) {
		return (int) (diffInMillis(getStartOfDay(date1), getStartOfDay(date2)) / 86400000);
	}

	/**
	 * roll &lt; DATE of date &gt; 1
	 */
	public static Date tomorrow(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.roll(Calendar.DATE, 1);
		return calendar.getTime();
	}

	/**
	 * roll &lt; DATE of NOW &gt; 1
	 */
	public static Date tomorrow() {
		return tomorrow(Calendar.getInstance().getTime());
	}

	public static Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static float calcFloatValue(int intPart, int decPart) {
		float ret = 0.0f;
		while (decPart != 0) {
			ret = (ret + (decPart % 10)) * 0.1f;
			decPart /= 10;
		}
		ret +=intPart;
		return ret;
	}
	
	public static String getRandomString(int length) {
		String base = "abcdefghigklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(base.length());
			stringBuffer.append(base.charAt(index));
		}
		return stringBuffer.toString();
	}

	// TODO: Fix bugs in the following three methods
	private static SecretKey secretKeyGenerator(String key) throws Exception {
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		return secretKey;
	}

	@SuppressLint("TrulyRandom") 
	public static String encrypt(String data, String key) throws Exception {
		Key desKey = secretKeyGenerator(key);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, desKey);
		return new String(cipher.doFinal(data.getBytes()));
	}

	public static String decrypt(String data, String key) throws Exception {
		Key desKey = secretKeyGenerator(key);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, desKey);
		return new String(cipher.doFinal(data.getBytes()));
	}


	/**
	 *  Get the integer part and decimal part of an float value <br>
	 *  <br> 
	 *  Example: <br>
	 *  <p>7.12 -> {7, 1}</p> 
	 *  <p>4.93 -> {4, 9}</p> 
	 *  <p>10.8657 -> {10, 9}</p>
	 *  <p>{14.2596} -> {14, 3}</p>  
	 */
	public static int[] parseFloatValue(float value) {
		int[] ret = new int[2];
		ret[0] = (int) value;
		value -= ret[0];
		value *= 10;
		ret[1] = (int)value;
		value -= ret[1];
		if (value > 0.4f) {
			ret[1] +=1;
		}
		if (ret[1] > 9) {
			ret[0] += 1;
			ret[1] %= 10;
		}
		return ret;
	}


	/**
	 *  Extract integers in a string in which they are separated by space <br>
	 *  
	 *  For Example: "1 2 3 4 5" -> {1, 2, 3, 4, 5} (ArrayList)
	 */
	@SuppressLint("UseValueOf") 
	public static ArrayList<Integer> parseIntegerList(String ints) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		if (ints == null) return ret;
		String[] strs = ints.split(" ");
		for (String str : strs) {
			try {
				ret.add(new Integer(Integer.parseInt(str)));
			} catch (Exception e) {
			}
		}
		return ret;
	}

	/**
	 *  For Example: {1, 2, 3, 4, 5} -> "1 2 3 4 5"
	 */
	public static String integersToString(int[] array) {
		if (array == null || array.length == 0) return "";
		String ret = "" + array[0];
		for (int i = 1; i < array.length; i++) {
			ret += " " + array[i];
		}
		return ret;
	}

	/**
	 *  For Example: {1, 2, 3, 4, 5} -> "1 2 3 4 5"
	 */
	public static String integersToString(ArrayList<Integer> array) {
		if (array == null || array.size() == 0) return "";
		String ret = "" + array.get(0);
		for (int i = 1; i < array.size(); i++) {
			ret += " " + array.get(i);
		}
		return ret;
	}


	/**
	 * <p>Combine 4 bytes to form a long integer.</p>  
	 *  
	 * <p>{bytes[offset+3], ..., bytes[offset]} (msb->lsb) -> long(4 bytes)</p>
	 * 
	 */
	public static long bytes2long(byte[] bytes, int offset) {
		long ldata = (long)bytes[offset];
		ldata &= 0xff;
		ldata |= (long)(bytes[offset + 1] << 8);
		ldata &= 0xffff;
		ldata |= (long)(bytes[offset + 2] << 16);
		ldata &= 0xffffff;
		ldata |= (long)(bytes[offset + 3] << 24);
		ldata &= 0xffffffff;
		return ldata;
	}

	/**
	 * Compares two byte arrays byte by byte. Returns true if the two array has the same contents 
	 */
	public static boolean arrayequal(byte[] arr1, byte[] arr2, int len) {
		for (int i = 0; i < len; i++)
			if (arr1[i] != arr2[i])
				return false;
		return true;
	}

	/**
	 * Glucose Meter Unit Conversion from mg/dL to mmol/L
	 * @param value value of unit "mg/dL"
	 * @return value of unit "mmol/L"
	 */
	public static float mg_dL2mmol_L(int value) {
		return (float)value / 18.0f;
	}


	//	/**
	//	 * @param src e.g. "02 AC 1E 4F"
	//	 * @return e.g. byte[] : {0x02, 0xAC, 0x1E, 0x4F}
	//	 */
	//	public static byte[] hexStringToByteArray(String src) {
	//		String[] tmpStr = src.split(" ");
	//		byte[] retData = new byte[tmpStr.length];
	//		for(int i = 0; i < tmpStr.length; i++) {
	//			if(tmpStr[i].length() != 2) {
	//				return new byte[0];
	//			}			
	//			char a = tmpStr[i].charAt(0);
	//		    char b = tmpStr[i].charAt(1);
	//		    retData[i] = (byte) ((hexToInt(a) << 4) | hexToInt(b));
	//		}
	//		return retData;
	//	}
	//	
	//	/**
	//	 * @param src e.g. "02AC1EAF"
	//	 * @return e.g. byte[] : {0x02, 0xAC, 0x1E, 0x4F}
	//	 * @throws IllegalArgumentException
	//	 */
	//	public static byte[] hexStringToByteArray2(String src) throws IllegalArgumentException {
	//		  int n = src.length();
	//		  byte[] retData = new byte[n/2];
	//		  for (int i = 0; i < n; i += 2) {
	//		    char a = src.charAt(i);
	//		    char b = src.charAt(i + 1);
	//		    retData[i/2] = (byte) ((hexToInt(a) << 4) | hexToInt(b));
	//		  }
	//		  return retData;
	//	}
	//	
	//	private static int hexToInt(char ch) {
	//		  if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
	//		  if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
	//		  if ('0' <= ch && ch <= '9') { return ch - '0'; }
	//		  throw new IllegalArgumentException(String.valueOf(ch));
	//	}

	/**
	 * Convert a byte array to an object
	 */
	public static Object convertToObject(byte[] bytes) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objectInputStream;
		Object object = null;
		try {
			objectInputStream = new ObjectInputStream(byteArrayInputStream); 
			object = objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}
	/**
	 * Convert an object to a byte array. The size of the object must be less than 4096(bytes)
	 */
	public static byte[] convertToByteArray(Object object) {
		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4096);
		try {
			objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			objectOutputStream.flush();
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = byteStream.toByteArray();
		return bytes;
	}
}
