package com.example.richtext.ui.activity;

import com.example.richtext.R;
import com.example.richtext.ui.widget.MixedTextView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class NoteDetailActivity extends BaseActivity {
	private String mNoteTitle;
	private String mNoteContent;
	private LinearLayout mNoteDetailContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_detail);
		
		Intent intent = getIntent();
		mNoteTitle = intent.getStringExtra("note_title");
		mNoteContent = intent.getStringExtra("note_content");
		
		mNoteDetailContent = (LinearLayout) findViewById(R.id.note_detail_content);
		mNoteDetailContent.addView(new MixedTextView(this, mNoteContent));
	}
}
