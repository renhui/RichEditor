package com.example.richtext.utils.imageloader.display;

import com.example.richtext.imageloader.core.ImageLoadingInfo;
import com.example.richtext.utils.DebugTraceTool;
import com.example.richtext.utils.imageloader.base.imageaware.ImageAware;

import android.graphics.Bitmap;

public class DisplayBitmapTask implements Runnable {
	
	private final Bitmap bitmap;
	private final String imageUri;
	private final ImageAware imageAware;
	private final String memoryCacheKey;
	
	public DisplayBitmapTask(Bitmap bitmap, ImageLoadingInfo imageLoadingInfo) {
		this.bitmap = bitmap;
		imageUri = imageLoadingInfo.uri;
		imageAware = imageLoadingInfo.imageAware;
		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		
	}

	@Override
	public void run() {
		if (imageAware.isCollected()) {
			DebugTraceTool.debugTrace(this, "ImageAware was collected by GC. Task is cancelled. " + memoryCacheKey);
		} 
	}

}
