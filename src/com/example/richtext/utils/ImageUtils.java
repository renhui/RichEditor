package com.example.richtext.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

/**
 * 图片工具类
 * 
 * @author renhui
 */
public class ImageUtils {

	public static final float SCREEN_WIDTH_PERCENT = 0.73f; // 480以上手机用此百分比
	public static final float SCREEN_WIDTH_PERCENT_SMALL_SCREEN = 0.68f; // 小屏
	public static final float STANDARD_WIDTH = 480;

	public static final float SCALE_NUM_75 = 0.75f; // 图片缩放占屏比例

	/**
	 * 以 480*800的手机为标准，如果手机尺寸比较大，图片放大相应的比例。
	 */
	public static final int standardWidth = 400;

	/**
	 * 将图片保存为文件
	 * 
	 * @param bitmap
	 * @return
	 */
	public static String createSignFile(Bitmap bitmap) {
		ByteArrayOutputStream bos = null;
		FileOutputStream fos = null;
		String path = null;
		File file = null;

		try {
			path = Environment.getExternalStorageDirectory() + File.separator
					+ System.currentTimeMillis() + ".jpg";
			DebugTraceTool.debugTraceE(ImageUtils.class.getName(), path);
			file = new File(path);
			fos = new FileOutputStream(file);
			bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
			byte[] b = bos.toByteArray();
			if (b != null) {
				fos.write(b);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}

				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	/**
	 * 估算要缩放的比例 (小屏手机 <= 480)
	 * 
	 * @param options
	 * @param reqWidth
	 * @return
	 */
	public static int reckonInSampleSizeForSmall(BitmapFactory.Options options,
			int reqWidth) {
		final int width = options.outWidth;
		int inSampleSize = 1;
		while ((width / inSampleSize) > reqWidth) {
			inSampleSize += 1;
		}
		return inSampleSize;
	}

	/**
	 * 估算要缩放的比例 (大屏手机)
	 * 
	 * @param options
	 * @param reqWidth
	 * @return
	 */
	public static int reckonInSampleSizeForBig(BitmapFactory.Options options,
			int reqWidth) {
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (width < (reqWidth / 2)) {
			inSampleSize = -1;
		} else {
			while ((int) (width / inSampleSize) > reqWidth) {
				inSampleSize = inSampleSize * 2;
			}
		}

		if (inSampleSize == -1) {
			return 1;
		} else {
			return inSampleSize;
		}
	}

	/**
	 * 获取手机的宽度大小
	 * 
	 * @param context
	 * @return
	 */
	public static int getPhoneScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取手机的高度大小
	 * 
	 * @param context
	 * @return
	 */
	public static int getPhoneScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 针对图片的大小进行处理
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Drawable convert(Context context, Bitmap bitmap) {
		Bitmap img;
		int req_width;
		// 判断屏幕的宽度
		if (getPhoneScreenWidth(context) <= 480) {
			req_width = (int) (getPhoneScreenWidth(context) * SCREEN_WIDTH_PERCENT_SMALL_SCREEN);
		} else {
			req_width = (int) (getPhoneScreenWidth(context) * SCREEN_WIDTH_PERCENT);
		}
		float scale = (float) req_width / (float) bitmap.getWidth();
		img = reSizeBitmap(bitmap, scale);
		BitmapDrawable draw = new BitmapDrawable(context.getResources(), img);
		draw.setBounds(0, 0, img.getWidth(), img.getHeight());
		return draw;
	}

	/*** 矩阵缩小图像 */
	public static Bitmap reSizeBitmap(Bitmap bitmap, float scale) {
		if (bitmap == null) {
			return null;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
}
