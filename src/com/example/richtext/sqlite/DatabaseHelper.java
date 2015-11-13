package com.example.richtext.sqlite;

import java.io.File;
import com.example.richtext.sqlite.tables.SqliteV1;
import com.example.richtext.utils.DebugTraceTool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库适配器
 * 
 * @author renhui
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String mDbFolder = "richtext_sqlite";
	private static final String mDbName = "richtext.db";
	private static final int mDbVersion = 0;

	private SQLiteDatabase mDatabase;

	public DatabaseHelper(Context context) {
		super(context, mDbName, null, mDbVersion);

		String pathToSqlite = context.getDatabasePath(mDbFolder).getPath();
		DebugTraceTool.debugTraceE(this, "Dir:" + pathToSqlite);

		File sqliteDir = new File(pathToSqlite);

		if (!sqliteDir.exists() || !sqliteDir.isDirectory()) {
			sqliteDir.mkdirs();
		}

		new File(sqliteDir + File.separator + mDbName);

		DebugTraceTool.debugTrace(this, "DB Path:" + pathToSqlite
				+ File.separator + mDbName);

		mDatabase = SQLiteDatabase.openDatabase(pathToSqlite + File.separator
				+ mDbName, null, SQLiteDatabase.CREATE_IF_NECESSARY
				| SQLiteDatabase.OPEN_READWRITE);

		if (mDatabase != null) {
			int version = mDatabase.getVersion();

			DebugTraceTool.debugTrace(this, "Current Version:" + mDbVersion
					+ ", DB Version:" + version);

			if (version != mDbVersion) {
				mDatabase.beginTransaction();

				try {
					if (version == 0) {
						onCreate(mDatabase);
					} else {
						onUpgrade(mDatabase, version, mDbVersion);
					}
					
					mDatabase.setVersion(mDbVersion);
					mDatabase.setTransactionSuccessful();
				} finally {
					mDatabase.endTransaction();
				}
			}
		} else {
			DebugTraceTool.debugTrace(this, "Create Database error!");
		}
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
	
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}
}
