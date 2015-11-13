package com.example.richtext.sqlite.tables;

import android.database.sqlite.SQLiteDatabase;

public class SqliteV1 implements SqliteInterface {
	
	/*创建数据库表SQL*/
	private final String mNoteTableCreate = "CREATE TABLE IF NOT EXISTS " + Tables.mNoteTable + "( " + Tables.mNoteNativeId + " TEXT NOT NULL, " + Tables.mNoteContent + " TEXT NOT NULL, " + Tables.mNoteModifyTime + " LONG NOT NULL "  +" );";
	
	@Override
	public boolean onCreate(SQLiteDatabase database) {
		database.execSQL(mNoteTableCreate);
		return true;
	}

	@Override
	public boolean onUpgrade(SQLiteDatabase database) {
		return true;
	}

}
