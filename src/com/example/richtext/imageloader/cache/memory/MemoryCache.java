package com.example.richtext.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.util.Collection;

/**
 * 接口--内存缓存
 *
 * @author renhui
 */
public interface MemoryCache {
	/**
	 * 通过键值对的方式放入缓存中
	 *
	 * @return <b>true</b> - 如果成功放置键值对到缓存中, <b>false</b> - 如果没有放置到缓存中
	 */
	boolean put(String key, Bitmap value);

	/** 根据键返回值. 如果没有此键的值则返回null. */
	Bitmap get(String key);

	/** 根据key移除内容 */
	Bitmap remove(String key);

	/** 返回缓存中的所有的键 */
	Collection<String> keys();

	/** 从缓存中移除所有的内容 */
	void clear();
}
