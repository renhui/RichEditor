package com.example.richtext;

import com.example.richtext.imageloader.core.ImageLoader;
import com.example.richtext.imageloader.core.ImageLoaderConfiguration;
import com.example.richtext.utils.DebugTraceTool;

import android.app.Application;

public class MyApplication extends Application {
	
	private static MyApplication mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		DebugTraceTool.debugTraceE(this, "application onCreate");
		
		// 初始化图片加载控件
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);
	}
	
	public static MyApplication getInstance() {
		return mInstance;
	}

}
