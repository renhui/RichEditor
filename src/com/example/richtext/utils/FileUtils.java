package com.example.richtext.utils;

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
		}
		return data;
	}
}
