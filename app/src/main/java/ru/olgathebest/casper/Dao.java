package ru.olgathebest.casper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.content.ContentValues.TAG;
import static ru.olgathebest.casper.DBHelper.KEY_CONTENT_MES;
import static ru.olgathebest.casper.DBHelper.KEY_FROM;
import static ru.olgathebest.casper.DBHelper.KEY_ID;
import static ru.olgathebest.casper.DBHelper.KEY_ID_MES;
import static ru.olgathebest.casper.DBHelper.KEY_STATUS_MES;
import static ru.olgathebest.casper.DBHelper.KEY_TIMESTAMP;
import static ru.olgathebest.casper.DBHelper.KEY_TO;
import static ru.olgathebest.casper.DBHelper.TABLE_NAME;
import static ru.olgathebest.casper.R.string.email;

/**
 * Created by Ольга on 10.12.2016.
 */

public class Dao {
    DBHelper dbHelper;
    Cursor cursor;
    List<Message> messages;
    // переменные для query
    String[] columns = null;
    String selection = null;
    String[] selectionArgs = null;
    String groupBy = null;
    String having = null;
    String orderBy = null;

    public Dao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;

    }

    public long insertMsg(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d("DB", "--- Insert in message_history: ---");
        cvPut(message,cv);
        // вставляем запись и получаем ее ID
        long rowID = db.insert(TABLE_NAME, null, cv);
        Log.d("Logging", "row inserted, ID = " + rowID);
        return rowID;
    }

    public List<Message> getAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        return cursorUtil(cursor);
    }

    public List<Message> cursorUtil(Cursor cursor) {

        messages = new ArrayList<Message>();

        if (cursor.moveToFirst()) {
            int idColInd = cursor.getColumnIndex(KEY_ID_MES);
            int toColInd = cursor.getColumnIndex(KEY_TO);
            int fromColInd = cursor.getColumnIndex(KEY_FROM);
            int messageColInd = cursor.getColumnIndex(KEY_CONTENT_MES);
            int statusColInd = cursor.getColumnIndex(KEY_STATUS_MES);
            int timestampColInd = cursor.getColumnIndex(KEY_TIMESTAMP);
            do {
                Message message = new Message(cursor.getString(idColInd),
                        cursor.getString(toColInd), cursor.getString(fromColInd),
                        cursor.getString(messageColInd), new Date(cursor.getLong(timestampColInd)),
                        StatusMsg.valueOf(cursor.getString(statusColInd)));
                messages.add(message);
            } while (cursor.moveToNext());

        } else {
            Log.d("Logging", "В базе нет данных!");
        }

        cursor.close();

        return messages;
    }


    public List<Message> getAllWhere(String to, String from) {
        selection = String.format("type='table' AND name='%s'",TABLE_NAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursor = db.query("sqlite_master", null, selection, null, null, null, null);
        if (cursor.moveToNext()) {
            selection = String.format(new String(), "%s = \"%s\" and %s = \"%s\" or %s = \"%s\" and %s = \"%s\"",
                    KEY_TO, to,KEY_FROM, from,KEY_TO, from,KEY_FROM, to);
            cursor = db.query(TABLE_NAME, null, selection, null, null, null, null);
            return cursorUtil(cursor);
        } else return null;
    }

    public void updateMsg(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d("DB", "--- Update mytable: ---");
        // подготовим значения для обновления
        cvPut(message,cv);
        // обновляем по id
        int updCount = db.update(TABLE_NAME, cv, KEY_ID_MES +" = " + "'" + message.getId() + "'",
                null);
        Log.d("DB", "updated rows count = " + updCount);
    }
    public void updateMsgById(Message message,long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d("DB", "--- Update mytable: ---");
        // подготовим значения для обновления
        cvPut(message,cv);
        // обновляем по id
        int updCount = db.update(TABLE_NAME, cv, KEY_ID +" = " + id,
                null);
        Log.d("DB", "updated rows count = " + updCount);
    }
    public void cvPut(Message message, ContentValues cv){
        cv.put(KEY_ID_MES, message.getId());
        cv.put(KEY_TO, message.getTo());
        cv.put(KEY_FROM, message.getFrom());
        cv.put(KEY_CONTENT_MES, message.getText());
        cv.put(KEY_STATUS_MES, message.getStatus().toString());
        cv.put(KEY_TIMESTAMP, message.getTime().getTime());
    }
}
