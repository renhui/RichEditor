package com.example.richtext.utils.imageloader;

/**
 * 图片加载类(单例)
 * 
 * @author renhui
 */
public class ImageLoader {

	/* volatile的使用保证了双重检测锁的正确性 */
	private volatile static ImageLoader instance;

	/**
	 * 获取图片加载的单例对象 
	 * 注：使用了双重校验的机制,提高了代码的运行效率
	 */
	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}
	
	/** 私有构造函数  */
	private ImageLoader() {
	}

}
