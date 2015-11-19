package com.example.richtext.ui.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.utils.TimeUtils;
import com.example.richtext.utils.ToastUtils;

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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.view_note, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.note_title);
		title.setText(mNoteList.get(position).content);
		
		TextView time = (TextView) convertView.findViewById(R.id.time);
		time.setText(TimeUtils.getTime(mNoteList.get(position).modifyTime));
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ToastUtils.show(mNoteList.get(position).content);
			}
		});
		return convertView;
	}

}