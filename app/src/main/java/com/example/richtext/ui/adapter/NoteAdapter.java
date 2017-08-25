package com.example.richtext.ui.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.ui.activity.NoteDetailActivity;
import com.example.richtext.utils.TimeUtils;

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
			convertView.setTag(new NoteViewHolder(convertView));
			
		}
		
		NoteViewHolder holder = (NoteViewHolder) convertView.getTag();
		holder.show(mNoteList.get(position));
		return convertView;
	}
	
	private class NoteViewHolder {
		
		private View mRootView;
		private TextView mTitleView;
		private TextView mTimeView;
		
		public NoteViewHolder(View rootView) {
			mRootView = rootView;
			mTitleView = (TextView) mRootView.findViewById(R.id.note_title);
			mTimeView = (TextView) mRootView.findViewById(R.id.time);
		}
		
		public void show(final Note note) {
			mTitleView.setText(note.title);
			mTimeView.setText(TimeUtils.getTime(note.modifyTime));
			mRootView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, NoteDetailActivity.class);
					intent.putExtra("note_title", note.title);
					//  TODO 还可以带其他参数
					intent.putExtra("note_content", note.content);
					mContext.startActivity(intent);
				}
			});
		}
	}

}