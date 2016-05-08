package com.device.lifescan;

import java.util.Calendar;
import java.util.Date;

import android.os.SystemClock;

import com.carelink.model.GlucoseRecord;
import com.carelink.util.Utils;
import com.comminterface.CommInterface;
import com.comminterface.FT311UARTInterface;
import com.device.GlucoseMeter;

public abstract class OneTouchMeter extends GlucoseMeter {
	private CommInterface commInterface = null;
	
	public OneTouchMeter(CommInterface commInterface) {
		this.commInterface = commInterface;
	}
	
	private void setConfigure() {
		switch (commInterface.getConnType()) {
		case CommInterface.COMM_FT311UART:
			((FT311UARTInterface)commInterface).setCofigure(9600, 8, 1, 0, 0);
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
	
	/**
	 * Get the maximum number of records that can be stored in the 
	 * Meter's memory
	 */
	public int getMaxNRecords() {
		return 0;
	}
	
	/**
	 * Power on the Meter
	 */
	public boolean powerOn() {
		setConfigure();
		
		String command = "DM?";			
		send(command.getBytes());
		
		int count = 0;
		
		while (true) {
			String response = readline();
			if (response != null && response.startsWith("?"))
				return true;
			count++;
			if (count == 5)
				return false;
			send(command.getBytes());
		}
	}
	
	/**
	 * <p>Get the Meter's software version and date</p>
	 * 
	 * Returns "" if failed
	 */
	public String getSoftwareInfo() {
		String command = "DM?";
		send(command.getBytes());
		
		// Response : ?xnn.nn.nn<space>mm/dd/yy<space>cksm<CR><LF>
		String response = readline();
		if (response == null || response.equals("") 
				|| response.length() < 20 || response.charAt(0) != '?') {
			return "";
		}
		return response.substring(2, 19);
	}
	
	/**
	 * <p>Get the Meter's serial number</p>
	 * 
	 * Returns "" if failed
	 */
	public String getMeterSN() {
		String command = "DM@";
		send(command.getBytes());
		
		// Response : @<space>"XXXXXXXXT"<space>cksm<CR><LF>
		// T : OneTouch Ultra, e.g. FKVC2C6TT
		
		// Response : @<space>"XXXXXXXXXY"<space>cksm<CR><LF> 
		// Y : OneTouch Ultra 2
		
		String response = readline();
		if (response == null || response.equals("") 
				|| response.length() < 15 || response.charAt(0) != '@') {
			return "";
		}
		return response.substring(3, 12);
	}
	
	/**
	 * <p>Get date and time from the Meter's clock</p>
	 * 
	 *  Returns null if failed
	 */
	public Date getMeterDate() {
		String command = "DMF";
		send(command.getBytes());
		
		// Response : F<space>"dow","mm/dd/yy","hh:mm:ss<space><space><space>"<space>cksm<CR><LF> 
		String response = readline();
		if (response == null || response.equals("") 
				|| response.length() < 32 || response.charAt(0) != 'F') {
			return null;
		}
		return str2date(response.substring(2, 32));
	}
	
//	/**
//	 * Set the meter clock with the specified date 
//	 * @param date
//	 * @return if successful, return true; or return false
//	 */
//	public boolean setMeterDate(Date date) {
//		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
//		String command = "DMT" + dateFormat.format(date);
//		send(command.getBytes());
//		
//		String response = readline();
//		if (response == null || !response.startsWith("T")
//				|| response.equals("T 0054")
//				|| str2date(response.substring(2, 32)) == null)
//			return false;
//		
//		return true;
//	}
	
	/**
	 * <p>Get the glucose Units setting on the meter display</p>
	 * 
	 *  Returns null if failed
	 */
	public String getMeterUnit() {
		String command = "DMSU?";
		send(command.getBytes());
		
		// Response : SU?,"MG/DL<space>"<space>cksm<CR><LF> 
		//         or SU?,"MMOL/L<space>"<space>cksm<CR><LF>		
		String response = readline();
		if (response == null || response.equals("") 
				|| response.length() < 12 || !response.startsWith("SU?")) {
			return null;
		}
		return response.substring(5, 11);
	}
	
	/**
	 * <p>Get the time format setting on the meter display (AM/PM or 24hr)</p>
	 * 
	 * Returns null if failed
	 */
	public String getTimeFormat() {
		String command = "DMST?";
		send(command.getBytes());
		
		// Response : ST?,"AM/PM<space>"<space>cksm<CR><LF> 
		//         or ST?,"24:00<space>"<space>cksm<CR><LF>		
		String response = readline();
		if (response == null || response.equals("") 
				|| response.length() < 12 || !response.startsWith("ST?")) {
			return null;
		}
		return response.substring(5, 10);
	}
	
	private GlucoseRecord[] records = null;
	private void doDownloadRecords() {
		String command = "DMP";
		send(command.getBytes());
		
		// HEADER : P<space>nnn,"MeterSN(9 chars)","MG/DL<space>"<space>cksm<CR><LF> 
		// RECORD : P<space>"dow","mm/dd/yy","hh:mm:ss<space><space><space>",
		//                  "xxnnnx",
		//                  "t","cc",
		//                   <space>00<space>cksm<CR><LF>
		
		//System.out.println("Start Downloading");
		String header = readline();
		if (header == null || !header.startsWith("P")) {
			SystemClock.sleep(1000);
			send(command.getBytes());
			header = readline();
			if (header == null || !header.startsWith("P")) {
				records = null;
				return;
			}
		}
		byte[] strBytes = header.getBytes();
		int nRecords = -1;
		try {
			nRecords = Integer.parseInt(new String(strBytes, 2, 3));
		} catch(Exception e) {
			records = null;
			return;
		}
		records = new GlucoseRecord[nRecords];
		for (int i = 0; i < nRecords; i++) {
			String recString = readline();
			if (recString == null || recString.length() < 41 || 
					(records[i] = str2record(recString.substring(2, 41))) == null) {
				records = null;
				return;
			}
		}
		//System.out.println("End Downloading");
	}
	
	/**
	 *  Return the number of glucose records in the Meter's memory or -1 if failed
	 */
	public int getNRecords() {
		if (records == null) {
			doDownloadRecords();
			if (records == null)
				return -1;
		} 
		return records.length;
	}
	
	/**
	 * <p>Download blood and control records from the Meter's memory.</p>
	 * 
	 * Returns null if failed
	 */
	public GlucoseRecord[] getAllRecords() {
		if (records == null) {
			doDownloadRecords();
			if (records == null)
				return null;
		}
		return records;
	}
	
	/**
	 * Zero the Meter's data log
	 */
	public boolean deleteAllRecords() {
		String command = "DMZ";
		send(command.getBytes());
		
		// Response : Z<space>005A<CR><LF>
		String response = readline();
		
		if (response != null && response.equals("Z 005A")) {
			records = null;
			return true;
		} else {
			SystemClock.sleep(100);
			send(command.getBytes());
			response = readline();
			if (response != null && response.equals("Z 005A")) {
				records = null;
				return true;
			}
		}
		
		return false;
	}
	
	private Date str2date(String str) { 
		// "dow","mm/dd/yy","hh:mm:ss<space><space><space>"
		// length = (5) + 1 + (10) + 1 + (1 + 8 + 3 + 1) = 30
		if (str == null || str.length() != 30)
			return null;
		int month = Integer.parseInt(str.substring(7, 9));
		int day = Integer.parseInt(str.substring(10, 12));
		int year = 2000 + Integer.parseInt(str.substring(13, 15));
		int hod = Integer.parseInt(str.substring(18, 20));
		int min = Integer.parseInt(str.substring(21, 23));
		int sec = Integer.parseInt(str.substring(24, 26));
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hod, min, sec);
		return calendar.getTime();
	}
	
	private GlucoseRecord str2record(String str) {
		// record: "dow","mm/dd/yy","hh:mm:ss<space><space><space>","xxnnnx"
		// length = 30 + 1 + 8 = 39
		if (str == null || str.length() != 39)
			return null;
		Date date = str2date(str.substring(0, 30));
		if (date == null)
			return null;
		// FIXME 
		int value = Integer.parseInt(str.substring(34, 37));
		return new GlucoseRecord(Utils.mg_dL2mmol_L(value), date, GlucoseRecord.TAG_RANDOM,  null);
	}
}
