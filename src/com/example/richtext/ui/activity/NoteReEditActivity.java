package com.example.richtext.ui.activity;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.ui.widget.RichEditView;
import com.example.richtext.utils.ToastUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 再编辑便签界面   */
public class NoteReEditActivity extends Activity {
	
	private String mNoteTitle;
	private String mNoteContent;
	
	private EditText mTitleEditor;
	private LinearLayout mContentEditor;
	private RichEditView mEditView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mNoteTitle = getIntent().getStringExtra("note_title");
		mNoteContent = getIntent().getStringExtra("note_content");
		
		setContentView(R.layout.activity_note_reedit);
		
		mTitleEditor = (EditText) findViewById(R.id.note_title);
		mContentEditor = (LinearLayout) findViewById(R.id.note_content);
		
		// 初始化内容
		mTitleEditor.setText(mNoteTitle);
		mEditView = new RichEditView(this, mNoteContent);
		mContentEditor.addView(mEditView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_note_menu, menu);
		View view = menu.findItem(R.id.item_save_note).getActionView();
		TextView tv = (TextView) view.findViewById(R.id.save_note);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToastUtils.show("保存");
				
				String title = mTitleEditor.getEditableText().toString();
				if (title == null || title.isEmpty() || TextUtils.isEmpty(title)) {
					ToastUtils.show(R.string.title_empty);
					return;
				}

				String content = mEditView.getEditableTextString();
				if (content == null || content.isEmpty() || TextUtils.isEmpty(content)) {
					ToastUtils.show(R.string.content_empty);
					return;
				}

				// 目前不做编辑覆盖数据库的操作 --- 采用插入操作  // TODO 后续做更新数据库操作
				Note note = new Note();
				note.nativeId = String.valueOf(System.currentTimeMillis());
				note.title = title;
				note.content = content;
				note.createTime = System.currentTimeMillis();
				note.modifyTime = System.currentTimeMillis();
				DatabaseAccessFactory.getInstance(NoteReEditActivity.this).noteAccessor().insert(note);
				
				ToastUtils.show(R.string.note_saved);
				// 跳转
				Intent intent = new Intent(NoteReEditActivity.this, NoteActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
}
