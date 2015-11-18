package com.example.richtext.sqlite.tables;

import java.util.ArrayList;

import com.example.richtext.moudle.Note;
import com.example.richtext.utils.DebugTraceTool;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * Note数据库操作
 * @author renhui
 *
 */
public class NoteAccessor extends TableAccessor {
	
	private static final String[] noteColumnList = new String[] {Tables.mNoteNativeId, Tables.mNoteContent, Tables.mNoteModifyTime};

	public NoteAccessor(SQLiteDatabase database) {
		super(database);
	}
	
	private Note getNote(Cursor c) {
		Note note = new Note();
		note.nativeId = c.getString(c.getColumnIndex(Tables.mNoteNativeId));
		note.title = c.getString(c.getColumnIndex(Tables.mNoteTitle));
		note.content = c.getString(c.getColumnIndex(Tables.mNoteContent));
		note.createTime = c.getLong(c.getColumnIndex(Tables.mNoteCreateTime));
		note.modifyTime = c.getLong(c.getColumnIndex(Tables.mNoteModifyTime));
		return note;
	}
	
	
	/**插入新的便签*/
	public synchronized void insert(Note note) {
		if (note == null || TextUtils.isEmpty(note.content)) {
			DebugTraceTool.debugTraceE(this, "note is null or note content is null, can't insert");
			return;
		}
		
		ContentValues cv = new ContentValues();
		cv.put(Tables.mNoteNativeId, note.nativeId);
		cv.put(Tables.mNoteTitle, note.title);
		cv.put(Tables.mNoteContent, note.content);
		cv.put(Tables.mNoteCreateTime, note.createTime);
		cv.put(Tables.mNoteModifyTime, note.modifyTime);
		long num = mDatabase.insert(Tables.mNoteTable, null, cv);
		DebugTraceTool.debugTraceE(this, "insert number " + num);
	}
	
	
	/** 查询所有的便签  */
	public ArrayList<Note> getNotes() {
		ArrayList<Note> list = new ArrayList<Note>();
		Cursor cursor = mDatabase.query(Tables.mNoteTable, noteColumnList, null, null, null, null, Tables.mNoteModifyTime);
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				list.add(getNote(cursor));
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
	
}
