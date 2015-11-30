package com.example.richtext.imageloader.core.download;

import com.example.richtext.imageloader.core.DisplayImageOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * 提供通过URI获取图片的输入流
 * 实现必须保证线程安全
 *
 * @author renhui
 */
public interface ImageDownloader {
	/**
	 * 通过URI获取图片的输入流
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 * @throws IOException    如果获取图片输入流的时候发生一些I/O错误
	 * @throws UnsupportedOperationException 如果图片的URI的方案(协议)不被支持
	 */
	InputStream getStream(String imageUri, Object extra) throws IOException;

	/** 
	 * 列举了支持的URI的协议(方案)。
	 * 提供了相应的方案来使用这些URI相关的协议(方案)
	 */
	public enum Scheme {
		HTTP("http"), HTTPS("https"), FILE("file"), CONTENT("content"), ASSETS("assets"), DRAWABLE("drawable"), UNKNOWN("");

		private String scheme;
		private String uriPrefix;

		Scheme(String scheme) {
			this.scheme = scheme;
			uriPrefix = scheme + "://";
		}

		/**
		 * 定义输入的URI的方案
		 *
		 * @param uri 要输入的URI
		 * @return 当前URI使用的Scheme
		 */
		public static Scheme ofUri(String uri) {
			if (uri != null) {
				// 枚举遍历所有的Scheme, 返回当前uri的Scheme
				for (Scheme s : values()) {
					if (s.belongsTo(uri)) {
						return s;
					}
				}
			}
			return UNKNOWN;
		}

		private boolean belongsTo(String uri) {
			return uri.toLowerCase(Locale.US).startsWith(uriPrefix);
		}

		/** 将scheme添加到输入的路径里面 */
		public String wrap(String path) {
			return uriPrefix + path;
		}

		/** 从输入的URI里面移除Scheme部分 */
		public String crop(String uri) {
			if (!belongsTo(uri)) {
				throw new IllegalArgumentException(String.format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, scheme));
			}
			return uri.substring(uriPrefix.length());
		}
	}
}
