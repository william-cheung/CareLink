package com.carelink.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.carelink.model.VersionInfo;
import com.carelink.util.VersionInfoParser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class UpdateAppTask extends AsyncTask<Void, Object, Void> {
	
	private Context context;
	private UpdateAppCallback callback;
	private VersionInfo versionInfo;
	private DownloadAppTask downloadAppTask;
	
	public UpdateAppTask(Context context, UpdateAppCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Void doInBackground(Void... params) {
		publishProgress(0);
		//callback.onCheckUpdate();
		try {
			URL url = new URL("http://219.223.189.239/~william/CareLink/version.xml");
			HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(2000);
			
			InputStream inputStream = connection.getInputStream();
			versionInfo = VersionInfoParser.getVersionInfo(inputStream);
			if (versionInfo != null) {
				Log.d(getClass().getSimpleName(), versionInfo.getVersionName() + " vs " + MyApplication.getVersionName());
				if (VersionInfo.compare(versionInfo.getVersionName(), MyApplication.getVersionName()) > 0) {
					Log.d(getClass().getSimpleName(), "New Version Found! ");
					publishProgress(2, versionInfo);
					//callback.onUpdateFound(versionInfo);
				} else {
					publishProgress(3);
					//callback.onNoUpdateFound();
				}
				
			} else {
				Log.d(getClass().getSimpleName(), "version info: null");
				publishProgress(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			publishProgress(1);
		}
		return null;
	}
	
	public void startDownloading() {
		if (versionInfo != null) {
			downloadAppTask = new DownloadAppTask();
			downloadAppTask.execute(versionInfo.getApkUrl());
		} 
	}
	
	public void stopDownloading() {
		if (downloadAppTask != null) {
			downloadAppTask.cancel(true);
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		int proc = (Integer)values[0];
		switch (proc) {
		case 0:
			callback.onCheckUpdate();
			break;
		case 1:
			callback.onCheckUpdateError();
			break;
		case 2:
			callback.onUpdateFound((VersionInfo) values[1]);
			break;
		case 3:
			callback.onNoUpdateFound();
			break;
		default:
			break;
		}
	}
	
	
	private class DownloadAppTask extends AsyncTask<String, Object, Void> {
		@Override
		protected Void doInBackground(String... params) {
			String apkUrl = params[0];
			downloadApk(apkUrl);
			return null;
		}
		
		private void downloadApk(String apkUrl) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				try {
					URL url = new URL(apkUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(2000);
					
					int fileSize = connection.getContentLength();
					InputStream inputStream = connection.getInputStream();
					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
					File file = new File(Environment.getExternalStorageDirectory(), "Download/CareLink.apk");
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					
					byte[] buffer = new byte[1024]; // 1K
					int len, cur = 0;
					while ((len = bufferedInputStream.read(buffer)) != -1) {
						fileOutputStream.write(buffer, 0, len);
						cur += len;
						publishProgress(1, (int)(cur * 100.0 / fileSize));
						// callback.onDowloading();
					}
					
					fileOutputStream.close();
					bufferedInputStream.close();
					inputStream.close();
					
					publishProgress(2, file);
					// callback.onCompleteDownloading(file);
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(0);
					// callback.onDownloadApkError();
				}
			}
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			int proc = (Integer)values[0];
			switch (proc) {
			case 0:
				callback.onDownloadApkError();
				break;
			case 1:
				callback.onDowloading((Integer) values[1]);
				break;
			case 2:
				callback.onCompleteDownloading((File) values[1]);
				break;
			default:
				break;
			}
		}
	}
	
	public interface UpdateAppCallback {
		public void onCheckUpdate();
		public void onCheckUpdateError();
		public void onNoUpdateFound(); 		
		public void onUpdateFound(VersionInfo versionInfo); 
		public void onDownloadApkError();
		public void onDowloading(int percent);
		public void onCompleteDownloading(File apkFile); 	 				
	}
	
	public void installApk(File apkFile) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
}


