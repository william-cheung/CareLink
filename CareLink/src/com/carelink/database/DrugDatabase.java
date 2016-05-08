package com.carelink.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;

import com.carelink.model.Drug;

public class DrugDatabase {
	private static DrugDatabase instance = null;
	private static ArrayList<Drug> drugs;
	
	private static final String dataFileName = "drugs.txt";
	
	public static void init(Context context) {
		if (instance == null) {
			instance = new DrugDatabase(context);
		}
	}
	
	private DrugDatabase(Context context) {
		try {
			InputStreamReader inputStreamReader = 
					new InputStreamReader(context.getResources().getAssets().open(dataFileName));
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			ArrayList<String> drugNames = new ArrayList<String>();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.equals("")) {
					drugNames.add(line);
				}
			}
			Collections.sort(drugNames);
			
			drugs = new ArrayList<Drug>();
			for (String name : drugNames) {
				drugs.add(new Drug(name));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Drug> getAll() {
		return drugs;
	}
}
