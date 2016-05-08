package com.carelink.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;

import com.carelink.model.Insulin;

public class InsulinDatabase {
	private static InsulinDatabase instance = null;
	private static ArrayList<Insulin> insulins;
	
	private static final String dataFileName = "insulin.txt";
	
	public static void init(Context context) {
		if (instance == null) {
			instance = new InsulinDatabase(context);
		}
	}
	
	private InsulinDatabase(Context context) {
		try {
			InputStreamReader inputStreamReader = 
					new InputStreamReader(context.getResources().getAssets().open(dataFileName));
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			ArrayList<String> insulinNames = new ArrayList<String>();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.equals("")) {
					insulinNames.add(line);
				}
			}
			Collections.sort(insulinNames);
			
			insulins = new ArrayList<Insulin>();
			for (String name : insulinNames) {
				insulins.add(new Insulin(name));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Insulin> getAll() {
		return insulins;
	}
}
