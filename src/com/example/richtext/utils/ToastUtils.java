package com.example.richtext.utils;

import com.example.richtext.MyApplication;

import android.widget.Toast;

/**
 * Toast工具类
 * @author renhui
 */
public class ToastUtils {

    public static void show(final String message) {
        Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void show(final int resId) {
         show(MyApplication.getInstance().getString(resId));
    }
}
