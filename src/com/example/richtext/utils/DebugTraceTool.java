package com.example.richtext.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class DebugTraceTool {

	private static final boolean mIsDebug = true; // 是否输出日志

	public static final int mStackLevel = 5;

	// 获取类名
	static private String getClassName(Object o) {
		if (o != null) {
			return o.getClass().getSimpleName();
		}

		return "NULL";
	}

	static private String getCallStackTop(int level) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		if (stackTrace.length > level) {
			return stackTrace[level - 1].getMethodName();
		}
		return "NULL";
	}

	static public void getCurrentThread(Object o) {
		if (mIsDebug) {
			Log.d(getClassName(o), "Method:" + getCallStackTop(mStackLevel) + ",Thread:" + Thread.currentThread().getName());
		}
	}

	static public void debugTrace(Object o) {
		if (mIsDebug && o != null) {
			Log.d(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static public void debugTrace(Class<?> c) {
		if (mIsDebug && c != null) {
			Log.d(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static public void debugTraceUpdate(Object o, String s) {
		if (mIsDebug && o != null) {
			if (s != null) {
				Log.w(getClassName(o), "[Update]: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.w(getClassName(o), "[Update]: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceCritical(Object o) {
		if (mIsDebug && o != null) {
			Log.w(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static public void debugTraceCritical(Class<?> c) {
		if (mIsDebug && c != null) {
			Log.w(getClassName(c), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static public void debugTraceE(Object o) {
		if (mIsDebug && o != null) {
			Log.e(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static public void debugTraceE(Class<?> c) {
		if (mIsDebug && c != null) {
			Log.e(getClassName(c), "Method trace: " + getCallStackTop(mStackLevel));
		}
	}

	static private String callStackToString(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	static public void debugTraceException(Class<?> cls, Exception e) {
		if (mIsDebug) {
			Log.e(cls.getClass().getSimpleName(), ",Exception:" + e.getLocalizedMessage() + ", CallStack:" + callStackToString(e));
		}
	}

	static public void debugTrace(Object o, String s) {
		if (mIsDebug && o != null) {
			if (s != null) {
				Log.d(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.d(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTrace(Class<?> c, String s) {
		if (mIsDebug && c != null) {
			if (s != null) {
				Log.d(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.d(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceCritical(Object o, String s) {
		if (mIsDebug && o != null) {
			if (s != null) {
				Log.w(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.w(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceCritical(Class<?> c, String s) {
		if (mIsDebug && c != null) {
			if (s != null) {
				Log.w(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.w(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceE(Object o, String s) {
		if (mIsDebug && o != null) {
			if (s != null) {
				Log.e(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.e(getClassName(o), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceE(Class<?> c, String s) {
		if (mIsDebug && c != null) {
			if (s != null) {
				Log.e(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel) + ",Message:" + s);
			} else {
				Log.e(c.getSimpleName(), "Method trace: " + getCallStackTop(mStackLevel));
			}
		}
	}

	static public void debugTraceException(Object o, Exception e) {
		if (mIsDebug) {
			Log.e(getClassName(o), ",Exception:" + e.getLocalizedMessage() + ", CallStack:\n" + callStackToString(e));
		}
	}
}

