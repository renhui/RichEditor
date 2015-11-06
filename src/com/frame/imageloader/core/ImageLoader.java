package com.frame.imageloader.core;

import android.text.TextUtils;

import com.frame.imageloader.core.imageaware.ImageAware;

public class ImageLoader {
	private static final String ERROR_WRONG_ARGUMENTS = "Wrong arguments were passed to displayImage() method (ImageView reference must not be null)";
	public static final String TAG = ImageLoader.class.getSimpleName();

	private volatile static ImageLoader instance;

	// 单例模式
	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				instance = new ImageLoader();
			}
		}
		return instance;
	}

	private ImageLoader() {
	}

	public void displayImage(String uri, ImageAware imageAware) {
		if (imageAware == null) {
			throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
		}
		
		if (TextUtils.isEmpty(uri)) {
			
		}
	}

}
