package com.device.lifescan;

import java.util.Date;

import android.os.SystemClock;
import com.carelink.model.GlucoseRecord;
import com.carelink.util.Utils;
import com.comminterface.CommInterface;
import com.comminterface.FT311UARTInterface;
import com.device.GlucoseMeter;

public abstract class OneTouchMeter2 extends GlucoseMeter {
	//private SimpleUartComm uartComm = null;
	//private BluetoothInterface btInterface = null;
	private CommInterface commInterface = null;
	
	public OneTouchMeter2(/*SimpleUartComm uartComm*/ /*BluetoothInterface bluetoothInterface*/ CommInterface commInterface) {
		//this.uartComm = uartComm;
		//btInterface = bluetoothInterface;
		this.commInterface = commInterface;
	}
	
	
	/**
	 * Get the maximum number of records that can be stored in the 
	 * Meter's memory
	 */
	public int getMaxNRecords() {
		return 0;
	}
	
	private void setConfigure() {
		//uartComm.setCofigure(9600, 8, 1, 0, 0);
		switch (commInterface.getConnType()) {
		case CommInterface.COMM_FT311UART:
			((FT311UARTInterface)commInterface).setCofigure(9600, 8, 1, 0, 0);
			break;
		default:
		}
	}
	
	protected void send(byte[] data) {
		//uartComm.send(data);
		//btInterface.send(data);
		commInterface.send(data);
	}
	
	protected boolean receive(byte[] buffer, int offset, int length) {
		return commInterface.receive(buffer, offset, length);
	}
	
	protected short calcCRC(byte[] buffer, int length) {
		short crc = (short) 0xffff;
		if (buffer != null) {
			for (int i = 0; i < length; i++ ) {
				crc = (short)((short)((crc >> 8) & 0xff) | (short)((crc << 8) & 0xffff));
				crc ^= (short)((short)buffer[i] & 0xff);
				crc ^= (crc & 0xff) >> 4;
				crc ^= crc << 12;
				crc ^= (crc & 0xff) << 5;
			}
		}
		return crc;
	}

	private static byte[] pc_ack1 = {0x02, 0x06, 0x07, 0x03, (byte) 0xFC, 0x72};
	private static byte[] pc_ack2 = {0x02, 0x06, 0x04, 0x03, (byte) 0xAF, 0x27};
	private static byte[] meter_ack1 = {0x02, 0x06, 0x06, 0x03, (byte) 0xCD, 0x41};
	//private static byte[] meter_ack2 = {0x02, 0x06, 0x05, 0x03, (byte) 0x9E, 0x14};
	
//	public boolean disconnect() 
//	{
//		byte[] data_out = {0x02, 0x06, 0x08, 0x03, (byte) 0xC2, 0x62}; // command from PC: Disconnect
//		byte[] data_in = new byte[6];;
//
//		send(data_out); 
//		receive(data_in, 0, data_in.length);
//
//		if (data_in[2] == 0x0C)
//			return true;
//		return false;
//	}

	private boolean _ot_ultra_poweron()
	{
		byte[] data_out = {0x02, 0x12, 0x00, 0x05, 0x0B, 0x02, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x19, (byte) 0xE7};
		byte[] data_in1 = new byte[6]; 
		byte[] data_in2 = new byte[17];
		
		send(data_out);
		if (receive(data_in1, 0, data_in1.length) == false)
			return false;
		if (receive(data_in2, 0, data_in2.length) == false)
			return false;
		send(meter_ack1);

		if (data_in2[3] == (byte)0x05 && data_in2[4] == (byte)0x06)
			return true;
		return false;
	}
	
	/**
	 * Power on the Meter
	 * @return true if successful
	 */
	public boolean powerOn() 
	{
		setConfigure();
		
		int count = 6;
		while (count-- != 0 && !_ot_ultra_poweron()) {
			SystemClock.sleep(1000);
		}
		if (count == -1)
			return false;
		
		byte[] buff = new byte[1];
		while (receive(buff, 0, 1)); // flush read buffer
		
		return true;
	}

	/**
	 * <p>Get the Meter�s software version and date.  (e.g. P02.00.0025/05/07)</p>
	 * 
	 * Returns null if failed
	 */
	public String getSoftwareInfo() {
		byte[] data_out = {0x02, 0x09, 0x00, 0x05, 0x0D, 0x02, 0x03, (byte) 0xDA, 0x71};
		byte[] data_in1 = new byte[6];
		byte[] data_in2 = new byte[26];
		
		// command from PC: read software version string and software creation date
		send(data_out);
		// reply from meter: ack
		if (!receive(data_in1, 0, data_in1.length))
			return null;
		// reply message from meter: S/W version string and creation date. (P02.00.0025/05/07)
		if (!receive(data_in2, 0, data_in2.length))
			return null;
		// reply from PC: ack 
		send(pc_ack1);
		
		if (data_in2[3] != 0x05 || data_in2[4] != 0x06) {
			return null;
		}
		
		return new String(data_in2, 6, 17);
	}

	/**
	 * <p>Get the Meter�s serial number</p>
	 * 
	 * Returns "" if failed
	 */
	public String getMeterSN() {
		byte[] data_out = {0x02, 0x12, 0x03, 0x05, 0x0B, 0x02, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x03, (byte) 0xBA, 0x6A};
		byte[] data_in1 = new byte[6];
		byte[] data_in2 = new byte[17];

		// send command message: read serial number
		send(data_out);
		// reply from meter: ack
		if (!receive(data_in1, 0, data_in1.length))
			return null;
		// reply from meter: serial number (C176SA0O0)
		if (!receive(data_in2, 0, data_in2.length))
			return null;
		// reply from pc: ack
		send(pc_ack2);
		
		if (data_in2[3] != 0x05 || data_in2[4] != 0x06) {
			return null;
		}

		return new String(data_in2, 5, 9);
	}

	/**
	 * Get the glucose Units setting on the meter display
	 * 
	 * @return "mg/dL" or "mmol/L" or null if failed
	 * 
	 */
	public String getMeterUnit() {
		byte[] data_out = {0x02, 0x0E, 0x00, 0x05, 0x09, 0x02, 0x09, 
				0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0xCE, (byte) 0xE7};
		byte[] data_in = new byte[12];

		// send command: read current unit settings
		send(data_out);
		// reply from meter: ack
		receive(data_in, 0, 6);
		// reply from meter: current unit settings
		receive(data_in, 0, 12);
		// reply from pc: ack
		send(pc_ack1);
			
		if (data_in[5] == 0)
			return "mg/dL";
		else if (data_in[5] == 1)
			return "mmol/L";
		return null;
	}

	/**
	 * <p>Get date and time from the Meter�s clock</p>
	 * 
	 * Returns null if failed
	 */
	public Date getMeterDate() {
		byte[] data_out = {0x02, 0x0D, 0x00, 0x05, 0x20, 0x02, 
				0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0xEC, 0x61};
		byte[] data_in = new byte[12];
			
		// send command: read RTC
		send(data_out);
		// reply from meter: ack
		receive(data_in, 0, 6);
		// reply from meter: RTC current settings
		receive(data_in, 0, 12);
		// reply from pc: ack
		send(pc_ack1);
		
		if (data_in[3] != 0x05 || data_in[4] != 0x06) {
			return null;
		}

		long ldata = Utils.bytes2long(data_in, 5) * 1000;
		return new Date(ldata);
	}

	/**
	 * Set the meter clock with the specified date 
	 * @param date
	 * @return if successful, return true; or return false
	 */
	public boolean setMeterDate(Date date) {
		byte[] data_out = {0x02, 0x0D, 0x03, 0x05, 0x20, 0x01, 
				0x00, 0x00, 0x00, 0x00, 0x03, 0x14, 0x33};
		byte[] data_in = new byte[12];
		long time, ack_time;
			
		time = date.getTime() / 1000;
		data_out[6] = (byte)(time & 0xff);
		data_out[7] = (byte)((time >> 8) & 0xff);
		data_out[8] = (byte)((time >> 16) & 0xff);
		data_out[9] = (byte)((time >> 24) & 0xff);
			
		//System.out.println(new Date(time * 1000));

		short chksum = calcCRC(data_out, 11);
		data_out[11] = (byte)(chksum & 0xff);
		data_out[12] = (byte)((chksum >> 8) & 0xff);
			
		// send command: write RTC
		send(data_out);
		// reply from meter: ack
		receive(data_in, 0, 6);
		// reply from meter: RTC current settings
		receive(data_in, 0, 12);
		// reply from pc: ack
		send(pc_ack2);

		ack_time = Utils.bytes2long(data_in, 5);

		if (ack_time == time)
			return true;
		return false;
	}

	/**
	 * Return the number of glucose records in the Meter's memory
	 * or -1 if failed
	 */
	public int getnRecords() 
	{
		byte[] data_out = {0x02, 0x0A, 0x00, 0x05, 0x1F, 
				(byte) 0xF5, 0x01, 0x03, 0x38, (byte) 0xAA};
		byte[] data_in = new byte[10];
		// send command: get number of records
		send(data_out);
		// reply from meter: ack
		receive(data_in, 0, 6);
		// reply from meter: number of records
		receive(data_in, 0, 10);
		// reply from pc: ack
		send(pc_ack1);
			
			
		if (data_in[3] != 0x05 || data_in[4] != 0x0F) {
			return -1;
		}
			
		int nRecords = (int)data_in[5];
		nRecords &= 0xff;
		nRecords |= (data_in[6] << 8);
		nRecords &= 0xffff;

		return nRecords;  
	}

	private GlucoseRecord getRecord(int offset) // Read the offset-th record
	{
		byte[] data_out = {0x02, 0x0A, 0x03, 0x05, 0x1F, 
				0x00, 0x00, // offset
				0x03, 0x4B, 0x5F};
		byte[] data_in1 = new byte[6], data_in2 = new byte[16];

		data_out[5] = (byte)(offset & 0xff);
		data_out[6] = (byte)((offset >> 8) & 0xff);

		short chksum = calcCRC(data_out, 8);
		data_out[8] = (byte)(chksum & 0xff);
		data_out[9] = (byte)((chksum >> 8) & 0xff);

		// send command: read glucose record
		send(data_out);
		// reply from meter: ack
		receive(data_in1, 0, data_in1.length);
		// reply from meter: glucose record
		receive(data_in2, 0, data_in2.length);
		// reply from pc: ack
		if (Utils.arrayequal(data_in1, meter_ack1, data_in1.length))
			send(pc_ack1);
		else
			send(pc_ack2);
			
		if (data_in2[3] != 0x05 || data_in2[4] != 0x06) {
			return null;
		}

		long date = Utils.bytes2long(data_in2, 5) * 1000;
		float value = Utils.mg_dL2mmol_L((int)Utils.bytes2long(data_in2, 9));
			
	    return new GlucoseRecord(value, new Date(date), GlucoseRecord.TAG_RANDOM,  null);
	}

	/**
	 * <p>Download glucose records from the Meter�s memory</p>
	 * 	 
	 * Returns null if failed
	 */
	public GlucoseRecord[] getAllRecords() // Read glucose records
	{
		int n = getnRecords();
		if (n == -1)
			return null;
		GlucoseRecord[] records = new GlucoseRecord[n];
		for (int i = 0; i < n; i++) 
		{
			//Log.d("meter_lib", "reading record " + i);
			if ((records[i] = getRecord(i)) == null)
				return null;
		}
		return records;
	}

	/**
	 * Delete all glucose records in the Meter
	 * @return true if successful
	 */
	public boolean deleteAllRecords()  //TODO Testing
	{
		byte[] data_out = {0x02, 0x08, 0x00, 0x05, 0x1A, 0x03, 0x56, (byte) 0xB0};
		byte[] data_in = new byte[8];

		// send command: delete all glucose records
		send(data_out);
		// reply from meter: ack
		receive(data_in, 0, 6);
		// reply from meter: command executed 
		receive(data_in, 0, 8);
		// check
		if (data_in[3] == 0x05 && data_in[4] == 0x06) 
		{
			// if STATUS_SUCCESS, ack
			send(pc_ack1);
			return true;
		}
		return false; // FAILED!
	}
}
