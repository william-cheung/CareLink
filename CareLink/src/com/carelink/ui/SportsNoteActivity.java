package com.carelink.ui;

import java.util.ArrayList;

import com.carelink.R;

import android.os.Bundle;
import android.widget.TextView;

public class SportsNoteActivity extends NoteActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setCustomTitleResource(R.layout.title_bar_default_back);
		setContentViewResource(R.layout.activity_sports_note);
		setTitleTextResource(R.string.title_activity_sports_note);
		super.onCreate(savedInstanceState);

		final ArrayList<TextView> notes = new ArrayList<TextView>();
		notes.add((TextView) findViewById(R.id.button_note0));
		notes.add((TextView) findViewById(R.id.button_note1));
		notes.add((TextView) findViewById(R.id.button_note2));
		notes.add((TextView) findViewById(R.id.button_note3));
		initTextViews(notes);
	}
}
