package com.comminterface;

import android.content.Context;
import android.os.SystemClock;

/**
 * The class is designed for communication between Android and Acessories via cables 
 * that are developed using <b>FT311D</b>(a chip)
 * 
 * @author william
 * @version 0.0.1
 */
public class FT311UARTInterface extends CommInterface {
	private FT311UARTInterfaceHelper uartInterfaceHelper = null;
	
	/**
	 * Constructor
	 * @param context The context to use. Usually your Application or Activity object
	 */
	public FT311UARTInterface(Context context) {
		super(context);
		uartInterfaceHelper = new FT311UARTInterfaceHelper(context);
		setConnType(COMM_FT311UART);
	}

	/**
	 * Configure the communication parameters
	 * @param baud Baud Rate(bps)
	 * @param dataBits the number of Data Bits
	 * @param stopBits the number of Stop Bits
	 * @param parity Parity Check = 0(No) or 1 (Yes)
	 * @param flowControl Flow Control = 0(No) or 1(Yes)
	 */
	public void setCofigure(int baud, int dataBits, int stopBits,
			int parity, int flowControl) {
		uartInterfaceHelper.SetConfig(baud, (byte)dataBits, (byte)stopBits, (byte)parity, (byte)flowControl);
	}
	
	/**
	 * Prepare for reading and writing data to the USB accessory 
	 * @return true if successful
	 */
	public boolean open() {
		if (uartInterfaceHelper.ResumeAccessory() == 0) {
			return true;
		}
		return false;
	}
	
	public void send(byte[] data) {
		SystemClock.sleep(20); // wait 20 ms
		uartInterfaceHelper.SendData(data.length, data);
	}
	
	public void send(byte[] data, int offset, int length) {
		if (length > 0) {
			SystemClock.sleep(20); // wait 20 ms
			
			byte[] bytes = new byte[length];
			System.arraycopy(data, offset, bytes, 0, length);
			uartInterfaceHelper.SendData(length, bytes);
		}
	}
	
	public void send(byte data) {
		byte[] bs = new byte[1];
		bs[0] = data;
		uartInterfaceHelper.SendData(1, bs);
	}
	
	public boolean receive(byte[] buffer, int offset, int length) {
		byte[] buffer1 = new byte[length];
		int[] nbytes = new int[1];
		int count = length;
		while (count > 0) {
			SystemClock.sleep(50); // wait 50ms
			byte status = uartInterfaceHelper.ReadData(count, buffer1, nbytes);
			if (status != 0x00) {
				return false;
			}
			System.arraycopy(buffer1, 0, buffer, offset, nbytes[0]);
			offset += nbytes[0];
			count -= nbytes[0];
		}
		if (count == 0) {
			return true;
		}
		return false;
	}
	
	public byte recvbyte() {
    	byte[] data = new byte[1];
    	int count = 6;
    	while (count-- != 0 && !receive(data, 0, 1)) {
    		try { Thread.sleep(10); } catch (Exception e) {}
    	}
    	if (count >= 0) {	
    		return data[0];
    	}
    																	
		return 0;
	}
	
	private byte[] readBuffer = new byte[256];
	private int count = 0;
	private int offset = 0;
	/**
	 * Designed for more efficient readline()
	 */
	public void resetReadBuffer() {
		count = 0;
		offset = 0;
	}	
	private byte recvbyte2() { // buffered 'recvbyte', more efficient
		if (count == 0) {
			byte status;
			int[] nbytes = new int[1];
			int limit = 3;
			do {
				SystemClock.sleep(50); 
				status = uartInterfaceHelper.ReadData(readBuffer.length, readBuffer, nbytes);
				if (--limit == 0) {
					return 0;
				}
			} while (status != 0x00 || nbytes[0] == 0); 
				
			count = nbytes[0];
			offset = 0;
		}
		
		if (count > 0) {
			count--;
			return readBuffer[offset++];
		}
		
		return 0;  // read error!
	}
	
	private byte[] buffer = new byte[512];
	public String readline() {   
    	int count = 0;
    	byte b;
    
    	while ((b = recvbyte2()) == '\r' || b == '\n' ) {
    		// Do Nothing
    	}
    	while (b != '\r' && b != '\n') {
    		if (b == 0) {
    			return null;
    		}
    		buffer[count++] = b;
    		b = recvbyte2();
    	} 
 
		String result = "";
		if (count > 0) {
			result = new String(buffer, 0, count);
		}
		return result;
	}
	
	public void close() {
		uartInterfaceHelper.DestroyAccessory(true);
		uartInterfaceHelper = null;
	}
}
