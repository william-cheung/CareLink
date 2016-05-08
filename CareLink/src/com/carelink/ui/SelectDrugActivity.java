package com.carelink.ui;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.carelink.R;
import com.carelink.database.DrugDatabase;
import com.carelink.model.Drug;

public class SelectDrugActivity extends MyActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setTitleTextResource(R.string.title_activity_select_drug);
		setContentViewResource(R.layout.activity_select_drug);
		super.onCreate(savedInstanceState);
		
		ArrayList<Drug> drugs = DrugDatabase.getAll();
		final ArrayList<ListItem> listItems = new ArrayList<SelectDrugActivity.ListItem>();
		for (Drug drug : drugs) {
			listItems.add(new ListItem(drug));
		}
		ListAdapter listAdapter = new ListAdapter(getApplicationContext(), listItems);
		ListView listView = (ListView) findViewById(R.id.listView_drugs);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra(UIConstants.EXTRA_NAME_DRUG, listItems.get(position).getDrug());
				SelectDrugActivity.this.setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	private class ListItem {
		private Drug drug;
		public ListItem(Drug drug) {
			this.drug = drug;
		}
		public Drug getDrug() {
			return drug;
		}
	}

	private class ListAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<ListItem> items;

		public ListAdapter(Context context, ArrayList<ListItem> items) {
			super();
			this.context = context;
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "SimpleDateFormat", "InflateParams" }) @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater)
						context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.drug_list_item, null);
			}
			ListItem item = items.get(position);
			TextView nameTextView = (TextView) convertView.findViewById(R.id.textView_name);
			nameTextView.setText(item.getDrug().getName());
			return convertView;
		}
	}
}
