package com.example.richtext.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.richtext.R;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.ui.widget.RichEditView;
import com.example.richtext.utils.DebugTraceTool;
import com.example.richtext.utils.FileUtils;
import com.example.richtext.utils.ImageUtils;
import com.example.richtext.utils.ToastUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 再编辑便签界面  
 * 
 * @author RenHui
 */
public class NoteReEditActivity extends Activity implements View.OnClickListener {

	private static final int REQUEST_CODE_PICK_IMAGE = 1023;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;
	private static final File PHOTO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/DCIM/Camera");

	private static int SELECTED_INDEX = 0;

	private File mCurrentPhotoFile;// 照相机拍照得到的图片

	private String mNoteTitle;
	private String mNoteContent;

	private EditText mTitleEditor;
	private LinearLayout mContentEditor;
	private RichEditView mEditView;

	private View mBtn1, mBtn2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNoteTitle = getIntent().getStringExtra("note_title");
		mNoteContent = getIntent().getStringExtra("note_content");

		setContentView(R.layout.activity_note_reedit);
		mTitleEditor = (EditText) findViewById(R.id.note_title);
		mContentEditor = (LinearLayout) findViewById(R.id.note_content);
		mBtn1 = findViewById(R.id.button1);
		mBtn2 = findViewById(R.id.button2);
		

		mTitleEditor.setText(mNoteTitle);
		mEditView = new RichEditView(this, mNoteContent);
		mContentEditor.addView(mEditView);
		
		mBtn1.setOnClickListener(this);
		mBtn2.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			hideKeyBoard();
			if (mEditView.getSelectionStart() == -1) {
				SELECTED_INDEX = 0;
			} else {
				SELECTED_INDEX = mEditView.getSelectionStart();
			}
			// 打开系统相册
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");// 相片类型
			startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
			break;
			
		case R.id.button2:
			hideKeyBoard();
			if (mEditView.getSelectionStart() == -1) {
				SELECTED_INDEX = 0;
			} else {
				SELECTED_INDEX = mEditView.getSelectionStart();
			}
			// 打开相机
			openCamera();
			break;

		default:
			break;
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_CODE_PICK_IMAGE) {
			Uri uri = data.getData();
			setImageView(FileUtils.getRealFilePath(this, uri));
		} else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
			DebugTraceTool.debugTraceE(this, mCurrentPhotoFile.getAbsolutePath());
			setImageView(mCurrentPhotoFile.getAbsolutePath());
		}
	}

	private void setImageView(String imageUrl) {
		if (!TextUtils.isEmpty(imageUrl) && !"del".equals(imageUrl)) {
			final int phone_width = getResources().getDisplayMetrics().widthPixels;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imageUrl, options);
			if (phone_width > 480) {
				int inSampleSize = ImageUtils.reckonInSampleSizeForBig(options, (int) (phone_width * 0.75));
				options.inSampleSize = inSampleSize;
			} else {
				int inSampleSize = ImageUtils.reckonInSampleSizeForSmall(options, (int) (phone_width * 0.75));
				options.inSampleSize = inSampleSize;
			}

			options.inJustDecodeBounds = false;
			Bitmap img = BitmapFactory.decodeFile(imageUrl, options);
			ImageSpan urlSpan = new ImageSpan(ImageUtils.convert(this, img), imageUrl);
			SpannableString string = new SpannableString(" ");
			string.setSpan(urlSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			Editable editable = mEditView.getEditableText();
			if (SELECTED_INDEX == 0) {
				editable.insert(SELECTED_INDEX, Html.fromHtml("<br>"));
				editable.insert(SELECTED_INDEX, string);
			} else if (SELECTED_INDEX == editable.length() && SELECTED_INDEX != 0) {
				editable.insert(SELECTED_INDEX, string);
				editable.insert(SELECTED_INDEX, Html.fromHtml("<br>"));
			} else {
				editable.insert(SELECTED_INDEX, Html.fromHtml("<br>"));
				editable.insert(SELECTED_INDEX, string);
				editable.insert(SELECTED_INDEX, Html.fromHtml("<br>"));
			}
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	protected void openCamera() {
		try {
			PHOTO_DIR.mkdirs();// 创建照片的存储目录
			mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/** 用当前时间给取得的图片命名 */
	@SuppressLint("SimpleDateFormat")
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyy_MM_dd_HH_mm_ss");
		return dateFormat.format(date) + ".jpg";
	}

	/** 隐藏软键盘 */
	public void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromInputMethod(mTitleEditor.getWindowToken(), 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_note_menu, menu);
		View view = menu.findItem(R.id.item_save_note).getActionView();
		TextView tv = (TextView) view.findViewById(R.id.save_note);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
				
				DebugTraceTool.debugTraceE(this, content);

				// 目前不做编辑覆盖数据库的操作 --- 采用插入操作 // TODO 后续做更新数据库操作
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
