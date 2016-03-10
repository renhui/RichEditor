package com.example.richtext.sqlite.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Sqlite基本操作
 * 
 * @author renhui
 */
public interface SqliteInterface {

	boolean onCreate(SQLiteDatabase database);

	boolean onUpgrade(SQLiteDatabase database);
}
