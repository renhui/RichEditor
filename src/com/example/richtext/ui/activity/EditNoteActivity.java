package com.example.richtext.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.richtext.R;
import com.example.richtext.imageloader.core.DisplayImageOptions;
import com.example.richtext.imageloader.core.ImageLoader;
import com.example.richtext.imageloader.core.assist.ImageScaleType;
import com.example.richtext.imageloader.core.assist.ImageSize;
import com.example.richtext.moudle.EditData;
import com.example.richtext.moudle.Note;
import com.example.richtext.sqlite.DatabaseAccessFactory;
import com.example.richtext.sqlite.tables.NoteAccessor;
import com.example.richtext.ui.widget.RichEditor;
import com.example.richtext.ui.widget.handwriting.WritePadDialog;
import com.example.richtext.ui.widget.handwriting.listener.WriteDialogListener;
import com.example.richtext.utils.DebugTraceTool;
import com.example.richtext.utils.ImageUtils;
import com.example.richtext.utils.LongBlogContent;
import com.example.richtext.utils.ToastUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 编辑Note页面
 * 
 * @author RenHui
 * 
 */
@SuppressLint("SimpleDateFormat")
public class EditNoteActivity extends BaseActivity {
	private static final int LONG_BLOG_WIDTH = 440; // 长微博最佳宽度

	private static final int REQUEST_CODE_PICK_IMAGE = 1023;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;

	private static final File PHOTO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/DCIM/Camera");

	private String mPageTitle;
	
	private String mNoteTitle;
	private String mNoteContent;
	private boolean mIsToEdit;

	private ActionBar mActionBar;
	private TextView mActionBarTitle;
	private EditText mTitleEditor;
	private RichEditor mContentEditor;
	private View mBtn1, mBtn2, mBtn3;
	private OnClickListener mBtnListener;

	private File mCurrentPhotoFile;// 照相机拍照得到的图片

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_note);

		Intent intent = getIntent();
		mPageTitle = intent.getStringExtra("next_page_title");
		mNoteTitle = intent.getStringExtra("note_title");
		mNoteContent = intent.getStringExtra("note_content");
		mIsToEdit =intent.getBooleanExtra("to_edit", false);
		
		setUpActionBar();

		mTitleEditor = (EditText) findViewById(R.id.editor_title);
		mContentEditor = (RichEditor) findViewById(R.id.richEditor);
		mBtnListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mContentEditor.hideKeyBoard();
				if (v.getId() == mBtn1.getId()) {
					// 打开系统相册
					Intent intent = new Intent(Intent.ACTION_PICK);
					intent.setType("image/*");// 相片类型
					startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
				} else if (v.getId() == mBtn2.getId()) {
					// 打开相机
					openCamera();
				} else if (v.getId() == mBtn3.getId()) {
//					 生成长微博
//					newLongBlog();
					// 手画板
					WritePadDialog mWritePadDialog = new WritePadDialog(EditNoteActivity.this, 
						new WriteDialogListener() {
							
							@Override
							public void onPaintDone(Object object) {
								Bitmap bitmap = (Bitmap) object;
								String path = ImageUtils.createSignFile(bitmap);
								insertBitmap(path);
							}
						});
					mWritePadDialog.show();
				}
			}
		};

		mBtn1 = findViewById(R.id.button1);
		mBtn2 = findViewById(R.id.button2);
		mBtn3 = findViewById(R.id.button3);

		mBtn1.setOnClickListener(mBtnListener);
		mBtn2.setOnClickListener(mBtnListener);
		mBtn3.setOnClickListener(mBtnListener);
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
		mActionBarTitle.setText(mPageTitle);
		mActionBarTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mActionBar.setCustomView(mActionBarTitle);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
    	if (mIsToEdit) {
			mTitleEditor.setText(mNoteTitle);
		}
		
	}

	@Override
	protected void onStop() {
		super.onStop();
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

				List<EditData> editList = mContentEditor.buildEditData();
				String content = dealEditData(editList);
				if (content == null || content.isEmpty() || TextUtils.isEmpty(content)) {
					ToastUtils.show(R.string.content_empty);
					return;
				}

				Note note = new Note();
				note.nativeId = String.valueOf(System.currentTimeMillis());
				note.title = title;
				note.content = content;
				note.createTime = System.currentTimeMillis();
				note.modifyTime = System.currentTimeMillis();
				DatabaseAccessFactory.getInstance(EditNoteActivity.this).noteAccessor().insert(note);
				
				ToastUtils.show(R.string.note_saved);
				
				// 隐藏软键盘
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mActionBarTitle.getWindowToken(), 0);
				// 跳转
				Intent intent = new Intent(EditNoteActivity.this,
						NoteActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	protected void openCamera() {
		try {
			// Launch camera to take photo for selected contact
			PHOTO_DIR.mkdirs();// 创建照片的存储目录
			mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
		} catch (ActivityNotFoundException e) {
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/** 用当前时间给取得的图片命名 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date) + ".jpg";
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_CODE_PICK_IMAGE) {
			Uri uri = data.getData();
			insertBitmap(getRealFilePath(uri));
		} else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
			insertBitmap(mCurrentPhotoFile.getAbsolutePath());
		}
	}

	/**
	 * 添加图片到富文本剪辑器
	 * 
	 * @param imagePath
	 */
	private void insertBitmap(String imagePath) {
		mContentEditor.insertImage(imagePath);
	}

	/**
	 * 根据Uri获取图片文件的绝对路径
	 */
	private String getRealFilePath(final Uri uri) {
		if (null == uri) {
			return null;
		}

		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/** 转换编辑框内的所有内容为字符串 */
	private String dealEditData(List<EditData> editList) {
		String data = "";
		for (EditData itemData : editList) {
			if (itemData.inputStr != null) {
				data += itemData.inputStr;
			} else if (itemData.imagePath != null) {
				data += itemData.imagePath;
			}
		}
		return data;
	}

	/** 生成长微博 */
	private void newLongBlog() {
		List<EditData> editList = mContentEditor.buildEditData();

		// 存储到数据库
		String content = dealEditData(editList);
		if (TextUtils.isEmpty(content)) {
			return;
		} else {
			Note note = new Note();
			note.content = content;
			note.modifyTime = System.currentTimeMillis();
			note.nativeId = String.valueOf(System.currentTimeMillis());
			NoteAccessor accessor = DatabaseAccessFactory.getInstance(
					EditNoteActivity.this).noteAccessor();
			accessor.insert(note);
		}

		// 生成长微博图片
		int fontSize = 30; // 字体大小 目前先自己设定18sp
		int wordNum = (LONG_BLOG_WIDTH / (fontSize)) - 1; // 转化成图片的时，每行显示的字数

		float canvasHeight = (float) (fontSize * 0.8); // 画布的高度
		int x = 10;
		float y = (float) (fontSize * 0.8); // 开始画的起始位置

		for (EditData itemData : editList) {
			if (itemData.inputStr != null) {
				LongBlogContent ct = LongBlogContent.getInstance();
				ct.clearStatus();
				ct.handleText(itemData.inputStr, wordNum);
				canvasHeight += 35 * (ct.getHeight() + 1);
			} else if (itemData.imagePath != null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(itemData.imagePath, options);
				canvasHeight += (float) options.outHeight
						/ ((float) options.outWidth / (float) LONG_BLOG_WIDTH);
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(LONG_BLOG_WIDTH,
				(int) canvasHeight, Config.ARGB_8888);
		// 创建画布
		Canvas canvas = new Canvas(bitmap);
		// 设置画布背景颜色
		canvas.drawARGB(255, 255, 255, 255);

		for (EditData itemData : editList) {
			if (itemData.inputStr != null) {
				LongBlogContent ct = LongBlogContent.getInstance();
				ct.clearStatus();
				ct.handleText(itemData.inputStr, wordNum);

				// 创建画笔
				Paint paint = new Paint();
				// 通过画笔设置字体的大小、格式、颜色
				paint.setTextSize(fontSize);
				paint.setARGB(255, 0, 0, 0);
				y = y + 10;
				// 将处理后的内容画到画布上
				String[] ss = ct.getContent();
				for (int i = 0; i < ct.getHeight(); i++) {
					canvas.drawText(ss[i], x, y, paint);
					y = y + 35;
				}
				canvas.save(Canvas.ALL_SAVE_FLAG);
			} else if (itemData.imagePath != null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(itemData.imagePath, options);
				DisplayImageOptions opt = new DisplayImageOptions.Builder()
						.imageScaleType(ImageScaleType.EXACTLY).build();
				Bitmap b = ImageLoader
						.getInstance()
						.loadImageSync(
								"file://" + itemData.imagePath,
								new ImageSize(
										LONG_BLOG_WIDTH,
										(int) ((float) options.outHeight / ((float) options.outWidth / (float) LONG_BLOG_WIDTH))),
								opt);
				canvas.drawBitmap(b, 0, y, null);
				canvas.save(Canvas.ALL_SAVE_FLAG);
				y += ((float) options.outHeight / ((float) options.outWidth / (float) LONG_BLOG_WIDTH)) + 35;
			}
		}
		canvas.restore();

		File sd = Environment.getExternalStorageDirectory();
		String fpath = sd.getPath() + "/EasyChangWeibo";

		// 设置保存路径
		String path = sd.getPath() + "/EasyChangWeibo/"
				+ System.currentTimeMillis() + ".png";

		File file = new File(fpath);
		if (!file.exists()) {
			file.mkdir();
		}

		FileOutputStream os = null;
		try {
			os = new FileOutputStream(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		try {
			os.flush();
			os.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		File file2 = new File(path);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file2));
		shareIntent.setType("image/*");
		startActivity(Intent.createChooser(shareIntent, "发布"));
	}
}
