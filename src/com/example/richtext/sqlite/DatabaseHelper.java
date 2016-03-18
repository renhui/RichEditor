package com.example.richtext.sqlite;

import com.example.richtext.sqlite.tables.SqliteV1;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		new SqliteV1().onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/***数据库升级代码  ---- 目前是第一版,不需要数据库升级**/
//		switch (oldVersion) {
//		case 1:
//			break;
//		default:
//			break;
//		}
	}
}
