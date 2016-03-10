package com.example.richtext.utils;

/**
 * 长微博内容处理工具类
 * 
 * @author renhui
 */
public final class LongBlogContent {

	private static LongBlogContent instance;
	
	private int height; // EditText内文本的行数
	private String[] weiboContent = new String[1000]; // 文本内容数组
	

	private LongBlogContent() {
	}

	public synchronized static LongBlogContent getInstance() {
		if (instance == null) {
			instance = new LongBlogContent();
		}

		return instance;
	}

	public void handleText(String content, int wordNum) {
		String[] textsplit = new String[10000];
		textsplit = content.split("\n");
		int i = 0;
		for (int j = 0; j < textsplit.length; j++) {
			content = textsplit[j];
			if (content.length() > wordNum) {
				int k = 0;
				while (k + wordNum <= content.length()) {
					weiboContent[i++] = content.substring(k, k + wordNum);
					k = k + wordNum;
				}
				weiboContent[i++] = content.substring(k, content.length());

			} else {
				weiboContent[i++] = content;
			}
		}
		this.height = i;

	}
	
	
	/**
	 * 返回EditText内容的高度
	 * @return
	 */
	public int getHeight(){
		return height;
	}
	
	/**
	 * 返回生成的长微博内容
	 * @return
	 */
	public String[] getContent(){
		return weiboContent;
	}
	
	public void clearStatus() {
		height =0;  
		weiboContent = new String[1000];
	}

}
