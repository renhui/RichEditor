package com.example.richtext.ui.activity;

import java.util.ArrayList;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.ui.adapter.NoteAdapter;
import com.example.richtext.ui.widget.fab.FloatingActionButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class NoteActivity extends BaseActivity {

	private FloatingActionButton mActionBtn;
	private ListView mListView;
	private NoteAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.acitivity_note);
		mListView = (ListView) findViewById(R.id.note_list);
		mActionBtn = (FloatingActionButton) findViewById(R.id.action_a);
		
		mActionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(NoteActivity.this, EditNoteActivity.class);
				intent.putExtra("next_page_title", "新建便签");
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		ArrayList<Note> noteList = DatabaseAccessFactory.getInstance(this).noteAccessor().getNotes();
		mAdapter = new NoteAdapter(this, noteList);
		mListView.setAdapter(mAdapter);

	}
}
