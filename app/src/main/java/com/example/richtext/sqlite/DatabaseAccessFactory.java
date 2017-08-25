package com.example.richtext.sqlite;

import com.example.richtext.sqlite.tables.NoteAccessor;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库访问工厂
 * 
 * @author renhui
 *
 */
public class DatabaseAccessFactory {
	private static final String mDbName = "richtext.db";
	private static final int mDbVersion = 1;

	private static DatabaseAccessFactory mInstance;
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	private NoteAccessor mNoteAccessor;

	private DatabaseAccessFactory(Context context) {
		super();
		mContext = context.getApplicationContext();
		if (mDatabase ==null || !mDatabase.isOpen()) {
			openDatabase();
		}
		
		this.mNoteAccessor = new NoteAccessor(this.mDatabase);

	}

	/** 获取数据库访问实例 */
	public static synchronized DatabaseAccessFactory getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseAccessFactory(context);
		}

		return mInstance;
	}

	public NoteAccessor noteAccessor() {
		return this.mNoteAccessor;
	}

	public static synchronized void shutdown() {
		if (mInstance != null) {
			mInstance.shutdownInternal();
			mInstance = null;
		}
	}

	private void shutdownInternal() {
		this.mDatabase.close();
		this.mDatabaseHelper.close();
	}

	private synchronized void openDatabase() throws SQLException {
		mDatabaseHelper = new DatabaseHelper(mContext, mDbName, null, mDbVersion);
		mDatabase = mDatabaseHelper.getWritableDatabase();
	}
}
