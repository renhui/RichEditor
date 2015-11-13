package com.example.richtext.ui.activity;

import java.util.ArrayList;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NoteActivity extends BaseActivity {

	private ListView mListView;
	private NoteAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.acitivity_note);
		mListView  = (ListView) findViewById(R.id.note_list);
		
		ArrayList<Note> noteList = new ArrayList<Note>();
		Note note = new Note();
		note.content = "哈哈";
		noteList.add(note);
		mAdapter = new NoteAdapter(noteList);
		mListView.setAdapter(mAdapter);
	}

	class NoteAdapter extends BaseAdapter {

		private ArrayList<Note> mNoteList;

		public NoteAdapter(ArrayList<Note> notes) {
			super();
			mNoteList = notes;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return mNoteList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.view_note,
						null);
			}

			TextView view = (TextView) convertView.findViewById(R.id.content);
			view.setText("测试的...哈哈哈");

			return null;
		}

	}

}
