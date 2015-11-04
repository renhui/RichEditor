package com.frame.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.frame.imageloader.core.assist.ViewScaleType;


/**
 * 图片感知接口
 * 此接口提供了图片处理和显示所需要的属性和行为
 * @author renhui
 *
 */
public interface ImageAware {
	
	int getWidth();
	
	int getHeight();
	
	ViewScaleType getScaleType();
	
	View getWrappedView();
	
	boolean isCollected();

	int	getId();
	
	boolean setImageDrawable(Drawable drawable);
	
	boolean setImageBitmap(Bitmap bitmap);
}
