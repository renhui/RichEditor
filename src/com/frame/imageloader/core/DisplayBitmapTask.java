package com.frame.imageloader.core;

import android.graphics.Bitmap;
import android.util.Log;

import com.frame.imageloader.core.assist.LoadedFrom;
import com.frame.imageloader.core.displayer.BitmapDisplayer;
import com.frame.imageloader.core.imageaware.ImageAware;
import com.frame.imageloader.core.listener.ImageLoadingListener;

/**
 * 加载图片任务 TODO 还有一些东西没处理
 * @author renhui
 *
 */
public class DisplayBitmapTask implements Runnable {
	
	private static final String LOG_DISPLAY_IMAGE_IN_IMAGEAWARE = "Display image in ImageAware (loaded from %1$s) [%2$s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";
	
	private final Bitmap bitmap;
	private final String imageUri;
	private final ImageAware imageAware;
	private final BitmapDisplayer displayer;
	private final LoadedFrom loadedFrom;
	private final ImageLoadingListener listener;
	
	public DisplayBitmapTask(Bitmap bitmap, ImageLoadingInfo imageLoadingInfo, LoadedFrom loadedFrom, BitmapDisplayer displayer) {
		this.bitmap = bitmap;
		this.loadedFrom = loadedFrom;
		this.imageUri = imageLoadingInfo.uri;
		this.displayer = displayer;
		this.imageAware = imageLoadingInfo.imageAware;
		this.listener = imageLoadingInfo.listener;
	}
	
	@Override
	public void run() {
		if (imageAware.isCollected()) {
			Log.d(DisplayBitmapTask.class.getName(), LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED);
			listener.onLoadingCancelled(imageUri, imageAware.getWrappedView());
		} else {
			Log.d(DisplayBitmapTask.class.getName(), LOG_DISPLAY_IMAGE_IN_IMAGEAWARE);
			displayer.display(bitmap, imageAware, loadedFrom);
			listener.onLoadingComplete(imageUri, imageAware.getWrappedView(), bitmap);
		}
	}

}
