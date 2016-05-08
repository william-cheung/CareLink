package com.comminterface;

import android.content.Context;


/**
 * A abstract class used to support communication between Android 
 * and Glucose Meters(or other possible devices). 
 * 
 * @author william
 * @version 0.0.1
 * 
 */

public abstract class CommInterface {
	 
	/**
	 * Connection Type: <b>Inval</b>id connection <b>type</b>
	 */
	public static final int COMM_INVALTYPE = 0;	
	/**
	 * Connection Type: FT311 uart
	 */
	public static final int COMM_FT311UART = 1;  
	/**
	 * Connection Type: Bluetooth
	 */
	public static final int COMM_BLUETOOTH = 2;  
	
	private int connType;
	private Context context;
	
	/**
	 * Constructor
	 * @param context The context to use. Usually your Application or Activity object
	 */
	protected CommInterface(Context context) {
		this.context = context;
		setConnType(COMM_INVALTYPE);
	}
	
	protected Context getContext() {
		return context;
	}
	
	/**
	 * Set the connection type
	 * @param type COMM_INVALTYPE, COMM_FT311UART or COMM_BLUETOOTH
	 */
	protected void setConnType(int type) {
		connType = type;
	}
	
	/**
	 * Get the connection type
	 * @return The connection type. COMM_INVALTYPE, COMM_FT311UART or COMM_BLUETOOTH  
	 */
	public int getConnType() {
		return connType;
	}
	
	/**
	 * Send data(usually commands) to the connected device
	 */
	public abstract void send(byte[] data);
	
	/**
	 * Send <b>length</b> bytes stored in the byte array <b>data</b> starting at <b>offset</b> 
	 * to the connected device
	 */
	public abstract void send(byte[] data, int offset, int length);
	
	/**
	 * Send a byte to the connected device
	 */
	public abstract void send(byte data);
	
	/**
	 * Reads up to <b>length</b> bytes from the connected device
	 * and stores them in the byte array <b>buffer</b> starting at <b>offset</b>. 
	 * Returns false if failed.
	 */
	public abstract boolean receive(byte[] buffer, int offset, int length);
	
	/**
	 * Read a byte from the connected device
	 * @return An ASCII coded character or 0x00 if failed
	 */
	public abstract byte recvbyte();
	
	/**
	 * Read a line ended with "\r\n", "\r" or "\n" from the connected device
	 * @return A string that contains a line WITHOUT '\r' and '\n' or null if failed
	 */
	public abstract String readline();
	
	/**
     * Close the object and release any system resources it holds 
     */
	public abstract void close();
}
