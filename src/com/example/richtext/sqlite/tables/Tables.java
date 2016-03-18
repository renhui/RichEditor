package com.example.richtext.sqlite.tables;

/**
 * 数据库表定义
 * @author renhui
 */
public class Tables {
	/**********************************************
	 *  SQLite V1
	 **********************************************/
	public static final String mNoteTable = "note";  // 便签表
	public static final String mNoteNativeId = "note_native_id";  // 本地存储id
	public static final String mNoteTitle = "note_title";  // 便签标题
	public static final String mNoteContent = "note_content";  // 便签内容
	public static final String mNoteCreateTime = "note_create_time"; // 便签创建时间 
	public static final String mNoteModifyTime = "note_modify_time"; // 便签修改时间

}
