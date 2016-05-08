package com.comminterface; 

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//import com.will.utils.Log;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

/**
 * A Helper Class<br> 
 * <br>
 * Created by <b>FTDI</b>; Modified by william<br>
 * <br>
 * <b>Date:</b> yyyy-mm-dd=2014-03-04
 * 
 * @author william
 * @version 0.0.1
 */

public class FT311UARTInterfaceHelper {

	private static final String ACTION_USB_PERMISSION = "com.UARTLoopback.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor = null;
	public FileInputStream inputstream = null;
	public FileOutputStream outputstream = null;
	public boolean mPermissionRequestPending = false;
	public read_thread readThread;

	private byte [] usbdata; 
	private byte []	writeusbdata;
	private byte  [] readBuffer; 		// Circular Buffer
	private int totalBytes;
	private int writeIndex;
	private int readIndex;
	private byte status;
	final int  maxnumbytes = 65536;

	public boolean datareceived = false;
	public boolean READ_ENABLE = false;
	public boolean accessory_attached = false;

	public Context global_context;

	public static String ManufacturerString = "mManufacturer=FTDI";
	public static String ModelString1 = "mModel=FTDIUARTDemo";
	public static String ModelString2 = "mModel=Android Accessory FT312D";
	public static String VersionString = "mVersion=1.0";

	public FT311UARTInterfaceHelper(Context context) {
		global_context = context;
		
		/*shall we start a thread here or what*/
		usbdata = new byte[1024]; 
		writeusbdata = new byte[256];
		
		/*128(make it 256, but looks like bytes should be enough)*/
		readBuffer = new byte [maxnumbytes];
		
		readIndex = writeIndex = 0;

		usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		context.registerReceiver(mUsbReceiver, filter);
	}

	public void SetConfig(int baud, byte dataBits, byte stopBits,
			byte parity, byte flowControl) {

		/*prepare the baud rate buffer*/
		writeusbdata[0] = (byte)baud;
		writeusbdata[1] = (byte)(baud >> 8);
		writeusbdata[2] = (byte)(baud >> 16);
		writeusbdata[3] = (byte)(baud >> 24);

		/*data bits*/
		writeusbdata[4] = dataBits;
		/*stop bits*/
		writeusbdata[5] = stopBits;
		/*parity*/
		writeusbdata[6] = parity;
		/*flow control*/
		writeusbdata[7] = flowControl;

		/*send the UART configuration packet*/
		SendPacket((int)8);
	}

	public byte SendData(int numBytes, byte[] buffer)  {
		status = 0x00; /*success by default*/
		/*
		 * if num bytes are more than maximum limit
		 */
		if(numBytes < 1) {
			/*return the status with the error in the command*/
			return status;
		}

		/*check for maximum limit*/
		if(numBytes > 256) {
			numBytes = 256;
		}

		/*prepare the packet to be sent*/
		for(int count = 0;count<numBytes;count++) {	
			writeusbdata[count] = buffer[count];
		}

		if(numBytes != 64) {
			SendPacket(numBytes);
		} else {
			byte temp = writeusbdata[63];
			SendPacket(63);
			writeusbdata[0] = temp;
			SendPacket(1);
		}

		return status;
	}
	
	/**
	 * Reads up to <b>numBytes</b> bytes from the accessory and stores them in the byte array <b>buffer</b>. 
	 * <b>actualNumBytes</b> holds the number of bytes actually read.<br>
	 * Returns 0x00 if successful or 0x01 if failed
	 */
	public byte ReadData(int numBytes,byte[] buffer, int [] actualNumBytes) {
		status = 0x00; /*success by default*/

		/*should be at least one byte to read*/
		if((numBytes < 1) || (totalBytes == 0)) {
			actualNumBytes[0] = 0;
			status = 0x01;
			return status;
		}

		/*check for max limit*/
		if(numBytes > totalBytes)
			numBytes = totalBytes;

		/*update the number of bytes available*/
		totalBytes -= numBytes;

		actualNumBytes[0] = numBytes;	

		/*copy to the user buffer*/	
		for(int count = 0; count<numBytes;count++) {
			buffer[count] = readBuffer[readIndex];
			readIndex++;
			/*shouldnt read more than what is there in the buffer,
			 * 	so no need to check the overflow
			 */
			readIndex %= maxnumbytes;
		}
		return status;
	}

	private void SendPacket(int numBytes) {	
		try {
			if (outputstream != null) {
				outputstream.write(writeusbdata, 0, numBytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int ResumeAccessory() {
		// Intent intent = getIntent();
		if (inputstream != null && outputstream != null) {
			return 1;
		}

		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		if(accessories == null) {
			// return 2 for accessory detached case
			accessory_attached = false;
			return 2;
		}

		UsbAccessory accessory = accessories[0];
		if (accessory != null) {
			accessory_attached = true;
			
			if (usbmanager.hasPermission(accessory)) {
				if (!OpenAccessory(accessory)) {
					Toast.makeText(global_context, "Oops, Open Accessory Failed", Toast.LENGTH_SHORT).show();
					return 1;
				}
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						Toast.makeText(global_context, "Request USB Permission", Toast.LENGTH_SHORT).show();
						usbmanager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		}
		
		Toast.makeText(global_context, "Ready to Communicate with Meter!", Toast.LENGTH_SHORT).show();
		
		return 0;
	}

	public void DestroyAccessory(boolean bConfiged) {
		if(true == bConfiged) {
			READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
			writeusbdata[0] = 0;  // send dummy data for instream.read going
			SendPacket(1);
		} else {
			SetConfig(9600,(byte)1,(byte)8,(byte)0,(byte)0);  // send default setting data for config
			try{Thread.sleep(10);}
			catch(Exception e){}

			READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
			writeusbdata[0] = 0;  // send dummy data for instream.read going
			SendPacket(1);
			if(true == accessory_attached) {}
		}

		try { Thread.sleep(10);}
		catch(Exception e){}	
		
		CloseAccessory();
	}

	public boolean OpenAccessory(UsbAccessory accessory)
	{
		filedescriptor = usbmanager.openAccessory(accessory);
		if(filedescriptor != null) {
			usbaccessory = accessory;

			FileDescriptor fd = filedescriptor.getFileDescriptor();

			inputstream = new FileInputStream(fd);
			outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
			if(inputstream == null || outputstream==null) {
				return false;
			}

			if(READ_ENABLE == false) {
				READ_ENABLE = true;
				readThread = new read_thread(inputstream);
				readThread.start();
			}
			return true;
		}
		return false;
	}

	public void CloseAccessory() {
		try {
			if (filedescriptor != null) {
				filedescriptor.close();
				filedescriptor = null;
			}
			
		} catch(IOException e) {
			//Log.log("Close Accessory Error1");
		}

		try {
			if (inputstream != null) {
				inputstream.close();
				inputstream = null;
			}
		} catch(IOException e) { 
			//Log.log("Close Accessory Error2");
		}

		try {
			if (outputstream != null) {
				outputstream.close();
				outputstream = null;
			}
		} catch(IOException e) { 
			//Log.log("Close Accessory Error3");
		}
		
		/* FIXME: add the notfication also to close the application */
		//Log.log("Closing USB Accessory ... ");
		
		READ_ENABLE = false;
		
		global_context.unregisterReceiver(mUsbReceiver);

		System.exit(0);
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Toast.makeText(global_context, "USB Permission Allowed", Toast.LENGTH_SHORT).show();
						if (!OpenAccessory(accessory)) {
							Toast.makeText(global_context, "Oops, Open Accessory Failed", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(global_context, "USB Permission Denied", Toast.LENGTH_SHORT).show();
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))  {
				DestroyAccessory(true);
			}
		}	
	};

	// usb input data handler
	private class read_thread extends Thread {
		FileInputStream instream;

		read_thread(FileInputStream stream ) {
			instream = stream;
			this.setPriority(Thread.MAX_PRIORITY);
		}

		public void run() {		
			//Log.log("Message1 From ReadThread");
			
			while (READ_ENABLE == true) {
				//Log.log("Message2 From ReadThread");
				
				while(totalBytes > (maxnumbytes - 1024)) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {e.printStackTrace();}
				}

				try {
					if(instream != null) {	
						//Log.log("Message3 From ReadThread");
						//Log.log("Available: " + inputstream.available() + " bytes");
						int readcount = instream.read(usbdata, 0, 1024);
						//Log.log("Read: " + readcount + " bytes");
						if(readcount > 0) {
							for(int count = 0;count < readcount; count++) {					    			
								readBuffer[writeIndex] = usbdata[count];
								writeIndex++;
								writeIndex %= maxnumbytes;
							}

							if(writeIndex >= readIndex)
								totalBytes = writeIndex-readIndex;
							else
								totalBytes = (maxnumbytes-readIndex)+writeIndex;
						}
					} else {
						READ_ENABLE = false;
						Toast.makeText(global_context, "RunTime Error: inputstream == null", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e){
					//e.printStackTrace();
					//Log.log("Exception From ReadThread");
				}
			}
		}
	}
}