package com.frame.imageloader.core.assist;

/**
 * 图片解码时的缩放类型
 * @author renhui
 */
public enum ImageScaleType {
	
	/** 将不进行图片缩放 */
	NONE,
	
	/**
	 * 图片将按照比例缩放如果图像大于可接受的最大尺寸(通常是2048 * 2048),要显示的位图大小不能超过这个尺寸,否则会报错。报错内容如下：
	 * "OpenGLRenderer: Bitmap too large to be uploaded into a texture".
	 * 图片将缩放一定的次数来适配设备的最大尺寸
	 */
	NONE_SAFE,
	
	/**
	 * 图片将被缩放2倍大小到更小的尺寸.
	 * 这是一种比较快速的方式,一般使用在lists、grids、galleries(以及其他的适配器view)
	 * 关联的方法：{@link android.graphics.BitmapFactory.Options#inSampleSize}
	 * 注：如果图片的原始尺寸比目标的尺寸小,则图片不会被缩放.
	 */
	IN_SAMPLE_POWER_OF_2,
	/**
	 * 图片将被缩放一定的倍数(1,2,3...)  使用条件是：内存使用很重要的时候,可以适当对图片进行缩放
	 * 关联的方法：{@link android.graphics.BitmapFactory.Options#inSampleSize}
	 * 注：如果图片的原始尺寸比目标的尺寸小,则图片不会被缩放.
	 */
	IN_SAMPLE_INT,
	/**
	 * 图像将缩小到指定的大小
	 * 如果图片的大小比目标尺寸小,则图片不会被缩放
	 */
	EXACTLY,
	/**
	 * 将图片缩放到指定的大小
	 * 如果图片大小比目标尺寸小，则图片会被拉伸到指定的大小
	 */
	EXACTLY_STRETCHED

}
