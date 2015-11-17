package com.example.richtext.ui.activity;

import java.util.ArrayList;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.ui.adapter.NoteAdapter;
import com.example.richtext.ui.widget.fab.FloatingActionButton;
import com.example.richtext.ui.widget.fab.FloatingActionsMenu;
import com.example.richtext.utils.DebugTraceTool;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class NoteActivity extends BaseActivity {

	private ListView mListView;
	private NoteAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.acitivity_note);
		mListView = (ListView) findViewById(R.id.note_list);

		ArrayList<Note> noteList = DatabaseAccessFactory.getInstance(this)
				.noteAccessor().getNotes();
		DebugTraceTool.debugTraceE(this, "数量" + noteList.size());
		mAdapter = new NoteAdapter(this, noteList);
		mListView.setAdapter(mAdapter);

		final View actionB = findViewById(R.id.action_b);

		FloatingActionButton actionC = new FloatingActionButton(
				getBaseContext());
		actionC.setTitle("Hide/Show Action above");
		actionC.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE
						: View.GONE);
			}
		});

		final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
		menuMultipleActions.addButton(actionC);

		ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
		drawable.getPaint().setColor(getResources().getColor(R.color.white));

		final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
		actionA.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				actionA.setTitle("Action A clicked");
			}
		});

	}
}
