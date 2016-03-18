package com.example.richtext.ui.widget.handwriting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.Config;
import android.view.MotionEvent;
import android.view.View;

/**
 * 能够绘图的View
 * 
 * @author renhui
 *
 */
@SuppressLint("ClickableViewAccessibility")
public class PaintView extends View {
	
	private Paint mPaint;  // 画笔
	private Path mPath;   // 绘图路径
	private Bitmap mBitmap;  // 位图
	private Canvas mCanvas;  // 画布
	
	private int screenWidth, screenHeight; // 屏幕宽高
	private float currentX, currentY;   // 当前的坐标

	public PaintView(Context context, int screenWidth, int screenHeight) {
		super(context);
		
		this.screenWidth = screenWidth;
		this.screenHeight = (int) (screenHeight * 0.9); // 微调画布的高度
		init();
	}
	
	private void init() {
		// 初始化画笔 、 位图、 画布
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);   // 画笔样式
		mPaint.setAntiAlias(true);  // 去除锯齿
		mPaint.setDither(true);  // 抖动处理，使得绘出来的图片更加平滑和饱满
		mPaint.setStrokeWidth(4);  // 画笔大小
		mPaint.setStrokeCap(Paint.Cap.ROUND);  // 画笔 圆角
		
		mPath = new Path();
		
		mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, null);
		canvas.drawPath(mPath, mPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentX = x;
			currentY = y;
			mPath.moveTo(currentX, currentY);
			break;
		case MotionEvent.ACTION_MOVE:
			currentX = x;
			currentY = y;
			mPath.quadTo(currentX, currentY, x, y);  // 移动时划线
			break;
		case MotionEvent.ACTION_UP:
			mCanvas.drawPath(mPath, mPaint);
			break;
		default:
			break;
		}
		
		invalidate();
		return true;
	}
	
	public Bitmap getPaintBitmap() {
		return resizeImage(mBitmap, 640, (int) (640 * ((float)screenHeight / (float)screenWidth)));
	}
	
	public Path getPath() {
		return mPath;
	}
	
	/**缩放*/
	public static Bitmap resizeImage(Bitmap bitmap, int width, int height) {
		int originWidth = bitmap.getWidth();
		int originHeight = bitmap.getHeight();
		
		float scaleWidth = ((float) width) / originWidth;
		float scaleHeight = ((float) height) / originHeight;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, originWidth, originHeight, matrix, true);
		return resizeBitmap;
	}
	
	/**清楚画板*/
	public void clear() {
		if (mCanvas != null) {
			mPath.reset();
			mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			invalidate();
		}
	}
	
	
	
	

}
