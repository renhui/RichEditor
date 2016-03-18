package com.example.richtext.imageloader.core.assist;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


/**
 * 简化ImageView的显示方式为两种： {@link #FIT_INSIDE} and {@link #CROP}
 * @author renhui
 */
public enum ViewScaleType {
	/**
	 * 对图像进行均匀的比例缩放（保持图像的宽高比），从而使图像的至少一个尺寸（宽度或高度）与视图的尺寸相等或更少。
	 */
	FIT_INSIDE,
	
	/**
	 * 对图像进行均匀的比例（保持图像的长宽比），使图像的两个维度（宽度和高度）将等于或大于视图的相应尺寸。
	 */
	CROP;
	
	/**
	 * 定义ImageView的显示风格
	 *
	 * @param imageView {@link ImageView}
	 * @return {@link #FIT_INSIDE} for
	 *         <ul>
	 *         <li>{@link ScaleType#FIT_CENTER}</li>
	 *         <li>{@link ScaleType#FIT_XY}</li>
	 *         <li>{@link ScaleType#FIT_START}</li>
	 *         <li>{@link ScaleType#FIT_END}</li>
	 *         <li>{@link ScaleType#CENTER_INSIDE}</li>
	 *         </ul>
	 *         {@link #CROP} for
	 *         <ul>
	 *         <li>{@link ScaleType#CENTER}</li>
	 *         <li>{@link ScaleType#CENTER_CROP}</li>
	 *         <li>{@link ScaleType#MATRIX}</li>
	 *         </ul>
	 */
	public static ViewScaleType fromImageView(ImageView imageView) {
		switch (imageView.getScaleType()) {
			case FIT_CENTER:
			case FIT_XY:
			case FIT_START:
			case FIT_END:
			case CENTER_INSIDE:
				return FIT_INSIDE;
			case MATRIX:
			case CENTER:
			case CENTER_CROP:
			default:
				return CROP;
		}
	}
}
