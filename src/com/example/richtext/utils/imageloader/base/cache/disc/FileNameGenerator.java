package com.example.richtext.utils.imageloader.base.cache.disc;

/**
 * 根据图片的uri生成hashcode类型的缓存文件名
 * 
 * @author renhui
 */
public class FileNameGenerator {

	/** 通过URI来生成唯一的图片文件名---hash码 */
	public String generate(String imageUri) {
		return String.valueOf(imageUri.hashCode());
	}
}
