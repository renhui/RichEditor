package com.nostra13.universalimageloader.cache.disc.naming;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class Md5FileNameGenerator implements FileNameGenerator {
	
	
	/**10位数字 + 26字母  ?! 目前不清楚为什么要设置此数,先不深入研究此问题
	 * 目前唯一知道的是BigInteger不可变的任意精度的整数 */
	private static final int RADIX = 10 + 26;	
	private static final String HASH_ALGORITHM = "MD5";  // 加密算法名称

	@Override
	public String generate(String imageUri) {
		byte[] md5 = getMD5(imageUri.getBytes());
		BigInteger bi = new BigInteger(md5).abs();
		return bi.toString(RADIX);
	}
	
	/**
	 * MD5加密
	 * @param data
	 * @return 加密后的内容
	 */
	private byte[] getMD5(byte[] data) {
		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(data);
			hash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}
		return hash;
	}
	
	/**
	 * MD5 加密 Sample
	 MessageDigest digester = MessageDigest.getInstance("MD5");
	 byte[] bytes = new byte[8192];
	 int byteCount;
	 while ((byteCount = in.read(bytes)) > 0) {
		 digester.update(bytes, 0, byteCount);
	 }
	 byte[] digest = digester.digest();
	 */
}
