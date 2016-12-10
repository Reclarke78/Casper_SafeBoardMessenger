package ru.olgathebest.casper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ольга on 10.12.2016.
 */

public class Dao {
    DBHelper dbHelper;

    Context context;
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
        this.context = context;
        this.dbHelper = dbHelper;

    }

    public long insertMsg(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.d("Logging", "--- Insert in message_history: ---");
        cv.put("msg_id", message.getId());
        cv.put("to_", message.getTo());
        cv.put("from_", message.getFrom());
        cv.put("message_", message.getText());
        cv.put("status_", message.getStatus().toString());
        cv.put("timestamp_", message.getTime().getTime());
        // вставляем запись и получаем ее ID
        long rowID = db.insert("message_history", null, cv);
        Log.d("Logging", "row inserted, ID = " + rowID);
        return rowID;
    }

    public List<Message> getAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursor = db.query("message_history", null, null, null, null, null, null);
        return cursorUtil(cursor);
    }

    public List<Message> cursorUtil(Cursor cursor) {

        messages = new ArrayList<Message>();

        if (cursor.moveToFirst()) {

            int idColInd = cursor.getColumnIndex("msg_id");
            int toColInd = cursor.getColumnIndex("to_");
            int fromColInd = cursor.getColumnIndex("from_");
            int messageColInd = cursor.getColumnIndex("message_");
            int statusColInd = cursor.getColumnIndex("status_");
            int timestampColInd = cursor.getColumnIndex("timestamp_");
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
        selection = "type='table' AND name='message_history'";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursor = db.query("sqlite_master", null, selection, null, null, null, null);
        if (cursor.moveToNext()) {
            selection = String.format(new String(), "to_ = \"%s\" and from_ = \"%s\" or to_ = \"%s\" and from_ = \"%s\"", to, from, from, to);
            cursor = db.query("message_history", null, selection, null, null, null, null);

            return cursorUtil(cursor);
        } else return null;
    }

    public void updateMsg(Message message) {
        //return null;
    }
}
