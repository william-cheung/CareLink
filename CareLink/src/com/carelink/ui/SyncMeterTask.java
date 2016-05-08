package com.carelink.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.carelink.model.GlucoseRecord;
import com.carelink.model.Record;
import com.comminterface.BluetoothInterface;
import com.device.GlucoseMeter;
import com.device.abbott.FreeStyleFreedom;
import com.device.lifescan.OneTouchSelect;
import com.device.lifescan.OneTouchUltra;
import com.device.lifescan.OneTouchUltra2;
import com.device.lifescan.OneTouchUltraEasy;
import com.device.lifescan.OneTouchUltraMini;

public class SyncMeterTask extends AsyncTask<Void, Object, Void>{
	private BluetoothInterface bluetoothInterface = null;
	private boolean wasBluetoothEnabled = false;
	
	private int meterType = -1;
	private GlucoseMeter glucoseMeter = null;
	private boolean isMeterPoweredOn = false;
	
	private SyncMeterCallBack syncMeterCallBack = null;

	public SyncMeterTask(Context context, int meterType, SyncMeterCallBack callBack) {
		this.meterType = meterType;
		bluetoothInterface = new BluetoothInterface(context);
		syncMeterCallBack = callBack;
	}

	private void enableBluetooth() {
		publishProgress(0);
		if (!bluetoothInterface.isEnabled()) {
			wasBluetoothEnabled = false;
			bluetoothInterface.enable();
		} else {
			wasBluetoothEnabled = true;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!bluetoothInterface.isEnabled()) {
			publishProgress(2);
			//syncMeterCallBack.onEnableBluetoothFailed();
		} else {
			publishProgress(1);
		}
	}

	private void connectMeterAndReadData() {
		publishProgress(3);
		//syncMeterCallBack.preConnectBluetooth();
		
		Log.d(getClass().getSimpleName(), "in connectMeterAndReadData()");
		
		BluetoothDevice[] allBoundedDevices = bluetoothInterface.getBoundedDevices();
		BluetoothDevice bluetoothDevice = null;
		if (allBoundedDevices != null) {
			for (BluetoothDevice device : allBoundedDevices) {
				if (device.getName().equals("HC-05")) {
					bluetoothDevice = device;
				}
			}  			
		}
		
		Log.d(getClass().getSimpleName(), "Seraching bluetooth device ...");

		if (bluetoothDevice != null) {
			Log.d(getClass().getSimpleName(), "connecting " + bluetoothDevice.getName() + " ...");
			if (bluetoothInterface.connect(bluetoothDevice)) {
				readData();
			} else {
				publishProgress(5);
				//syncMeterCallBack.onConnectBluetoothFailed();
				release();
			}
		} else {
			Log.d(getClass().getSimpleName(), "bluetoothDevice is null");
			publishProgress(5);
			//syncMeterCallBack.onConnectBluetoothFailed();
			release();
		}
	}

	private void readData() {
		initGlucoseMeter();
		publishProgress(6);
		//syncMeterCallBack.prePoweronMeter();
		Log.d(getClass().getSimpleName(), "power on meter ...");
		if (glucoseMeter != null && glucoseMeter.powerOn()) {
			publishProgress(7);
			//syncMeterCallBack.onPoweronMeterSuccess();
			
			isMeterPoweredOn = true;
			
			publishProgress(9);
			SystemClock.sleep(4000);
			//syncMeterCallBack.preReadData();
			Log.d(getClass().getSimpleName(), "reading data ...");
			GlucoseRecord[] records = glucoseMeter.getAllRecords();
			if (records != null) {
				publishProgress(10, records);
				//syncMeterCallBack.onReadDataSuccess(records.length);
			} else {
				Log.d(getClass().getSimpleName(), "reading data failed ... try again ...");
				SystemClock.sleep(4000);
				records = glucoseMeter.getAllRecords();
				if (records != null) {
					publishProgress(10, records);
				} else {
					publishProgress(11);
					//syncMeterCallBack.onReadDataFailed();
					release();
				}
			}
		} else {
			publishProgress(8);
			//syncMeterCallBack.onPoweronMeterFailed();
			release();
		}
	}
	
	private void initGlucoseMeter() {
		switch (meterType) {
		case GlucoseMeter.ONETOUCH_ULTRA:
			glucoseMeter = new OneTouchUltra(bluetoothInterface);
			break;
		case GlucoseMeter.ONETOUCH_ULTRAEASY:
			glucoseMeter = new OneTouchUltraEasy(bluetoothInterface);
			break;
		case GlucoseMeter.ONETOUCH_ULTRA2:
			glucoseMeter = new OneTouchUltra2(bluetoothInterface);
			break;
		case GlucoseMeter.ONETOUCH_ULTRAMINI:
			glucoseMeter = new OneTouchUltraMini(bluetoothInterface);
			break;
		case GlucoseMeter.ONETOUCH_SELECT:
			glucoseMeter = new OneTouchSelect(bluetoothInterface);
			break;
		case GlucoseMeter.FREESTYLE_FREEDOM:
			glucoseMeter = new FreeStyleFreedom(bluetoothInterface);
			break;
		default:
			break;
		}
	}
	
	public void clearData() {
		if (glucoseMeter != null && isMeterPoweredOn) {
			Log.d("SyncMeterService", "Clear data ...");
			glucoseMeter.deleteAllRecords();
		}
	}
	
	public void release() {
		bluetoothInterface.destroy();
		if (!wasBluetoothEnabled) {
			bluetoothInterface.disable();
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		enableBluetooth();
		connectMeterAndReadData();
		return null;
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		int status = (Integer) values[0];
		switch (status) {
		case 0:
			syncMeterCallBack.preEnableBluetooth();
			break;
		case 1:
			syncMeterCallBack.onEnableBluetoothSuccess();
			break;
		case 2:
			syncMeterCallBack.onEnableBluetoothFailed();
			break;
		case 3:
			syncMeterCallBack.preConnectBluetooth();
			break;
		case 4:
			syncMeterCallBack.onConnectBluetoothSuccess();
			break;
		case 5:
			syncMeterCallBack.onConnectBluetoothFailed();
			break;
		case 6:
			syncMeterCallBack.prePoweronMeter();
			break;
		case 7:
			syncMeterCallBack.onPoweronMeterSuccess();
			break;
		case 8:
			syncMeterCallBack.onPoweronMeterFailed();
			break;
		case 9:
			syncMeterCallBack.preReadData();
			break;
		case 10:
			syncMeterCallBack.onReadDataSuccess((Record[]) values[1]);
			break;
		case 11:
			syncMeterCallBack.onReadDataFailed();
			break;
		default:
			break;
		}
		super.onProgressUpdate(values);
	}
}

interface SyncMeterCallBack {
	public void preEnableBluetooth();
	public void onEnableBluetoothSuccess(); 		
	public void onEnableBluetoothFailed(); 
	public void preConnectBluetooth();
	public void onConnectBluetoothSuccess(); 	
	public void onConnectBluetoothFailed(); 
	public void prePoweronMeter();
	public void onPoweronMeterSuccess(); 		
	public void onPoweronMeterFailed(); 
	public void preReadData();
	public void onReadDataSuccess(Record[] records); 			
	public void onReadDataFailed(); 				
}
