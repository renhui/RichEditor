package com.example.richtext.ui.activity;

import java.util.ArrayList;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.ui.adapter.NoteAdapter;
import com.example.richtext.utils.DebugTraceTool;

import android.os.Bundle;
import android.widget.ListView;

public class NoteActivity extends BaseActivity {

	private ListView mListView;
	private NoteAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.acitivity_note);
		mListView  = (ListView) findViewById(R.id.note_list);

		ArrayList<Note> noteList = DatabaseAccessFactory.getInstance(this).noteAccessor().getNotes();
		DebugTraceTool.debugTraceE(this, "数量"+ noteList.size());
		mAdapter = new NoteAdapter(this, noteList);
		mListView.setAdapter(mAdapter);
	}
}
