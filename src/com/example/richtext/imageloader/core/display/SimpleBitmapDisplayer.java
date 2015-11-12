package com.example.richtext.imageloader.core.display;

import com.example.richtext.imageloader.core.assist.LoadedFrom;
import com.example.richtext.imageloader.core.imageaware.ImageAware;

import android.graphics.Bitmap;


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
