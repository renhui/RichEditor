package com.example.richtext.imageloader.utils;

import java.io.File;

import com.example.richtext.imageloader.cache.disc.DiskCache;

/**
 * 图片磁盘缓存工具类
 * 
 * @author renhui
 */
public final class DiskCacheUtils {

	/**
	 * 私有构造函数
	 */
	private DiskCacheUtils() {
	}

	/**
	 * 查找缓存中的图片
	 * 
	 * @param imageUri
	 * @param diskCache
	 * @return 如果已经缓存了,则返回获取到的缓存,如果从没缓存过就返回null
	 */
	public static File findInCache(String imageUri, DiskCache diskCache) {
		File image = diskCache.get(imageUri);
		return image != null && image.exists() ? image : null;
	}

	/**
	 * 清除已经磁盘中缓存的图片文件
	 * 
	 * @param imageUri
	 *            图片的Uri
	 * @param diskCache
	 *            磁盘缓存方法
	 * @return 如果图片文件存在而且删除成功了,返回true;否则返回false
	 */
	public static boolean removeFromCache(String imageUri,
			DiskCache diskCache) {
		File image = diskCache.get(imageUri);
		return image != null && image.exists() && image.delete();
	}
}
