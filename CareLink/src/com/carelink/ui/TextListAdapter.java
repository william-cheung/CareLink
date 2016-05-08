package com.carelink.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.carelink.R;


/**
 * TextListAdapter along with TextListItem is designed for List of Name Strings in an alert dialog 
 * <br />
 * Note: there <b>MUST</b> be an TextView whose id is <b>R.id.textView</b> in <b>itemLayoutRes</b>  
 * @author william
 *
 */
public class TextListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<TextListItem> items;
	private int itemLayoutRes;

	public TextListAdapter(Context context, ArrayList<TextListItem> items, int itemLayoutRes) {
		super();
		this.context = context;
		this.items = items;
		this.itemLayoutRes = itemLayoutRes;
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

	@SuppressLint("InflateParams") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)
					context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(itemLayoutRes, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.textView);
		TextListItem item = items.get(position);
		textView.setText(item.getText());
		return convertView;
	}
}
