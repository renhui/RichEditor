package com.example.richtext.sqlite.tables;

import com.example.richtext.moudle.Note;
import com.example.richtext.utils.DebugTraceTool;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * 便签数据库操作
 * @author renhui
 *
 */
public class NoteAccessor extends TableAccessor {

	public NoteAccessor(SQLiteDatabase database) {
		super(database);
	}
	
	
	/**插入新的便签*/
	public synchronized void insert(Note note) {
		if (note == null || TextUtils.isEmpty(note.content)) {
			DebugTraceTool.debugTraceE(this, "note is null or note content is null, can't insert");
			return;
		}
		
		ContentValues cv = new ContentValues();
		cv.put(Tables.mNoteNativeId, note.nativeId);
		cv.put(Tables.mNoteContent, note.content);
		cv.put(Tables.mNoteModifyTime, note.modifyTime);
		long num = mDatabase.insert(Tables.mNoteTable, null, cv);
		DebugTraceTool.debugTraceE(this, "insert number " + num);
	}
}
