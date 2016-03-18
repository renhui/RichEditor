package com.example.richtext.imageloader.core.listener;

import com.example.richtext.imageloader.core.assist.FailReason;

import android.graphics.Bitmap;
import android.view.View;


/**
 * 图片加载进程监听
 * @author renhui
 */
public interface ImageLoadingListener {
	
	/**
	 * 当图片加载任务开始的时候被调用
	 * @param imageUri
	 * @param view
	 */
	void onLoadingStarted(String imageUri, View view);
	
	/**
	 * 当图片加载出现错误的时候被调用
	 * @param imageUri
	 * @param view
	 * @param failReason
	 */
	void onLoadingFailed(String imageUri, View view, FailReason failReason);
	
	/**
	 * 当图片加载(展示)成功的时候被调用
	 * @param imageUri
	 * @param view
	 * @param loadedImage
	 */
	void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);
	
	/**
	 * 当加载任务被取消的时候调用(因为用来加载此图片的view被用来进行其他的加载任务了)
	 * @param imageUri
	 * @param view
	 */
	void onLoadingCancelled(String imageUri, View view);
}
