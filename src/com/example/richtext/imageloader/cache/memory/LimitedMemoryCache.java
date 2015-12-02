package com.example.richtext.imageloader.cache.memory;

import android.graphics.Bitmap;

import com.example.richtext.imageloader.utils.L;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 有限制的缓存. 提供对象存储. 所有要缓存的bitmaps的尺寸大小不会超过规定的大小限制 {@link #getSizeLimit()}).
 * <b>NOTE:</b> 这种缓存方式使用了strong和weak引用来存储Bitmaps. 
 * 强引用	 -  用来限制bitmaps的数量(取决于缓存的尺寸大小), 弱引用 - 对于其他所有的缓存bitmap.
 *
 * @author renhui
 * @see BaseMemoryCache
 */
public abstract class LimitedMemoryCache extends BaseMemoryCache {

	private static final int MAX_NORMAL_CACHE_SIZE_IN_MB = 16;
	private static final int MAX_NORMAL_CACHE_SIZE = MAX_NORMAL_CACHE_SIZE_IN_MB * 1024 * 1024;

	private final int sizeLimit;

	/**AtomicInteger是一个能提供原子操作的Integer类.此类在进行加减法操作的时候是线程安全的,不需要synchronized关键字.*/
	private final AtomicInteger cacheSize;

	/**
	 * 包含要存储的对象的强引用. 每一个添加进去的对象都是最后一个. 如果硬缓存的尺寸将要超过限定的尺寸则删除掉最开始缓存的对象.
	 * 但是在{@link #softMap}里面还会继续存在(在{@link #softMap}里面存储的内容能够随时被GC回收)
	 */
	private final List<Bitmap> hardCache = Collections.synchronizedList(new LinkedList<Bitmap>());

	/** @param 最大的缓存的尺寸的限制 (单位：bytes) */
	public LimitedMemoryCache(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		cacheSize = new AtomicInteger();
		if (sizeLimit > MAX_NORMAL_CACHE_SIZE) {
			L.w("You set too large memory cache size (more than %1$d Mb)", MAX_NORMAL_CACHE_SIZE_IN_MB);
		}
	}

	@Override
	public boolean put(String key, Bitmap value) {
		boolean putSuccessfully = false;
		// 尝试添加到应缓存里面
		int valueSize = getSize(value);
		int sizeLimit = getSizeLimit();
		int curCacheSize = cacheSize.get();
		if (valueSize < sizeLimit) {
			// 如果需要--移除最开始的缓存的内容
			while (curCacheSize + valueSize > sizeLimit) {
				Bitmap removedValue = removeNext();
				if (hardCache.remove(removedValue)) {
					curCacheSize = cacheSize.addAndGet(-getSize(removedValue));
				}
			}
			hardCache.add(value);
			cacheSize.addAndGet(valueSize);

			putSuccessfully = true;
		}
		// 添加到软缓存里面
		super.put(key, value);
		return putSuccessfully;
	}

	@Override
	public Bitmap remove(String key) {
		Bitmap value = super.get(key);
		if (value != null) {
			if (hardCache.remove(value)) {
				cacheSize.addAndGet(-getSize(value));
			}
		}
		return super.remove(key);
	}

	@Override
	public void clear() {
		hardCache.clear();
		cacheSize.set(0);
		super.clear();
	}

	protected int getSizeLimit() {
		return sizeLimit;
	}

	protected abstract int getSize(Bitmap value);

	protected abstract Bitmap removeNext();
}
