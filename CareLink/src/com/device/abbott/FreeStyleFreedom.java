package com.device.abbott;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.carelink.model.GlucoseRecord;
import com.carelink.util.Utils;
import com.comminterface.*;
import com.device.GlucoseMeter;

import android.annotation.SuppressLint;
import android.os.SystemClock;

public class FreeStyleFreedom extends GlucoseMeter {
	private CommInterface commInterface = null;
	private MeterResponse meterResponse = null;
	
	public FreeStyleFreedom(CommInterface commInterface) {
		this.commInterface = commInterface;
	}
	
	/**
	 * Get the Meter's name
	 */
	public String getMeterName() {
		return "FreeStyle FREEDOM";
	}
	
//	public int getMAXNRecords() {
//		return 0;
//	}
	
	private void setConfigure() {
		switch (commInterface.getConnType()) {
		case CommInterface.COMM_FT311UART:
			((FT311UARTInterface)commInterface).setCofigure(19200, 8, 1, 0, 0);
			break;
		default:
		}
	}
	
	private void send(byte[] data) {
		for (byte b : data) {
			SystemClock.sleep(50); 
			commInterface.send(b);
		}
	}
	
	private String readline() {
		return commInterface.readline();
	}
	
	
	
	@Override
	public boolean powerOn() {
		meterResponse = getMeterResponse();
		if (meterResponse == null) {
			return false;
		}
		return true;
	}

	/**
	 * <p>Get the Meter's serial number and date of the Meter's inner clock </p>
	 * 
	 * <p>
	 * Serial Number : XXXXXXX-XXXXX <br>
	 * Date: yyyy-MM-dd HH:mm:ss <br>
	 * </p>
	 * If failed, return "Failed to Read the Meter's Info"
	 */
	@SuppressLint("SimpleDateFormat") 
	public String getMeterInfo() {
		if (meterResponse == null) {
			if ((meterResponse = getMeterResponse()) == null) {
				return "Failed to Read the Meter's Info";
			}
		}

		String info = "";
		info += "Serial Number : " + meterResponse.getSerialNumber() + "\r\n";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		info += "Date : " + dateFormat.format(meterResponse.getMeterDate());
		return info;
	}
	
	/**
	 * <p>Download glucose records from the Meter’s memory </p>
	 *
	 * Returns null if failed 
	 */
	public GlucoseRecord[] getAllRecords() {
		if (meterResponse == null) {
			if ((meterResponse = getMeterResponse()) == null) {
				return null;
			}
		}
		return meterResponse.getRecords();
	}
	
	private MeterResponse getMeterResponse() {	
		setConfigure();
		
		//Log.log("Start Download");
		
		String command = "memmem";    // Log.log(command);	
		send(command.getBytes());     // Log.log(command);
		
		String serialNumber = readline();  // Log.log(serialNumber);
		if (serialNumber == null)
			return null;
		
		String softwareInfo = readline();
		if (softwareInfo == null)
			return null;
		
		String dateString = readline();
		if (dateString == null)
			return null;
		Date meterDate = str2meterdate(dateString);
		
		int nRecords = Integer.parseInt(readline());
		GlucoseRecord[] records = new GlucoseRecord[nRecords];
		for (int i = 0; i < nRecords; i++) {
			records[i] = str2record(readline()); 
			if (records[i] == null)
				return null;
		}
		
		//Log.log("End Download");
		
		return new MeterResponse(serialNumber, softwareInfo, meterDate, nRecords, records);
	}
	
	private static String[] months = {"Jan ", "Feb ", "Mar ", "Apr ", "May ",  "June", "July",
		    "Aug ", "Sep ", "Oct ", "Nov ", "Dec " };
	
	private Date str2meterdate(String dateString) { // Nov  14 2013 09:08:00
		byte[] strBytes = dateString.getBytes();
		int month = -1;
		for (String m : months) {
			month++;
			String tmp = new String(strBytes, 0, 4);
			if (tmp.equals(m)) {
				break;
			}
		}
		month = month + 1;
		int day = Integer.parseInt(new String(strBytes, 5, 2));
		int year = Integer.parseInt(new String(strBytes, 8, 4));
		int hour = Integer.parseInt(new String(strBytes, 13, 2));
		int minute = Integer.parseInt(new String(strBytes, 16, 2));
		int second = Integer.parseInt(new String(strBytes, 19, 2));
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute, second);
		Date meterDate = calendar.getTime();
		
		return meterDate;
	}
	
	private GlucoseRecord str2record(String str) { // 149  Nov  14 2013 08:27 14 0x00
		if (str == null || str.equals("")) {
			return null;
		}
		byte[] strBytes = str.getBytes();
		int value = Integer.parseInt(new String(strBytes, 0, 3));
		int month = -1;
		for (String m : months) {
			month++;
			String tmp = new String(strBytes, 5, 4);
			if (tmp.equals(m)) {
				break;
			}
		}
		month = month + 1;
		int day = Integer.parseInt(new String(strBytes, 10, 2));
		int year = Integer.parseInt(new String(strBytes, 13, 4));
		int hour = Integer.parseInt(new String(strBytes, 18, 2));
		int minute = Integer.parseInt(new String(strBytes, 21, 2));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute);
		Date date = calendar.getTime();
		return new GlucoseRecord(Utils.mg_dL2mmol_L(value), date, GlucoseRecord.TAG_RANDOM,  null);
	}
	
	class MeterResponse {
		private String serialNumber = "AAAADDD-ADDDD";
		private String softwareInfo = "X.XX-P";
		private Date meterDate;
		private int nRecords = 0;
		private GlucoseRecord[] records = null;
		
		public MeterResponse(String serialNumber, String softwareInfo,
				Date meterDate, int nRecords, GlucoseRecord[] records) {
			super();
			this.serialNumber = serialNumber;
			this.softwareInfo = softwareInfo;
			this.meterDate = meterDate;
			this.nRecords = nRecords;
			this.records = records;
		}
		
		public String getSerialNumber() {
			return serialNumber;
		}
		public String getSoftwareInfo() {
			return softwareInfo;
		}
		public Date getMeterDate() {
			return meterDate;
		}
		public int getnRecords() {
			return nRecords;
		}
		public GlucoseRecord[] getRecords() {
			return records;
		}
	}

	@Override
	public int getMeterType() {
		return GlucoseMeter.FREESTYLE_FREEDOM;
	}
}
