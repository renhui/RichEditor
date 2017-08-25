package com.imageloader.core.display;

import android.graphics.Bitmap;

import com.imageloader.core.assist.LoadedFrom;
import com.imageloader.core.imageaware.ImageAware;

/**
 * 在{@link ImageAware}里面展示{@link Bitmap}.
 * 实现的代码需要适用于一些Bitmap的变化或者任何Bitmap动画的展示
 * 实现的代码必须是线程的安全的
 * @author renhui
 */
public interface BitmapDisplayer {
	
	
	/**
	 * 在{@link ImageAware} 里面展示bitmap
	 * 注意：此方法需要在UI线程里面执行,强烈建议不要在此方法里面执行任何繁重的任务
	 * @param bitmap  要加载的源Bitmap
	 * @param imageAware  用来展示Bitmap的控件
	 * @param loadedFrom  加载源
	 */
	void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom);
}
