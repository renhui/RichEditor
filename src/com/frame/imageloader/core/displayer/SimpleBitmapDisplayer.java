package com.frame.imageloader.core.displayer;

import android.graphics.Bitmap;

import com.frame.imageloader.core.LoadedFrom;
import com.frame.imageloader.core.imageaware.ImageAware;

/**
 * 仅仅是在{@link ImageAware}里面展示Bitmap
 * @author renhui
 */
public class SimpleBitmapDisplayer implements BitmapDisplayer {

	@Override
	public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
		imageAware.setImageBitmap(bitmap);
	}
}
