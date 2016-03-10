package com.example.richtext.imageloader.core;

import com.example.richtext.imageloader.core.assist.ImageSize;
import com.example.richtext.imageloader.core.listener.ImageLoadingListener;
import com.example.richtext.imageloader.core.listener.ImageLoadingProgressListener;
import com.example.richtext.utils.imageloader.base.imageaware.ImageAware;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 加载和展示图片的任务的信息
 *
 * @author renhui
 * @see com.example.richtext.imageloader.utils.MemoryCacheUtils
 * @see DisplayImageOptions
 * @see ImageLoadingListener
 * @see com.example.richtext.imageloader.core.listener.ImageLoadingProgressListener
 */
public class ImageLoadingInfo {
	/**图片的uri */
	public String uri;   
	/**图片缓存键 */
	public String memoryCacheKey;
	/**图片展示控件 {@link ImageAware} */
	public ImageAware imageAware;
	/**图片尺寸信息{@link ImageSize} */
	public ImageSize targetSize;
	/**图片展示时的参数 */
	public DisplayImageOptions options;
	/**图片加载时的监听 */
	public ImageLoadingListener listener;
	/**图片加载过程的监听 */
	public ImageLoadingProgressListener progressListener;
	/**图片加载时的uri锁 */
	public ReentrantLock loadFromUriLock;

	public ImageLoadingInfo(String uri, ImageAware imageAware, ImageSize targetSize, String memoryCacheKey,
			DisplayImageOptions options, ImageLoadingListener listener,
			ImageLoadingProgressListener progressListener, ReentrantLock loadFromUriLock) {
		this.uri = uri;
		this.imageAware = imageAware;
		this.targetSize = targetSize;
		this.options = options;
		this.listener = listener;
		this.progressListener = progressListener;
		this.loadFromUriLock = loadFromUriLock;
		this.memoryCacheKey = memoryCacheKey;
	}
}
