package com.imageloader.core.process;

import android.graphics.Bitmap;

/**
 * 针对bitmap做一些操作。 
 * 实现的代码必须要保证线程的安全
 * @author renhui
 */
public interface BitmapProcessor {

	/**
	 * 针对bitmap进行一些操作
	 * 此方法需要执行在其他的线程上面(不要在UI线程上面去执行)
	 * 注意：如果返回的是已经创建的新的bitmap,别忘了回收bitmap
	 * @param bitmap
	 * @return
	 */
	Bitmap process(Bitmap bitmap);
}
