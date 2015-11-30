package com.example.richtext.imageloader.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * (输入/输出)操作工具类
 * 
 * @author renhui
 * @since 2015-11-3
 */
public final class IoUtils {
	
	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;  // 默认缓冲区大小 --- 32KB
	public static final int DEFAULT_IMAGE_TOTAL_SIZE = 500 * 1024;	// 默认图片总大小
	public static final int CONTINUE_LOADING_PERCENTAGE = 75;  

	private IoUtils() {
	}
	
	
	public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener) throws IOException {
		return copyStream(is, os, listener, DEFAULT_BUFFER_SIZE);
	}
	
	public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener, int bufferSize)
			throws IOException {
		int current = 0;
		int total = is.available();
		if (total <= 0) {
			total = DEFAULT_IMAGE_TOTAL_SIZE;
		}

		final byte[] bytes = new byte[bufferSize];
		int count;
		if (shouldStopLoading(listener, current, total)) return false;
		while ((count = is.read(bytes, 0, bufferSize)) != -1) {
			os.write(bytes, 0, count);
			current += count;
			if (shouldStopLoading(listener, current, total)) return false;
		}
		os.flush();
		return true;
	}
	
	private static boolean shouldStopLoading(CopyListener listener, int current, int total) {
		if (listener != null) {
			boolean shouldContinue = listener.onBytesCopied(current, total);
			if (!shouldContinue) {
				if (100 * current / total < CONTINUE_LOADING_PERCENTAGE) {
					return true; // if loaded more than 75% then continue loading anyway
				}
			}
		}
		return false;
	}
	
	/**
	 * 从流中读取所有的数据然后静默关闭输入流
	 * @param is  输入流
	 */
	public static void readAndCloseStream(InputStream is) {
		final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
		try {
			while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1);
		} catch (IOException ignored) {
		} finally {
			closeSilently(is);
		}
	}

	/***
	 * 静默关闭
	 * @param closeable
	 */
	public static void closeSilently(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ignored) {
			}
		}
	}
	
	
	/** 监听和控制复制过程 */
	public static interface CopyListener {
		
		/**
		 * @param current 已经加载过的字节数
		 * @param total	要加载的字节数
		 * @return 如果拷贝需要继续则返回true, 如果拷贝需要被中断返回false
		 */
		boolean onBytesCopied(int current, int total);
		
	}

}
