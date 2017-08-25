package com.example.richtext.ui.widget.handwriting;

import com.example.richtext.R;
import com.example.richtext.ui.widget.handwriting.listener.WriteDialogListener;
import com.example.richtext.utils.ToastUtils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * 手写板对话框
 * 
 * @author renhui
 *
 */
public class WritePadDialog extends Dialog implements View.OnClickListener {

	private Context mContext;
	private WriteDialogListener mWriteDialogListener;
	private PaintView mPaintView;
	private FrameLayout mFrameLayout;
	private Button mBtnOk, mBtnClear, mBtnCancle;

	public WritePadDialog(Context context,
			WriteDialogListener writeDialogListener) {
		super(context);
		this.mContext = context;
		this.mWriteDialogListener = writeDialogListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 无标题

		setContentView(R.layout.dialog_write_pad);

		mFrameLayout = (FrameLayout) findViewById(R.id.table_view);

		// 获取屏幕尺寸
		DisplayMetrics metrics = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;

		mPaintView = new PaintView(mContext, screenWidth, screenHeight);
		mFrameLayout.addView(mPaintView);
		mPaintView.requestFocus();

		mBtnOk = (Button) findViewById(R.id.write_pad_ok);
		mBtnClear = (Button) findViewById(R.id.write_pad_clear);
		mBtnCancle = (Button) findViewById(R.id.write_pad_cancel);
		mBtnOk.setOnClickListener(this);
		mBtnClear.setOnClickListener(this);
		mBtnCancle.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.write_pad_ok:
			if (mPaintView.getPath().isEmpty()) {
				ToastUtils.show("没有输入任何内容..");
				return;
			}
			
			mWriteDialogListener.onPaintDone(mPaintView.getPaintBitmap());
			dismiss();
			break;
		case R.id.write_pad_clear:
			mPaintView.clear();
			break;
		case R.id.write_pad_cancel:
			cancel();
			break;
		default:
			break;
		}
	}

}
