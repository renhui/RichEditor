package com.example.richtext.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;

/**
 * 图片工具类
 * 
 * @author renhui
 */
public class ImageUtils {

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
			path = Environment.getExternalStorageDirectory() + File.separator +  System.currentTimeMillis() + ".jpg";
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
}
