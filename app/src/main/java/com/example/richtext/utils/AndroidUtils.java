package com.example.richtext.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

public final class AndroidUtils {

	public static boolean isKitKatOrHigher() {
		return Build.VERSION.SDK_INT >= 19;
	}

	public static boolean isJellyBeanOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static boolean isICSOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static boolean isHoneycombOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean isGingerbreadOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean isFroyoOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean isGoogleTV(Context context) {
		return context.getPackageManager().hasSystemFeature(
				"com.google.android.tv");
	}

	/**
	 * Checks if {@link android.os.Environment}.MEDIA_MOUNTED is returned by
	 * {@code getExternalStorageState()} and therefore external storage is read-
	 * and writeable.
	 */
	public static boolean isExtStorageAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * Whether there is any network connected.
	 */
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	// @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	// public static boolean isRtlLayout() {
	// if (AndroidUtils.isJellyBeanMR1OrHigher()) {
	// int direction = TextUtils.getLayoutDirectionFromLocale(Locale
	// .getDefault());
	// return direction == View.LAYOUT_DIRECTION_RTL;
	// }
	// return false;
	// }

	/**
	 * Whether there is an active WiFi connection.
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
	}

	public static boolean isSDCardAvailable() {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			ToastUtils.show("SD卡不可用");
			return false;
		}
		return true;
	}
}
