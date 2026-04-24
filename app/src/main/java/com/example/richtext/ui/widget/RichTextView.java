package com.example.richtext.ui.widget;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.richtext.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * 图片文本混排展示控件
 * 
 * @author renhui
 */
public class RichTextView extends LinearLayout {
	
	public static final String IMAGE_SRC_REGEX = "<img[^<>]*?\\ssrc=['\"]?(.*?)['\"].*?>";
	
	private Context mContext;
	private String mContent;
	private SparseArray<String> mImageArray;

	public RichTextView(Context context, String content) {
		super(context);
		mContext = context;
		mContent = content;
		mImageArray = new SparseArray<String>();
		this.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin = 11;
		params.bottomMargin = 11;
		params.gravity = Gravity.CENTER_VERTICAL;
		this.setLayoutParams(params);
		this.removeAllViews();
		createShowView();
	}

	public void createShowView() {
		Matcher m = Pattern.compile(IMAGE_SRC_REGEX).matcher(mContent);
		while (m.find()) {
			mImageArray.append(mContent.indexOf("<img"), m.group(1));
			mContent = mContent.replaceFirst("<img[^>]*>", "");
		}

		if (mImageArray.size() == 0) {
			appendTextView(mContent);
		} else {
			for (int i = 0; i < mImageArray.size(); i++) {
				String s;
				if (i == 0 && (mImageArray.size() - 1 == 0)) {
					s = mContent.substring(0, mImageArray.keyAt(i));
					appendTextView(s);
					appendImageView(mImageArray.valueAt(i));
					s = mContent.substring(mImageArray.keyAt(i), mContent.length());
					appendTextView(s);
				} else if (i == 0) {
					s = mContent.substring(0, mImageArray.keyAt(i));
					appendTextView(s);
					appendImageView(mImageArray.valueAt(i));
				} else if (i == mImageArray.size() - 1) {
					s = mContent.substring(mImageArray.keyAt(i - 1), mImageArray.keyAt(i));
					appendTextView(s);
					s = mContent.substring(mImageArray.keyAt(i), mContent.length());
					appendImageView(mImageArray.valueAt(i));
					appendTextView(s);
				} else {
					s = mContent.substring(mImageArray.keyAt(i - 1), mImageArray.keyAt(i));
					appendTextView(s);
					appendImageView(mImageArray.valueAt(i));
				}
			}
		}
	}

	// 添加图片
	private void appendImageView(final String uri) {
		if (uri == null || uri.isEmpty()) {
			return;
		}

		// 去掉URI里面的空格
		final String uriStr = uri.replaceAll(" ", "");

		LinearLayout imageLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams param = new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		// param.rightMargin = 15;
		imageLayout.setLayoutParams(param);

		final ImageView image = new ImageView(mContext);
		image.setLayoutParams(param);

		image.setScaleType(ScaleType.FIT_XY);
		imageLayout.setGravity(Gravity.CENTER);

		int phone_width = mContext.getResources().getDisplayMetrics().widthPixels; // 屏宽
		showImage(phone_width, uriStr, image);
		imageLayout.addView(image);
		this.addView(imageLayout);
	}

	private void showImage(int phone_width, final String uriStr, final ImageView image) {
		Object model = buildGlideModel(uriStr);
		if (model == null) {
			return;
		}
		Glide.with(mContext)
				.asBitmap()
				.load(model)
				.into(new CustomTarget<Bitmap>() {
					@Override
					public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
						if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
							return;
						}
						int targetWidth = (int) (phone_width * 0.95f);
						int targetHeight = targetWidth * bitmap.getHeight() / bitmap.getWidth();
						LinearLayout.LayoutParams imageParam =
								new LayoutParams(targetWidth, targetHeight);
						image.setLayoutParams(imageParam);
						image.setImageBitmap(bitmap);
						image.invalidate();
					}

					@Override
					public void onLoadCleared(Drawable placeholder) {
						// no-op
					}
				});
	}

	private Object buildGlideModel(String uriStr) {
		if (uriStr == null || uriStr.isEmpty()) {
			return null;
		}
		if (uriStr.startsWith("content://") || uriStr.startsWith("file://")
				|| uriStr.startsWith("http://") || uriStr.startsWith("https://")) {
			return Uri.parse(uriStr);
		}
		if (isFileExist(uriStr)) {
			return new File(uriStr);
		}
		return null;
	}

	// 添加文本内容
	private void appendTextView(String content) {
		// 判断内容是否为空
		if (content == null || content.isEmpty()) {
			return;
		}

		// 如果内容仅仅是回车,不进行显示
		if (content.length() == 1 && content.charAt(0) == '\n') {
			return;
		}

		// 逻辑： <br /> 长度为6 加一个回车字符是7
		if (content.startsWith("<br>") || content.startsWith("<br />")) {
			if (content.length() <= 7) {
				return;
			}
		}

		TextView textView = new TextView(mContext);
		LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setTextSize(18);
		textView.setTextColor(getResources().getColor(R.color.blue_semi_transparent_pressed));
		textView.setPadding(0, 6, 0, 6);
		textView.setLayoutParams(params);
		textView.setText(Html.fromHtml(content));
		this.addView(textView);
	}
	
	/*** 检测图片是否在指定的目录里面 */
	public static boolean isFileExist(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
