package com.renhui.richtext;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 继承ImageView 此ImageView可以存储图片的地址
 * 
 * @author renhui
 */
public class RichImageView extends ImageView {

	private String picturePath; // 图片地址

	public RichImageView(Context context) {
		this(context, null);
	}

	public RichImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RichImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
}
