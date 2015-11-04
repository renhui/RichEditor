package com.frame.imageloader.core.imageaware;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.frame.imageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.utils.L;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewAware implements ImageAware {
	public static final String WARN_CANT_SET_DRAWABLE = "Can't set a drawable into view. You should call ImageLoader on UI thread for it.";
	public static final String WARN_CANT_SET_BITMAP = "Can't set a bitmap into view. You should call ImageLoader on UI thread for it.";
	
	protected Reference<View> viewRef;
	protected boolean checkActualViewSize;
	
	public ViewAware(View view) {
		this(view, true);
	}
	
	public ViewAware(View view, boolean checkActualViewSize) {
		if (view == null) throw new IllegalArgumentException("view must not be null");

		this.viewRef = new WeakReference<View>(view);
		this.checkActualViewSize = checkActualViewSize;
	}
	
	
	@Override
	public int getWidth() {
		View view = viewRef.get();
		if (view != null) {
			final ViewGroup.LayoutParams params = view.getLayoutParams();
			int width = 0;
			if (checkActualViewSize && params != null && params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
				width = view.getWidth(); // Get actual image width
			}
			if (width <= 0 && params != null) width = params.width; // Get layout width parameter
			return width;
		}
		return 0;
	}
	
	
	@Override
	public int getHeight() {
		View view = viewRef.get();
		if (view != null) {
			final ViewGroup.LayoutParams params = view.getLayoutParams();
			int height = 0;
			if (checkActualViewSize && params != null && params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
				height = view.getHeight(); // Get actual image height
			}
			if (height <= 0 && params != null) height = params.height; // Get layout height parameter
			return height;
		}
		return 0;
	}
	
	@Override
	public ViewScaleType getScaleType() {
		return ViewScaleType.CROP;
	}

	@Override
	public View getWrappedView() {
		return viewRef.get();
	}

	@Override
	public boolean isCollected() {
		return viewRef.get() == null;
	}

	@Override
	public int getId() {
		View view = viewRef.get();
		return view == null ? super.hashCode() : view.hashCode();
	}
	
	@Override
	public boolean setImageDrawable(Drawable drawable) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			View view = viewRef.get();
			if (view != null) {
				setImageDrawableInto(drawable, view);
				return true;
			}
		} else {
			L.w(WARN_CANT_SET_DRAWABLE);
		}
		return false;
	}

	@Override
	public boolean setImageBitmap(Bitmap bitmap) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			View view = viewRef.get();
			if (view != null) {
				setImageBitmapInto(bitmap, view);
				return true;
			}
		} else {
			L.w(WARN_CANT_SET_BITMAP);
		}
		return false;
	}
	
	protected abstract void setImageDrawableInto(Drawable drawable, View view);

	protected abstract void setImageBitmapInto(Bitmap bitmap, View view);
	

}
