package com.carelink.model;

import com.google.gson.Gson;

import android.util.Log;

public class VersionInfo {
	private String versionName = "";
	private String description = "";
	private String apkUrl = "";
	
	public VersionInfo() {
	}
	
	public String getVersionName() {
		return versionName;
	}
	
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getApkUrl() {
		return apkUrl;
	}
	
	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}
	
	/**
	 * The format of version name MUST be : x.x.x.x, or the result is undefined.
	 */
	public static int compare(String version1, String version2) {
		String[] vs1 = version1.split("\\.");
		String[] vs2 = version2.split("\\.");
		Log.d("VersionInfo", new Gson().toJson(vs1));
		Log.d("VersionInfo", new Gson().toJson(vs2));
		int i = 0;
		while (i < vs1.length && i < vs2.length) {
			int x = Integer.parseInt(vs1[i]);
			int y = Integer.parseInt(vs2[i]);
			if (x != y) {
				return x - y;
			} else {
				i++;
			}
		}
		return 0;
	}
}
