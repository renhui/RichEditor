package com.example.richtext.utils.imageloader.download;

import java.util.Locale;

public enum Scheme {
	HTTP("http"),  //网络图片 
	HTTPS("https"), //网络图片
	FILE("file"), //文件系统的图片
	ASSETS("assets"),//应用包含的资源图片
	DRAWABLE("drawable"),
	UNKNOWN("");  // 未知图片资源
	
	private String scheme;
	private String uriPrefix;
	
	private Scheme(String scheme) {
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
