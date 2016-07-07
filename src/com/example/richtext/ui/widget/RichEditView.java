package com.example.richtext.ui.widget;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.example.richtext.R;
import com.example.richtext.utils.ImageUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * 图文混排编辑界面
 * 
 * 注：主要是通过EditText来完成相关的再编辑处理 {@link RichEditor} 完成的是首次创建的编辑操作
 * ,因为RichEditor编辑效果好，但是不能做为再编辑的控件
 * 
 * {@link RichEditView} 可以作为初次编辑的控件，也可以作为再次编辑的控件，实用性更高
 * 
 * @author RenHui
 */
public class RichEditView extends LinearLayout {

	private Context mContext;
	private EditText mEditText;
	private String mContent;
	private SparseArray<SparseArray<String>> mImageArray;

	@SuppressWarnings("deprecation")
	public RichEditView(Context context, String content) {
		super(context);
		
		mContext = context;
		mContent = content;
		mImageArray = new SparseArray<SparseArray<String>>();
		mEditText = new EditText(mContext);
		LinearLayout.LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mEditText.setLayoutParams(params);
		mEditText.setTextColor(getResources().getColor(R.color.mix_edit_text_color));
		mEditText.setBackgroundDrawable(null);
		createShowView();
	}

	private void createShowView() {
		final int phone_width = mContext.getResources().getDisplayMetrics().widthPixels;
		mContent = mContent.replace("<img src=\"\" />", "");
		Matcher m = Pattern.compile(RichTextView.IMAGE_SRC_REGEX).matcher(mContent);
		while (m.find()) {
			int i = mContent.indexOf("<img");
			String tempStr = Html.fromHtml(mContent.substring(0, i)).toString();
			String urlStr = m.group(1).replaceAll(" ", "");
			if (mImageArray.get(tempStr.length()) == null) {
				SparseArray<String> urlArray = new SparseArray<String>();
				urlArray.append(0, urlStr);
				mImageArray.append(tempStr.length(), urlArray);
			} else {
				SparseArray<String> tempArray = mImageArray.get(tempStr.length());
				tempArray.append(tempArray.size(), urlStr);
				mImageArray.append(tempStr.length(), tempArray);
			}
			mContent = mContent.replaceFirst("<img[^>]*>", "");
		}

		mEditText.setText(Html.fromHtml(mContent));
		if (mImageArray.size() == 0) {
			this.addView(mEditText);
			return;
		}

		for (int i = mImageArray.size() - 1; i >= 0; i--) {
			if (mImageArray.valueAt(i) == null) {
				continue;
			}
			for (int j = mImageArray.valueAt(i).size() - 1; j >= 0; j--) {
				if (mImageArray.valueAt(i).valueAt(j) == null || mImageArray.valueAt(i).valueAt(j).isEmpty()) {
					continue;
				}

				Bitmap img = null;
				String fs = "";

				// 判断路径是否是完整的本地路径
				if (mImageArray.valueAt(i).valueAt(j) != null && mImageArray.valueAt(i).valueAt(j).startsWith("/storage")) {
					fs = mImageArray.valueAt(i).valueAt(j);
				}
				File file = new File(fs);

				if (file.exists()) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(fs, options);
					if (phone_width > 480) {
						int inSampleSize = ImageUtils.reckonInSampleSizeForBig(options, (int) (phone_width * 0.85));
						options.inSampleSize = inSampleSize;
					} else {
						int inSampleSize = ImageUtils.reckonInSampleSizeForSmall(options, (int) (phone_width * 0.85));
						options.inSampleSize = inSampleSize;
					}

					options.inJustDecodeBounds = false;
					img = changeBitmap(BitmapFactory.decodeFile(fs, options));
					inSertImage(img, i, j);
				}
			}
		}
		this.addView(mEditText);
	}

	public void inSertImage(Bitmap img, int i, int j) {
		ImageSpan urlSpan = new ImageSpan(ImageUtils.convert(mContext, img), mImageArray.valueAt(i).valueAt(j));
		SpannableString string = new SpannableString(" ");
		string.setSpan(urlSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		Editable editable = mEditText.getEditableText();
		int index = mImageArray.keyAt(i);
		if (index == 0) {
			editable.insert(index, Html.fromHtml("<br>"));
			editable.insert(index, string);
		} else {
			editable.insert(index, Html.fromHtml("<br>"));
			editable.insert(index, string);
			editable.insert(index, Html.fromHtml("<br>"));
		}
	}

	/** 设定获取当前的光标焦点的位置 */
	public int getSelectionStart() {
		if (!mEditText.hasFocus()) {
			return -1;
		}
		return mEditText.getSelectionStart();
	}

	/**设定当前编辑框的光标的位置 */
	public void setSelection(int index) {
		mEditText.setSelection(index);
	}

	/** 获取当前编辑框的内容（此内容为可编辑的） */
	public Editable getEditableText() {
		return mEditText.getEditableText();
	}

	/** 获取当前编辑框的内容，去除 <p> 和  </p> 标签，保持换行格式的正确*/
	public String getEditableTextString() {
		String htmlContent = Html.toHtml(mEditText.getEditableText());
		htmlContent = htmlContent.replaceFirst("<p[^>]*>", "");
		if (htmlContent != null && htmlContent.length() > 5) {
			htmlContent = htmlContent.substring(0, htmlContent.length() - 5);
		}
		String ss = StringEscapeUtils.unescapeHtml4(htmlContent);
		return ss;
	}

	/**
	 * 编辑界面的图片放大规则
	 * 
	 * @param bitmap
	 * @return
	 */
	public Bitmap changeBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int phone_width = mContext.getResources().getDisplayMetrics().widthPixels;
		int width = bitmap.getWidth();
		float scale = ((float) phone_width / (float) ImageUtils.standardWidth); // 放大倍数
		if (width * scale > phone_width) {
			scale = (float) ((float) phone_width / (float) width);
		}
		
		if (scale != 1) {
			bitmap = ImageUtils.reSizeBitmap(bitmap, scale);
		}
		
		if (((float) bitmap.getWidth()) > ((float) (ImageUtils.SCALE_NUM_75 * phone_width))) {
			return ImageUtils.reSizeBitmap(bitmap, ImageUtils.SCALE_NUM_75);
		} else {
			return bitmap;
		}
	}
}
