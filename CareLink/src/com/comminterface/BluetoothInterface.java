package com.comminterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;


/**
 * The class is designed for communication between Android and Glucose Meters(or other possible devices) 
 * via Bluetooth. 
 * 
 * @author william
 * @version 0.0.1
 */
public class BluetoothInterface extends CommInterface {
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ArrayList<BluetoothDevice> arrayList = new ArrayList<BluetoothDevice>();
	private BluetoothSocket bluetoothSocket = null;
	/**
	 * Constructor
	 * @param context The context to use. Usually your Application or Activity object
	 */
	public BluetoothInterface(Context context) {
		super(context);
		
		setConnType(COMM_BLUETOOTH);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getContext().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {    
		public void onReceive(Context context, Intent intent) {        
			String action = intent.getAction();        
			// When discovery finds a device        
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {            
				// Get the BluetoothDevice object from the Intent            
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);            
				// Add the name and address to an array adapter to show in a ListView            
				arrayList.add(device);        
			}    
		}
	};
	
	/**
	 * Turn on the local Blutooth adapter
	 * @return true if successfull
	 */
	public boolean enable() {
		if (bluetoothAdapter != null) {
//			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    
//			((Activity)globalContext).startActivityForResult(enableBtIntent, -1);
			bluetoothAdapter.enable();
			int count = 15;
			while (!isEnabled()) { // wait 3 sec
				if (count-- == 0)
					return false;
				SystemClock.sleep(200);
			}
		} else {
			//FIXME
			//CommHelper.toast_s(getContext(), "No Bluetooth Adapter in the Device");
			return false;
		}
		return true;
	}
	
	/**
	 * Turn off the local Blutooth adapter
	 */
	public void disable() {
		if (isEnabled())
			bluetoothAdapter.disable();
	}
	
	/**
	 * Return true if Bluetooth is currently enabled and ready for use
	 */
	public boolean isEnabled() {
		if (bluetoothAdapter == null)
			return false;
		return bluetoothAdapter.isEnabled();
	}
	
	//FIXME NullPointerException
	private void scanDevices() {
		bluetoothAdapter.startDiscovery(); 
	}

	/**
	 * Start the remote device discovery process and return the set of BluetoothDevice objects 
	 * that are bonded (paired) to the local adapter. If Bluetooth state is not STATE_ON, this 
	 * method will return an empty set. Return null if error exists.
	 */
	public BluetoothDevice[] getBoundedDevices() {
		if (bluetoothAdapter != null) {
			scanDevices();
			return (BluetoothDevice[]) bluetoothAdapter.getBondedDevices().toArray(
					new BluetoothDevice[0]);
		}
		return null;
	}
	
	/**
	 * Connect with the given BluetoothDevice. Returns true if connected
	 */
	public boolean connect(BluetoothDevice bluetoothDevice) {
     
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
        	// MY_UUID is the app's UUID string, also used by the server code
        	 bluetoothSocket = 
        			 bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (Exception e) { }
        if (bluetoothSocket == null) {
        	//FIXME
        	System.out.println("Failed to get BluetoothSocket");
        	return false;
        }
        
        // Cancel discovery because it will slow down the connection
        bluetoothAdapter.cancelDiscovery();
     
        try {
        	// Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            bluetoothSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
            	bluetoothSocket.close();
            } catch (IOException closeException) { }
            return false;
        }
        
        if (!bluetoothSocket.isConnected()) {
        	return false;
        }
        return true;
	}
	
	public void send(byte[] data) {
		SystemClock.sleep(50);
		OutputStream outputStream;
		try {
			outputStream = bluetoothSocket.getOutputStream();
			outputStream.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(byte[] data, int offset, int length) {
		if (length > 0) {
			byte[] bytes = new byte[length];
			System.arraycopy(data, offset, bytes, 0, length);
			send(bytes);
		}
	}
	
	@Override
	public void send(byte data) {
		byte[] bytes = new byte[1];
		bytes[0] = data;
		send(bytes);
	}
	
	public boolean receive(byte[] buffer, int offset, int length) {
		try {
			InputStream inputStream = bluetoothSocket.getInputStream();
			int limit = 20;
			int count = length;
			while (count > 0) {
				if (inputStream.available() > 0) {
					int bytes = inputStream.read(buffer, offset, count);
					if (bytes == -1)
						return false;
					offset += bytes;
					count -= bytes;
				} else {
					if (limit-- == 0) {
						return false;
					}
					SystemClock.sleep(50);
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public byte recvbyte() {
		byte[] buffer = new byte[1];
		try {
			InputStream inputStream = bluetoothSocket.getInputStream();
			int limit = 3;
			while (true) {
				if (inputStream.available() > 0) {
					if (inputStream.read(buffer, 0, 1) != 1)
						return 0x00;
					else {
						break;
					}
				} else {
					if (limit-- == 0) {
						return 0x00;
					}
					SystemClock.sleep(50);
				}
			}
		} catch (Exception e) {
			return 0x00;
		}
		return buffer[0];
	}

	private byte[] buffer = new byte[512];
	public String readline() {
		int count = 0;
    	byte b;
    	
    	while ((b = recvbyte()) == '\r' || b == '\n' ) {
    		// Do Nothing
    	}
    	while (b != '\r' && b != '\n') {
    		if (b == 0) {
    			return null;
    		}
    		buffer[count++] = b;
    		b = recvbyte();
    	} 
 
		String result = "";
		if (count > 0) {
			result = new String(buffer, 0, count);
		}
		return result;
	}
	
	/**
     * Close the Bluetooth socket and release any system resources the object holds. This method does not
     * turn off the local Bluetooth adapter
     */
	public void destroy() {
		if (bluetoothSocket != null) {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		getContext().unregisterReceiver(mReceiver);
	}

	/**
	 * Do same things as BluetoothInterface.destroy()
	 */
	@Override
	public void close() {
		destroy();
	}
}
