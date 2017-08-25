package com.imageloader.cache.disc.naming;

/**
 * 根据图片的uri生成hashcode类型的缓存文件名
 * @author renhui
 */
public class HashCodeFileNameGenerator implements FileNameGenerator {
	
	@Override
	public String generate(String imageUri) {
		return String.valueOf(imageUri.hashCode());
	}
}
