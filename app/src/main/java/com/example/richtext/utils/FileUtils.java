package com.example.richtext.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;

/***
 * File工具类
 * @author: RenHui
 */
public class FileUtils {
	
	/** 根据Uri获取图片文件的绝对路径 */
	public static String getRealFilePath(Context context, Uri uri) {
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
			Cursor cursor = context.getContentResolver().query(uri, new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
			if (data == null || data.length() == 0) {
				data = copyUriToCache(context, uri);
			}
		}
		return data;
	}

	/** 获取可持久读取的图片路径（content uri 优先复制到应用缓存目录） */
	public static String getUsableImagePath(Context context, Uri uri) {
		if (uri == null) {
			return null;
		}
		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			String copiedPath = copyUriToCache(context, uri);
			if (copiedPath != null && copiedPath.length() > 0) {
				return copiedPath;
			}
		}
		return getRealFilePath(context, uri);
	}

	private static String copyUriToCache(Context context, Uri uri) {
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			if (inputStream == null) {
				return null;
			}
			File cacheFile = new File(context.getCacheDir(), "picked_" + System.currentTimeMillis() + ".jpg");
			outputStream = new FileOutputStream(cacheFile);
			byte[] buffer = new byte[8192];
			int len;
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			outputStream.flush();
			return cacheFile.getAbsolutePath();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception ignored) {
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception ignored) {
			}
		}
	}
}
