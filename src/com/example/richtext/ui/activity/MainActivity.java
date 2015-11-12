package com.example.richtext.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.example.richtext.R;
import com.example.richtext.imageloader.core.ImageLoader;
import com.example.richtext.imageloader.core.ImageLoaderConfiguration;
import com.example.richtext.moudle.EditData;
import com.example.richtext.ui.widget.RichEditor;
import com.example.richtext.utils.LongBlogContent;

import android.annotation.SuppressLint;
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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

/**
 * 主Activity入口
 * 
 * @author RenHui
 * 
 */
@SuppressLint("SimpleDateFormat")
public class MainActivity extends FragmentActivity {
	private static final int REQUEST_CODE_PICK_IMAGE = 1023;
	private static final int REQUEST_CODE_CAPTURE_CAMEIA = 1022;
	private RichEditor editor;
	private View btn1, btn2, btn3;
	private OnClickListener btnListener;

	private static final File PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
	private File mCurrentPhotoFile;// 照相机拍照得到的图片

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// 初始化图片加载控件
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);

		editor = (RichEditor) findViewById(R.id.richEditor);
		btnListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editor.hideKeyBoard();
				if (v.getId() == btn1.getId()) {
					// 打开系统相册
					Intent intent = new Intent(Intent.ACTION_PICK);
					intent.setType("image/*");// 相片类型
					startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
				} else if (v.getId() == btn2.getId()) {
					// 打开相机
					openCamera();
				} else if (v.getId() == btn3.getId()) {
//					List<EditData> editList = editor.buildEditData();
					// 下面的代码可以上传、或者保存，请自行实现
//					dealEditData(editList);
					
//					TODO 生成长微博
					int picWidth = 1000; // 适应新浪微博解析分辨率
					int fontSize = 30; // 字体大小  目前先自己设定18sp
					
					int WORDNUM = (1000/(fontSize)) -1; // 转化成图片的时，每行显示的字数
					
					// 设置文字在图片中的显示间距
					int x = 10;
					float y = (float) (fontSize * 0.8);
					LongBlogContent ct = LongBlogContent.getInstance();
					ct.clearStatus();
					ct.handleText("阿达的卡上来得及阿来得及阿来得及垃圾设定来科技阿萨德及垃圾设定来", WORDNUM);
					
					String imageUri = Environment.getExternalStorageDirectory().getPath() + "/EasyChangWeibo/" + "20151011162535.png";
					BitmapFactory.Options options = new BitmapFactory.Options();
					Bitmap b = BitmapFactory.decodeFile(imageUri, options);
					Log.e("11", options.outHeight + "高度");
					
					Bitmap bitmap = Bitmap.createBitmap(picWidth, 35*(ct.getHeight() + 1) + options.outHeight, Config.ARGB_8888);
					//创建画布
					Canvas canvas = new Canvas(bitmap);
					//设置画布背景颜色
					canvas.drawARGB(255, 255, 255, 255);
					//创建画笔
					Paint paint = new Paint();
					//通过画笔设置字体的大小、格式、颜色
					paint.setTextSize(fontSize);
					paint.setARGB(255, 0, 0, 0);										
					y = y + 10;					
					//将处理后的内容画到画布上
					String []ss = ct.getContent();
					for(int i = 0; i < ct.getHeight(); i++){
						canvas.drawText(ss[i], x, y, paint);
						y = y + 35;
					}
					
					canvas.save(Canvas.ALL_SAVE_FLAG);
//					canvas.restore();			
					
					canvas.drawBitmap(b, 0, y, null);
					canvas.restore();
					
					File sd = Environment.getExternalStorageDirectory();
					String fpath = sd.getPath() + "/EasyChangWeibo";
					
					//设置保存路径
					String path = sd.getPath() + "/EasyChangWeibo/" + "123.png";

					File file = new File(fpath);
					if(!file.exists()){
						file.mkdir();
					}

					FileOutputStream os = null;
					try {
						os = new FileOutputStream(new File(path));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
					
					try {
						os.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					
	                File file2 = new File(path);
	                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file2));
	                
	                shareIntent.setType("image/*");
	                startActivity(Intent.createChooser(shareIntent, "发布"));
				}
			}
		};

		btn1 = findViewById(R.id.button1);
		btn2 = findViewById(R.id.button2);
		btn3 = findViewById(R.id.button3);

		btn1.setOnClickListener(btnListener);
		btn2.setOnClickListener(btnListener);
		btn3.setOnClickListener(btnListener);
	}

	/**
	 * 负责处理编辑数据提交等事宜，请自行实现
	 */
	protected void dealEditData(List<EditData> editList) {
		String data = "";
		for (EditData itemData : editList) {
			if (itemData.inputStr != null) {
				data += itemData.inputStr;
				Log.d("RichEditor", "commit inputStr=" + itemData.inputStr);
			} else if (itemData.imagePath != null) {
				data += itemData.imagePath;
				Log.d("RichEditor", "commit imgePath=" + itemData.imagePath);
			}
		}
		Log.e("111", data);
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

	/**
	 * 用当前时间给取得的图片命名
	 */
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
		editor.insertImage(imagePath);
	}

	/**
	 * 根据Uri获取图片文件的绝对路径
	 */
	public String getRealFilePath(final Uri uri) {
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
}
