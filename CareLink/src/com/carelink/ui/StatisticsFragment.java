package com.carelink.ui;

import com.carelink.R;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class StatisticsFragment extends Fragment {
	
	Fragment glucoseStatisticsFragment, heartParamsStatisticsFragment;
	Fragment weightStatisticsFragment, sportsStatisticsFragment;
	Fragment curFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		glucoseStatisticsFragment = new GlucoseStatisticsFragment();
		heartParamsStatisticsFragment = new HeartParamsStatisticsFragment();
		weightStatisticsFragment = new WeightStatisticsFragment();
		sportsStatisticsFragment = new SportsStatisticsFragment();
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragmentContainer_statistics, glucoseStatisticsFragment);
		fragmentTransaction.add(R.id.fragmentContainer_statistics, heartParamsStatisticsFragment);
		fragmentTransaction.add(R.id.fragmentContainer_statistics, weightStatisticsFragment);
		fragmentTransaction.add(R.id.fragmentContainer_statistics, sportsStatisticsFragment);
		fragmentTransaction.hide(glucoseStatisticsFragment);
		fragmentTransaction.hide(heartParamsStatisticsFragment);
		fragmentTransaction.hide(weightStatisticsFragment);
		fragmentTransaction.hide(sportsStatisticsFragment);
		fragmentTransaction.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_statistics, container, false);
		
		getFragmentManager().beginTransaction().show(curFragment = glucoseStatisticsFragment).commit();

		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_tabBar);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Fragment fragment = null;
				switch(checkedId){
				case R.id.tab_button_glucose:
					fragment = glucoseStatisticsFragment;
					break;
				case R.id.tab_button_heart_params:
					fragment = heartParamsStatisticsFragment;
					break;
				case R.id.tab_button_weight:
					fragment = weightStatisticsFragment;
					break;
				case R.id.tab_button_sports:
					fragment = sportsStatisticsFragment;
					break;
				} 
				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					fragmentTransaction.hide(curFragment);
					fragmentTransaction.show(fragment);
					fragmentTransaction.commit();
					curFragment = fragment;
				}
			}
		});
		return view;
	}

}
