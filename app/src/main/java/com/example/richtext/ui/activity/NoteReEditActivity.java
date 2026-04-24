package com.example.richtext.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

/** 再编辑便签界面  
 * 
 * @author RenHui
 */
public class NoteReEditActivity extends Activity implements View.OnClickListener {

	private static final String IMAGE_DEBUG_TAG = "RichEditorImageDiag";
	private static final int REQUEST_CODE_PICK_IMAGE = 1023;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;

	private static int SELECTED_INDEX = 0;

	private File mCurrentPhotoFile;// 照相机拍照得到的图片

	private String mNoteTitle;
	private String mNoteContent;
	private String mNoteNativeId;

	private EditText mTitleEditor;
	private LinearLayout mContentEditor;
	private RichEditView mEditView;

	private View mBtn1, mBtn2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNoteTitle = getIntent().getStringExtra("note_title");
		mNoteContent = getIntent().getStringExtra("note_content");
		mNoteNativeId = getIntent().getStringExtra("note_native_id");

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
			if (data == null || data.getData() == null) {
				ToastUtils.show("获取图片失败");
				Log.e(IMAGE_DEBUG_TAG, "onActivityResult PICK failed: data or uri is null");
				return;
			}
			Uri uri = data.getData();
			String imagePath = FileUtils.getUsableImagePath(this, uri);
			Log.d(IMAGE_DEBUG_TAG, "PICK result uri=" + uri + ", realPath=" + imagePath);
			setImageView(imagePath);
		} else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
			if (mCurrentPhotoFile == null) {
				ToastUtils.show("获取图片失败");
				Log.e(IMAGE_DEBUG_TAG, "onActivityResult CAMERA failed: mCurrentPhotoFile is null");
				return;
			}
			DebugTraceTool.debugTraceE(this, mCurrentPhotoFile.getAbsolutePath());
			Log.d(IMAGE_DEBUG_TAG, "CAMERA result file=" + mCurrentPhotoFile.getAbsolutePath()
					+ ", exists=" + mCurrentPhotoFile.exists() + ", length=" + mCurrentPhotoFile.length());
			setImageView(mCurrentPhotoFile.getAbsolutePath());
		}
	}

	private void setImageView(String imageUrl) {
		if (TextUtils.isEmpty(imageUrl) || "del".equals(imageUrl)) {
			Log.e(IMAGE_DEBUG_TAG, "setImageView skip: invalid imageUrl=" + imageUrl);
			return;
		}
		final int phoneWidth = getResources().getDisplayMetrics().widthPixels;
		final int targetWidth = (int) (phoneWidth * 0.75f);
		final Object model = toGlideModel(imageUrl);
		if (model == null) {
			ToastUtils.show("图片地址无效");
			Log.e(IMAGE_DEBUG_TAG, "setImageView failed: Glide model is null, imageUrl=" + imageUrl);
			return;
		}
		Log.d(IMAGE_DEBUG_TAG, "setImageView start, imageUrl=" + imageUrl + ", model=" + describeModel(model)
				+ ", targetWidth=" + targetWidth);
		Glide.with(this)
				.asBitmap()
				.load(model)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.override(targetWidth, Target.SIZE_ORIGINAL)
				.listener(new RequestListener<Bitmap>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
						Log.e(IMAGE_DEBUG_TAG, "Glide load failed. model=" + model + ", firstResource=" + isFirstResource);
						if (e != null) {
							Log.e(IMAGE_DEBUG_TAG, "Glide root causes:\n" + e.getRootCauses());
							Log.e(IMAGE_DEBUG_TAG, "Glide stacktrace", e);
						}
						return false;
					}

					@Override
					public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
						Log.d(IMAGE_DEBUG_TAG, "Glide load success. size=" + resource.getWidth() + "x"
								+ resource.getHeight() + ", dataSource=" + dataSource + ", model=" + model);
						return false;
					}
				})
				.into(new CustomTarget<Bitmap>() {
					@Override
					public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
						insertImageSpan(resource, imageUrl);
					}

					@Override
					public void onLoadCleared(@Nullable Drawable placeholder) {
						// no-op
					}

					@Override
					public void onLoadFailed(@Nullable Drawable errorDrawable) {
						super.onLoadFailed(errorDrawable);
						ToastUtils.show("图片解码失败");
						Log.e(IMAGE_DEBUG_TAG, "CustomTarget onLoadFailed, imageUrl=" + imageUrl
								+ ", model=" + describeModel(model));
					}
				});
	}

	private Object toGlideModel(String imageUrl) {
		if (TextUtils.isEmpty(imageUrl)) {
			return null;
		}
		if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
			return Uri.parse(imageUrl);
		}
		File file = new File(imageUrl);
		if (file.exists()) {
			return file;
		}
		return imageUrl;
	}

	private String describeModel(Object model) {
		if (model instanceof File) {
			File file = (File) model;
			return "File(path=" + file.getAbsolutePath() + ", exists=" + file.exists()
					+ ", canRead=" + file.canRead() + ", length=" + file.length() + ")";
		}
		if (model instanceof Uri) {
			Uri uri = (Uri) model;
			return "Uri(" + uri.toString() + ")";
		}
		return model.getClass().getSimpleName() + "(" + model + ")";
	}

	private void insertImageSpan(Bitmap img, String imageUrl) {
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

	public Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		Uri outputUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
		return intent;
	}

	protected void openCamera() {
		try {
			File photoDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
			if (photoDir == null) {
				ToastUtils.show("无法创建相机目录");
				Log.e(IMAGE_DEBUG_TAG, "openCamera failed: external files dir is null");
				return;
			}
			if (!photoDir.exists() && !photoDir.mkdirs()) {
				ToastUtils.show("无法创建相机目录");
				Log.e(IMAGE_DEBUG_TAG, "openCamera failed: mkdirs failed, path=" + photoDir.getAbsolutePath());
				return;
			}
			mCurrentPhotoFile = new File(photoDir, getPhotoFileName());// 给新照的照片文件命名
			Log.d(IMAGE_DEBUG_TAG, "openCamera output file=" + mCurrentPhotoFile.getAbsolutePath());
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
		} catch (ActivityNotFoundException e) {
			Log.e(IMAGE_DEBUG_TAG, "openCamera ActivityNotFoundException", e);
		} catch (Exception e) {
			Log.e(IMAGE_DEBUG_TAG, "openCamera exception", e);
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

				Note note = new Note();
				note.nativeId = mNoteNativeId;
				note.title = title;
				note.content = content;
				note.modifyTime = System.currentTimeMillis();
				boolean updated = DatabaseAccessFactory.getInstance(NoteReEditActivity.this)
						.noteAccessor()
						.updateByNativeId(note);
				if (!updated) {
					note.nativeId = String.valueOf(System.currentTimeMillis());
					note.createTime = System.currentTimeMillis();
					DatabaseAccessFactory.getInstance(NoteReEditActivity.this).noteAccessor().insert(note);
				}

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
