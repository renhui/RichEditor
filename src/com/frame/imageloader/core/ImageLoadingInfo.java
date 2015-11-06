package com.frame.imageloader.core;

import com.frame.imageloader.core.imageaware.ImageAware;
import com.frame.imageloader.core.listener.ImageLoadingListener;

final class ImageLoadingInfo {
	
	final String uri;
	final ImageAware imageAware;
	final ImageLoadingListener listener;
	
	
	public ImageLoadingInfo(String uri, ImageAware imageAware, ImageLoadingListener listener) {
		this.uri = uri;
		this.imageAware = imageAware;
		this.listener = listener;
	}
}
