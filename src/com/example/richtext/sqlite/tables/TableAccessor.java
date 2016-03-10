package com.example.richtext.sqlite.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库表访问基类
 * 
 * @author renhui
 */
abstract public class TableAccessor {
	
	protected SQLiteDatabase mDatabase;
	
	public TableAccessor(SQLiteDatabase database) {
		if (database == null) {
			throw new IllegalArgumentException("Must send a valid SQLiteDatabase object-reference");
		}
		
		this.mDatabase = database;
	}

}
