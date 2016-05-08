package com.device.lifescan;

import com.comminterface.CommInterface;
import com.device.GlucoseMeter;

public class OneTouchSelect extends OneTouchMeter2 {
	
	private static byte[] pc_ack1 = {0x02, 0x06, 0x07, 0x03, (byte) 0xFC, 0x72};
	//private static byte[] pc_ack2 = {0x02, 0x06, 0x04, 0x03, (byte) 0xAF, 0x27};
	//private static byte[] meter_ack1 = {0x02, 0x06, 0x06, 0x03, (byte) 0xCD, 0x41};
	//private static byte[] meter_ack2 = {0x02, 0x06, 0x05, 0x03, (byte) 0x9E, 0x14};
	
	public OneTouchSelect(CommInterface commInterface) {
		super(commInterface);
	}
	
	public int getMeterType() {
		return GlucoseMeter.ONETOUCH_SELECT;
	}

	@Override
	public String getMeterName() {
		return "OneTouch Select";
	}
	
	@Override
	public int getMaxNRecords() {
		return 150;
	}

	@Override
	public String getSoftwareInfo() {
		byte[] data_out = {0x02, 0x09, 0x00, 0x05, 0x0D, 0x03, 0x03, (byte) 0xEB, 0x42};
		byte[] data_in1 = new byte[6];
		byte[] data_in2 = new byte[26];
		
		// command from PC: read software version string and software creation date
		send(data_out);
		// reply from meter: ack
		if (!receive(data_in1, 0, data_in1.length))
			return null;
		// reply message from meter: S/W version string and creation date. (P02.00.0009/03/07)
		if (!receive(data_in2, 0, data_in2.length))
			return null;
		// reply from PC: ack 
		send(pc_ack1);
		
		if (data_in2[3] != 0x05 || data_in2[4] != 0x06) {
			return null;
		}
		
		byte[] info = new byte[17];
		System.arraycopy(data_in2, 6, info, 0, 17); 
		return new String(info);
	}

	@Override
	public String getMeterSN() {
		byte[] data_out = {0x02, 0x12, 0x00, 0x05, 0x0B, 0x02, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x03, 0x19, (byte) 0xE7};
		byte[] data_in1 = new byte[6];
		byte[] data_in2 = new byte[17];

		// send command message: read serial number
		send(data_out);
		// reply from meter: ack
		if (!receive(data_in1, 0, data_in1.length))
			return null;
		// reply from meter: serial number (KDG15001)
		if (!receive(data_in2, 0, data_in2.length))
			return null;
		// reply from pc: ack
		send(pc_ack1);
		
		if (data_in2[3] != 0x05 || data_in2[4] != 0x06) {
			return null;
		}

		byte[] serial = new byte[8];
		System.arraycopy(data_in2, 5, serial, 0, 8);
		return new String(serial);
	}
	
	// TODO : public String getTimeFormat()
	
	
}
