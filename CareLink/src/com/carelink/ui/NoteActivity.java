package com.carelink.ui;

import java.util.ArrayList;

import com.carelink.R;
import com.carelink.model.Note;
import com.carelink.util.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class NoteActivity extends MyActivity {
	private String tags = "";
	private String text = "";
	private ArrayList<TextView> noteTextViews;
	
	private boolean[] noteSelected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			Note note = (Note) bundle.get(UIConstants.EXTRA_NAME_NOTE);
			if (note != null) {
				tags = note.getTags();
				text = note.getText();
			}
		}

		final EditText editText = (EditText) findViewById(R.id.editText);
		editText.setText(text);
		editText.setSelection(text.length());
		findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tags = "";
				for (int i = 0; i < noteSelected.length; i++) {
					if (noteSelected[i] == true) {
						tags += "" + i + " ";
					}
				}
				if (!tags.equals("")) {
					tags = tags.substring(0, tags.length()-1);
				}
				text = editText.getText().toString();
				Intent intent = new Intent();
				intent.putExtra(UIConstants.EXTRA_NAME_NOTE, new Note(tags, text));
				
				NoteActivity.this.setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	protected void initTextViews(ArrayList<TextView> textViews) {
		noteTextViews = textViews;
		noteSelected = new boolean[noteTextViews.size()];
		ArrayList<Integer> indices = Utils.parseIntegerList(tags);
		for (Integer index : indices) {
			noteSelected[index] = true;
			TextView noteTextView = noteTextViews.get(index);
			noteTextView.setBackgroundResource(R.drawable.tag_bg_selected);
			noteTextView.setTextColor(getResources().getColor(R.color.white));
		}

		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				for (int i = 0; i < noteTextViews.size(); i++) {
					TextView note = noteTextViews.get(i);
					if (v.getId() == note.getId()) {
						if (noteSelected[i]) {
							noteSelected[i] = false; 
							note.setBackgroundResource(R.drawable.tag_bg_normal);
							note.setTextColor(getResources().getColor(R.color.theme));
						} else {
							noteSelected[i] = true; 
							note.setBackgroundResource(R.drawable.tag_bg_selected);
							note.setTextColor(getResources().getColor(R.color.white));
						}
					}
				}
			}
		};
		for (TextView note : noteTextViews) {
			note.setOnClickListener(listener);
		}
	}
}
