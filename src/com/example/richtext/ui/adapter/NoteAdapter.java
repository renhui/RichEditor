package com.example.richtext.ui.adapter;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.richtext.R;
import com.example.richtext.moudle.Note;

public class NoteAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Note> mNoteList;

	public NoteAdapter(Context context, ArrayList<Note> notes) {
		super();
		mContext = context;
		mNoteList = notes;
	}

	@Override
	public int getCount() {
		return mNoteList.size();
	}

	@Override
	public Object getItem(int position) {
		return mNoteList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.view_note, null);
		}

		TextView view = (TextView) convertView.findViewById(R.id.content);
		view.setText(mNoteList.get(position).content);
		return convertView;
	}

}