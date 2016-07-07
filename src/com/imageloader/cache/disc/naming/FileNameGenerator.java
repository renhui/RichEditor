package com.imageloader.cache.disc.naming;


/**
 * 磁盘缓存文件名生成(接口)
 * 
 * @author renhui
 *
 */
public interface FileNameGenerator {
	
	/**通过URI来生成唯一的图片文件名*/
	String generate(String imageUri);

}
