package com.carelink.ui;

import java.util.ArrayList;

import com.carelink.R;
import android.os.Bundle;
import android.widget.TextView;


public class DrugsNoteActivity extends NoteActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_drugs_note);
		setTitleTextResource(R.string.title_activity_drugs_note);
		super.onCreate(savedInstanceState);
		
		final ArrayList<TextView> noteTextViews = new ArrayList<TextView>();
		noteTextViews.add((TextView) findViewById(R.id.button_note0));
		noteTextViews.add((TextView) findViewById(R.id.button_note1));
		initTextViews(noteTextViews);
	}
}
