package com.example.richtext;

import android.app.Application;
import android.os.Handler;

import com.example.richtext.utils.DebugTraceTool;

public class MyApplication extends Application {

    private static MyApplication mInstance;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mHandler = new Handler();
        DebugTraceTool.debugTraceE(this, "application onCreate");
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public static Handler getHandler() {
        return getInstance().mHandler;
    }

}
