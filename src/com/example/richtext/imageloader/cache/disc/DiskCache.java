package com.example.richtext.imageloader.cache.disc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.example.richtext.imageloader.utils.IoUtils;

/**
 * 磁盘缓存接口
 * @author renhui
 */
public interface DiskCache {
	
	/**
	 * 获取磁盘缓存根目录
	 * @return 磁盘缓存根目录
	 */
	File getDirectory();
	
	/**
	 * 获取已经缓存下来的图片文件
	 * @param imageUri
	 * @return 已缓存的图片文件 or 空(如果该图片没有被缓存过)
	 */
	File get(String imageUri);
	
	/**
	 * 保存图片流到磁盘
	 * 注：输入图片流在这个方法里面不应该被关闭掉
	 * @param imageUri 原始的图片Uri
	 * @param imageStream 图片输入流(不能在此类里面关闭流)
	 * @param listener 保存进程的监听(此监听可以被省略,如果在ImageLoader调用里面没有调用)
	 * @return 如果保存成功了,返回true; 如果没有被保存成功，返回false
	 * @throws IOException
	 */
	boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException;
	
	/**
	 * 保存图片位图在磁盘缓存
	 * @param imageUri  原始图片URI
	 * @param bitmap  图片Bitmap
	 * @return	如果保存成功了,返回true; 如果没有被保存成功，返回false
	 * @throws IOException
	 */
	boolean save(String imageUri, Bitmap bitmap) throws IOException;
	
	/**
	 * 删除指定的URI相关的图片文件
	 * @param imageUri  图片文件的Uri
	 * @return  如果图片文件删除成功,返回true; 如果指定uri的图片文件不存在或者图片文件不能够被删除掉,则返回false
	 */
	boolean remove(String imageUri);
	/**
	 * 关闭磁盘缓存,释放所有资源
	 */
	void close();
	
	/**
	 * 清除所有的磁盘缓存
	 */
	void clear();
}
