package com.frame.imageloader.core.imageaware;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ImageViewAware extends ViewAware {
	public ImageViewAware(ImageView imageView) {
		super(imageView);
	}

	@Override
	protected void setImageDrawableInto(Drawable drawable, View view) {
		((ImageView) view).setImageDrawable(drawable);
		if (drawable instanceof AnimationDrawable) {
			((AnimationDrawable)drawable).start();
		}
	}

	@Override
	protected void setImageBitmapInto(Bitmap bitmap, View view) {
		((ImageView) view).setImageBitmap(bitmap);
	}

}
