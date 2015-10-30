package com.example.richtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 这只是一个简单的ImageView，可以存放Bitmap和Path等信息
 * 通过继承相关的,可以针对性的为控件增加内容和标志
 * @author RenHui
 * 
 */
public class DataImageView extends ImageView {

	private String absolutePath;  // 本地的图片地址
	
	private String picAddress;	// 网络的图片的地址

	private Bitmap bitmap;

	public DataImageView(Context context) {
		this(context, null);
	}

	public DataImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DataImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	public String getPicAddress() {
		return picAddress;
	}

	public void setPicAddress(String picAddress) {
		this.picAddress = picAddress;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}
