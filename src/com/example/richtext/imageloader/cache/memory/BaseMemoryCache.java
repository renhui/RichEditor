package com.example.richtext.imageloader.cache.memory;

import android.graphics.Bitmap;

import java.lang.ref.Reference;
import java.util.*;

/**
 * 内存缓存基类 (貌似存储bitmap的引用都是软引用 SoftReference)
 * 实现了内存缓存的通用的功能. 提供了对象引用的非强引用存储功能 {@linkplain Reference not strong})
 *
 * @author renhui
 */
public abstract class BaseMemoryCache implements MemoryCache {

	/** 存储方式：存储对象的软引用 */
	private final Map<String, Reference<Bitmap>> softMap = Collections.synchronizedMap(new HashMap<String, Reference<Bitmap>>());

	@Override
	public Bitmap get(String key) {
		Bitmap result = null;
		Reference<Bitmap> reference = softMap.get(key);
		if (reference != null) {
			result = reference.get();
		}
		return result;
	}

	@Override
	public boolean put(String key, Bitmap value) {
		softMap.put(key, createReference(value));
		return true;
	}

	@Override
	public Bitmap remove(String key) {
		Reference<Bitmap> bmpRef = softMap.remove(key);
		return bmpRef == null ? null : bmpRef.get();
	}

	@Override
	public Collection<String> keys() {
		synchronized (softMap) {
			return new HashSet<String>(softMap.keySet());
		}
	}

	@Override
	public void clear() {
		softMap.clear();
	}

	/** 创建对象值的非强引用---注：这个方法决定了缓存对象性质和方式 */
	protected abstract Reference<Bitmap> createReference(Bitmap value);
}
