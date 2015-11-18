package com.example.richtext.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.example.richtext.R;
import com.example.richtext.imageloader.core.ImageLoader;
import com.example.richtext.moudle.EditData;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


/**
 * 以ScrollView为父容器的富文本编辑器
 * @author renhui
 */
public class RichEditor extends ScrollView {
	
	private static final int EDIT_PADDING = 10; // editText正常的padding值 =10dp
	private static final int EDIT_FIRST_PADDING_TOP = 10;  // 第一个EidtText的Padding值  .. 此值是否真的有用,待考究
	
	private int disappearingImageIndex = 0;	// 消失的ImageView在Layout的index
	private int editNormalPadding = 0; // 编辑框正常的padding值
	private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
	
	private OnKeyListener mKeyListener;	// 键盘监听	
	private OnClickListener mCloseBtnListener;	// 图片叉号点击的监听
	private OnFocusChangeListener mFocusChangeListener;	// EditText焦点变化的监听
	private LayoutInflater mInflater;  // 获取到通过XML文件插入View的SystemService
	private LinearLayout mContainerLayout;  // 所有的子view的容器, scrollView内唯一的ViewGroup.
	private EditText mLastFocusEdit; // 获的最近的获得焦点的EditText
	private LayoutTransition mTransition;  // 在图片View添加或者remove时候,触发transition动画

	// 必备三个构造函数
	public RichEditor(Context context) {
		this(context, null);
	}
	
	public RichEditor(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mInflater = LayoutInflater.from(context);  // 获取从xml添加view的Service
		// init containerlayout
		mContainerLayout = new LinearLayout(context);
		mContainerLayout.setOrientation(LinearLayout.VERTICAL);
		mContainerLayout.setBackgroundColor(Color.WHITE);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		setupLayoutTransitions(mContainerLayout);
		addView(mContainerLayout, layoutParams);
		
		// 编辑框的退格键的监听,用来处理点击此按钮时可能需要view合并的操作
		mKeyListener = new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					EditText editText = (EditText) v;
					onBackspacePress(editText);
				}
				return false;
			}
		};
		
		// 图片点击“叉号”按钮的监听
		mCloseBtnListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RelativeLayout parentView = (RelativeLayout) v.getParent();
				onImageCloseClick(parentView);
			}
		};
		
		// edittext焦点变化的事件监听
		mFocusChangeListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mLastFocusEdit = (EditText) v;
				}
			}
		};
		
		// 创建默认的Edit
		LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		editNormalPadding = dip2px(EDIT_PADDING);
		EditText firstEdit = createEditText("请输入内容...", dip2px(EDIT_FIRST_PADDING_TOP));
		mContainerLayout.addView(firstEdit, firstEditParam);
		mLastFocusEdit = firstEdit;
	}
	
	/** 创建文本输入框   */
	@SuppressLint("InflateParams")
	private EditText createEditText(String hint, int paddingTop) {
		EditText editText = (EditText) mInflater.inflate(R.layout.editor_item, null);
		editText.setOnKeyListener(mKeyListener);
		editText.setTag(viewTagIndex++);
		editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, 0);
		editText.setHint(hint);
		editText.setOnFocusChangeListener(mFocusChangeListener);
		return editText;
	}
	
	// 创建图片显示的view
	@SuppressLint("InflateParams")
	private RelativeLayout createImageLayout() {
		RelativeLayout layout = (RelativeLayout) mInflater.inflate(R.layout.edit_imageview, null);
		layout.setTag(viewTagIndex++);
		View closeView = layout.findViewById(R.id.image_close);
		closeView.setTag(layout.getTag());
		closeView.setOnClickListener(mCloseBtnListener);
		return layout;
	}
	
	
	// dp和pixel转换
	public int dip2px(float dipValue) {
		float m = getContext().getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}
	
	
	// 软键盘回退事件处理 
	private void onBackspacePress(EditText editText) {
		int startSelection = editText.getSelectionStart();
		// 判断光标是否已经顶到了文本框的最前方,根据获得的状态来判断是否删除之前的图片并合并两个View
		if (startSelection == 0) {
			int editIndex = mContainerLayout.indexOfChild(editText);  // 获取当前的eidtText在容器内的索引
			View preView = mContainerLayout.getChildAt(editIndex - 1); // 如果editIndex-1 < 0,则会返回null
			if (preView != null) {
				if (preView instanceof RelativeLayout) {
					// EditText的上一个View是图片
					onImageCloseClick(preView);
				} else if (preView instanceof EditText) {
					// 光标EditText的上一个View对应的还是文本框EditText
					String str1 = editText.getText().toString();
					EditText preEdit = (EditText) preView;
					String str2 = preEdit.getText().toString();
					
					// 合并两个EditText
					mContainerLayout.setLayoutTransition(null);
					mContainerLayout.removeView(editText);
					mContainerLayout.setLayoutTransition(mTransition);
					
					// 文本合并
					preEdit.setText(str2 + str1);
					preEdit.requestFocus();
					preEdit.setSelection(str2.length(), str2.length());  // 放置光标
					mLastFocusEdit = preEdit;
				}
			}
		}
	}
	
	private void onImageCloseClick(View view) {
		if (mTransition != null && !mTransition.isRunning()) {
			disappearingImageIndex = mContainerLayout.indexOfChild(view);
			mContainerLayout.removeView(view);
		}
	}
	
	/** 为指定设定transition动画监听   */
	private void setupLayoutTransitions(LinearLayout layout) {
		mTransition = new LayoutTransition();
		layout.setLayoutTransition(mTransition);
		mTransition.addTransitionListener(new TransitionListener() {
			@Override
			public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
			}
			
			@Override
			public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
				// 动画结束了,而且动画是消失的动画
				if (!transition.isRunning() && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
					// transition 动画结束, 合并EditText
					mergeEditText();
				}
			}
		});
		// 设定view添加时的动画时间
		mTransition.setDuration(300);
		mContainerLayout.setLayoutTransition(mTransition);
	}
	
	
	/** 图片删除完成后,如果上下方都是EditText,则将两个EditText的内容进行合并处理*/
	private void mergeEditText() {
		Log.d("richEditor", "~ 合并Edit操作  ~");
		View preView = mContainerLayout.getChildAt(disappearingImageIndex -1);
		View nextView = mContainerLayout.getChildAt(disappearingImageIndex);
		
		// 判断上下方是否都为EditText,
		if (preView != null && preView instanceof EditText && nextView != null && nextView instanceof EditText) {
			EditText preEdit = (EditText) preView;
			EditText nextEdit = (EditText) nextView;
			String str1 = preEdit.getText().toString();
			String str2 = preEdit.getText().toString();
			String mergeText = "";
			if (str2.length() > 0) {
				mergeText = str1 + "\n" + str2;
			} else {
				mergeText = str1;
			}
			
			mContainerLayout.setLayoutTransition(null);
			mContainerLayout.removeView(nextEdit);
			preEdit.setText(mergeText);
			preEdit.requestFocus();
			preEdit.setSelection(str1.length(), str1.length());
			mContainerLayout.setLayoutTransition(mTransition);
		}
	}
	
	/**
	 * 根据View的宽度,动态的缩放bitmap的尺寸
	 * @param filePath 图片的路径
	 * @param width view的宽度
	 * @return
	 */
	private Bitmap getScaledBitmap(String filePath, int width) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		int sampleSize = options.outWidth > width ? options.outWidth / width + 1 : 1;
		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleSize;
		return BitmapFactory.decodeFile(filePath, options);
	}
	
	
	/** 富文本控件插入图片的对外接口  */
	public void insertImage(String imagePath) {
		Bitmap bmp = getScaledBitmap(imagePath, getWidth());
		insertImage(bmp, imagePath);
	}
	
	/**插入一张图片*/
	private void insertImage(Bitmap bitmap, String imagePath) {
		String lastEditStr = mLastFocusEdit.getText().toString();
		int cursorIndex = mLastFocusEdit.getSelectionStart();
		String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
		int lastEditIndex = mContainerLayout.indexOfChild(mLastFocusEdit);
		
		if (lastEditStr.length() == 0 || editStr1.length() == 0) {
			// 如果EditText为空了,或者光标已经在editText的最前面,则直接插入图片，往下移动EditText
			addImageViewAtIndex(lastEditIndex, bitmap, imagePath);
		} else {
			// 如果EditText不为空而且光标不在最顶端,则需要添加新的imageView和EditText
			mLastFocusEdit.setText(editStr1);
			String editStr2 = lastEditStr.substring(cursorIndex).trim();
			if (mContainerLayout.getChildCount() -1 == lastEditIndex || editStr2.length() > 0) {
				addEditTextAtIndex(lastEditIndex + 1,  editStr2);
			}
			
			addImageViewAtIndex(lastEditIndex + 1,  bitmap, imagePath);
			mLastFocusEdit.requestFocus();
			mLastFocusEdit.setSelection(editStr1.length(), editStr1.length());
		}
		hideKeyBoard();
	}
	
	
	/**
	 * 在特定位置插入EditText
	 * @param index  位置
	 * @param editStr EditText显示的文字
	 */
	private void addEditTextAtIndex(final int index, String editStr) {
		EditText editText2 = createEditText("", getResources().getDimensionPixelSize(R.dimen.edit_padding_top));
		editText2.setText(editStr);

		// 请注意此处，EditText添加、或删除不触动Transition动画
		mContainerLayout.setLayoutTransition(null);
		mContainerLayout.addView(editText2, index);
		mContainerLayout.setLayoutTransition(mTransition); // remove之后恢复transition动画
	}
	
	/** 在指定的位置添加图片   */
	private void addImageViewAtIndex(final int index, Bitmap bmp, String imagePath) {
		final RelativeLayout imageLayout = createImageLayout();
		RichImageView imageView = (RichImageView) imageLayout.findViewById(R.id.edit_imageView);
		imageView.setPicturePath(imagePath);
		ImageLoader.getInstance().displayImage("file://" + imagePath, imageView);
		int imageHeight = getWidth() * bmp.getHeight() / bmp.getWidth();
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);
		imageView.setLayoutParams(lp);
		
		// 在onActivityResult处进行UI处理无法触发动画,so在此处post处理
		mContainerLayout.postDelayed(new Runnable() {
			@Override
			public void run() {
				mContainerLayout.addView(imageLayout, index);
			}
		}, 200);
}
	
	/**隐藏软键盘*/
	public void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromInputMethod(mLastFocusEdit.getWindowToken(), 0);
	}
	
	
	public List<EditData> buildEditData() {
		List<EditData> dataList = new ArrayList<EditData>();
		int num = mContainerLayout.getChildCount();
		for (int index = 0; index < num; index++) {
			View itemView = mContainerLayout.getChildAt(index);
			EditData itemData = new EditData();
			if (itemView instanceof EditText) {
				EditText item = (EditText) itemView;
				itemData.inputStr = item.getText().toString();
			} else if (itemView instanceof RelativeLayout) {
				RichImageView item = (RichImageView) itemView.findViewById(R.id.edit_imageView);
				itemData.imagePath = item.getPicturePath();
			}
			dataList.add(itemData);
		}
		
		return dataList;
	}
}
