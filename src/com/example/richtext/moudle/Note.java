package com.example.richtext.moudle;

import java.io.Serializable;

public class Note implements Serializable {

	private static final long serialVersionUID = 1394499517856927779L;
	
	public String nativeId;
	
	public String title;

	public String content;

	public long createTime;
	
	public long modifyTime;

}
