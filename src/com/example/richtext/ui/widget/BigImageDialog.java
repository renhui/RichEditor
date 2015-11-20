package com.example.richtext.ui.widget;

import com.example.richtext.MyApplication;
import com.example.richtext.R;
import com.example.richtext.imageloader.core.DisplayImageOptions;
import com.example.richtext.imageloader.core.ImageLoader;
import com.example.richtext.imageloader.core.assist.ImageScaleType;
import com.example.richtext.utils.AndroidUtils;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class BigImageDialog extends Dialog implements OnViewTapListener{

	private View mRootView;
	private Activity mActivity;
	private Animator mCurrentAnimator;
	private final int mShortAnimationDuration = 200;
	private ImageView mExpandedImageView;
	View mThumbView;
	private String mImagePath;
	private android.view.View.OnClickListener onClickListener;
	private PhotoViewAttacher mPhotoViewAttacher;
	
	@SuppressLint("InflateParams")
	public BigImageDialog(Context context) {
		super(context, R.style.bigimage_dialog);
		if (mRootView == null) {
			mRootView = LayoutInflater.from(context).inflate(R.layout.big_image_layout, null);
		}
		
		mActivity = (Activity) context;
		setContentView(mRootView);
		getWindow().getAttributes().width = -1;
		getWindow().getAttributes().height = -1;
		getWindow().setAttributes(getWindow().getAttributes());
		mExpandedImageView = (ImageView) mRootView.findViewById(R.id.big_image);
		mExpandedImageView.setVisibility(View.VISIBLE);

		int rotationCfg = Settings.System.getInt(mActivity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
		if (rotationCfg == 0) {
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		} else if (rotationCfg == 1) {
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		}
		
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void startAnimation() {
		if (!AndroidUtils.isHoneycombOrHigher()) {
			return;
		}
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}
		setImage();
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		mThumbView.getGlobalVisibleRect(startBounds);
		mRootView.getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// thumbView.setAlpha(0f);

		mExpandedImageView.setPivotX(0f);
		mExpandedImageView.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and
		// scale properties
		// (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(mExpandedImageView, View.X, startBounds.left, finalBounds.left)).with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y, startBounds.top, finalBounds.top)).with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_X, startScale, 1f))
				.with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_Y, startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
		final float startScaleFinal = startScale;
		onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}
				if (mPhotoViewAttacher != null && mPhotoViewAttacher.getScale() > 1 || mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					dismiss();
					return;
				}
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator.ofFloat(mExpandedImageView, View.X, startBounds.left)).with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y, startBounds.top)).with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_Y, startScaleFinal));
				set.setDuration(mShortAnimationDuration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mThumbView.setAlpha(1f);
						mExpandedImageView.setImageResource(android.R.color.black);
						mCurrentAnimator = null;
						dismiss();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						mThumbView.setAlpha(1f);
						mExpandedImageView.setImageResource(android.R.color.black);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;

				mRootView.setAnimation(AnimationUtils.loadAnimation(mRootView.getContext(), android.R.anim.fade_out));

			}
		};
		mExpandedImageView.setOnClickListener(onClickListener);
	}

	@Override
	public void dismiss() {
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.dismiss();
	}

	private void setImage() {
		if (mImagePath == null) {
			return;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mImagePath, options);
		DisplayImageOptions opts;
		opts = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).build(); 
		ImageLoader.getInstance().displayImage("file://" + mImagePath, mExpandedImageView, opts);
		mPhotoViewAttacher = new PhotoViewAttacher(mExpandedImageView);
		mPhotoViewAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mPhotoViewAttacher.setOnViewTapListener(BigImageDialog.this);
	}
	

	public void showBigImage(View thumbView, String avatarPath) {
		mThumbView = thumbView;
		mImagePath = avatarPath;

		show();

		MyApplication.getHandler().post(new Runnable() {

			@Override
			public void run() {
				startAnimation();
			}
		});

	}

	@Override
	public void onBackPressed() {
		onClickListener.onClick(mExpandedImageView);
	}

	@Override
	public void onViewTap(View arg0, float arg1, float arg2) {
		onClickListener.onClick(mExpandedImageView);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onViewTap(null, 0, 0);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
