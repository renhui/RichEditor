package com.example.richtext.imageloader.utils;

import android.util.Log;

import com.example.richtext.imageloader.core.ImageLoader;

/**
 * "Less-word" analog of Android {@link android.util.Log logger}
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.6.4
 */
public final class L {

	private static final String LOG_FORMAT = "%1$s\n%2$s";
	private static volatile boolean writeDebugLogs = true;
	private static volatile boolean writeLogs = true;

	private L() {
	}

	public static void d(String message, Object... args) {
		if (writeDebugLogs) {
			log(Log.DEBUG, null, message, args);
		}
	}

	public static void i(String message, Object... args) {
		log(Log.INFO, null, message, args);
	}

	public static void w(String message, Object... args) {
		log(Log.WARN, null, message, args);
	}

	public static void e(Throwable ex) {
		log(Log.ERROR, ex, null);
	}

	public static void e(String message, Object... args) {
		log(Log.ERROR, null, message, args);
	}

	public static void e(Throwable ex, String message, Object... args) {
		log(Log.ERROR, ex, message, args);
	}

	private static void log(int priority, Throwable ex, String message, Object... args) {
		if (!writeLogs) return;
		if (args.length > 0) {
			message = String.format(message, args);
		}

		String log;
		if (ex == null) {
			log = message;
		} else {
			String logMessage = message == null ? ex.getMessage() : message;
			String logBody = Log.getStackTraceString(ex);
			log = String.format(LOG_FORMAT, logMessage, logBody);
		}
		
		switch (priority) {
		case Log.VERBOSE:
			Log.v(ImageLoader.TAG, log);
			break;
		case Log.DEBUG:
			Log.e(ImageLoader.TAG, log);
			break;
		case Log.INFO:
			Log.i(ImageLoader.TAG, log);
			break;
		case Log.WARN:
			Log.w(ImageLoader.TAG, log);
			break;
		case Log.ERROR:
			Log.e(ImageLoader.TAG, log);
			break;
		default:
			break;
		}
	}
}