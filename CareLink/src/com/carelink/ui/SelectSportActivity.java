package com.carelink.ui;

import com.carelink.R;
import com.carelink.model.SportRecord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectSportActivity extends MyActivity {
	private int selectedItem = -1;
	private View selectedItemView;

	private final int[] itemResIds = {
		R.id.item_run_fast,
		R.id.item_run_slowly,
		R.id.item_walk_fast,
		R.id.item_walk_slowly,
		R.id.item_dancing,
		R.id.item_swimming,
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_select_sport);
		setTitleTextResource(R.string.title_activity_select_sport);
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			int sportIndex = bundle.getInt(UIConstants.EXTRA_NAME_SPORT_INDEX);
			if (sportIndex != -1) {
				selectedItem = sportIndex;
			}
		}
				
		OnClickListener onClickListener = new OnClickListener() {
			public void onClick(View v) {
				if (selectedItemView != null) {
					selectedItemView.findViewById(R.id.imageView_selected).setVisibility(View.GONE);
					((TextView)selectedItemView.findViewById(R.id.textView_sportItem))
						.setTextColor(getResources().getColor(R.color.default_text_color));
				}
				selectedItemView = v;
				if (selectedItemView != null) {
					selectedItemView.findViewById(R.id.imageView_selected).setVisibility(View.VISIBLE);
					((TextView)selectedItemView.findViewById(R.id.textView_sportItem))
						.setTextColor(getResources().getColor(R.color.theme));
				}
				
				for (int i = 0; i < itemResIds.length; i++) {
					if (v.getId() == itemResIds[i]) {
						selectedItem = i;
						break;
					}
				}
			}
		};
		
		for (int i = 0; i < itemResIds.length; i++) {
			View itemView = findViewById(itemResIds[i]);
			itemView.setOnClickListener(onClickListener);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView_icon);
			imageView.setImageResource(SportRecord.getSportIconResId(i));
			TextView textView = (TextView)itemView.findViewById(R.id.textView_sportItem);
			textView.setText(SportRecord.getSportNameResId(i));
			
			if (selectedItem == i) {
				selectedItemView = itemView;
				itemView.findViewById(R.id.imageView_selected).setVisibility(View.VISIBLE);
				textView.setTextColor(getResources().getColor(R.color.theme));
			}
		}
		
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(UIConstants.EXTRA_NAME_SPORT_INDEX, selectedItem);
				SelectSportActivity.this.setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
}
