package com.example.richtext.imageloader.core.assist;


/**
 * 列举图片加载和显示失败的原因
 * @author renhui
 */
public class FailReason {
	
	private final FailType type;
	
	private final Throwable cause;
	
	public FailReason(FailType type, Throwable cause) {
		this.type = type;
		this.cause = cause;
	}
	
	public FailType getType() {
		return type;
	}

	public Throwable getCause() {
		return cause;
	}

	/**列举一些加载失败的类型*/
	public static enum FailType {
		/** I/O 出现异常.可能是因为网络交互出现问题,也可能是因为缓存图片的时候文件系统出现异常*/
		IO_ERROR,
		
		/** 解码失败*/
		DECODING_ERROR,
		
		/** 网络下载被拒绝,而且之前图片没有缓存到磁盘中*/
		NETWORK_DENIED,
		
		/** 创建图片的位图的时候内存不够*/
		OUT_OF_MEMORY,
		
		/** 在加载图片时发生未知错误*/
		UNKNOWN
	}

}
