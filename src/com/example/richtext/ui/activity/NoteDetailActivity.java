package com.example.richtext.ui.activity;

import com.example.richtext.R;
import com.example.richtext.ui.widget.RichTextView;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NoteDetailActivity extends BaseActivity {
	private String mNoteTitle;
	private String mNoteContent;
	private ActionBar mActionBar;
	private TextView mActionBarTitle;
	private LinearLayout mNoteDetailContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_detail);

		Intent intent = getIntent();
		mNoteTitle = intent.getStringExtra("note_title");
		mNoteContent = intent.getStringExtra("note_content");

		setUpActionBar();

		mNoteDetailContent = (LinearLayout) findViewById(R.id.note_detail_content);
		mNoteDetailContent.addView(new RichTextView(this, mNoteContent));
	}

	/** 添加ActionBar */
	private void setUpActionBar() {
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setHomeButtonEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(false);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBarTitle = new TextView(this, null);
		mActionBarTitle.setId(R.id.actionbar_finish);
		mActionBarTitle.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.img_title_back, 0, 0, 0);
		mActionBarTitle.setMaxLines(2);
		mActionBarTitle.setEllipsize(TruncateAt.END);
		mActionBarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		mActionBarTitle.setTextColor(getResources().getColor(R.color.pink));
		mActionBarTitle.setGravity(Gravity.CENTER_VERTICAL);
		mActionBarTitle.setClickable(true);
		mActionBarTitle.setPadding(5, 0, 32, 0);
		mActionBarTitle.setText(mNoteTitle);
		mActionBarTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mActionBar.setCustomView(mActionBarTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_note_menu, menu);
		View view = menu.findItem(R.id.item_save_note).getActionView();
		TextView tv = (TextView) view.findViewById(R.id.save_note);
		tv.setText("编辑");
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NoteDetailActivity.this, NoteReEditActivity.class);
				intent.putExtra("note_title", mNoteTitle);
				intent.putExtra("note_content", mNoteContent);
				startActivity(intent);
				finish();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
}
