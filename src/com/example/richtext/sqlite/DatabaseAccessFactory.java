package com.example.richtext.sqlite;

import com.example.richtext.sqlite.tables.NoteAccessor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库访问工厂
 * @author renhui
 *
 */
public class DatabaseAccessFactory {
	
	private static DatabaseAccessFactory mInstance;  
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;
	
	private NoteAccessor mNoteAccessor;
	
	private DatabaseAccessFactory(Context context) {
		super();
		
		this.mDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
		this.mDatabase = this.mDatabaseHelper.getDatabase();
		
		this.mNoteAccessor = new NoteAccessor(this.mDatabase);
		
	}
	
	
	/**获取数据库访问实例 */
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
	
	

}
