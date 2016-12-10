package ru.olgathebest.casper;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Ольга on 10.12.2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "message_history";

    public static final String KEY_ID_MES = "msg_id";
    public static final String KEY_ID= "id";
    public static final String KEY_TO= "to_";
    public static final String KEY_FROM= "from_";
    public static final String KEY_TIMESTAMP = "timestamp_";
    public static final String KEY_CONTENT_MES = "message_";
    public static final String KEY_STATUS_MES = "status_";

    private static final String DATABASE_NAME = "myDB";
    private static final int DATABASE_VERSION = 2;
    public final String TAG = "dataBase";

    private static final String CREATE_TABLE = String.format("CREATE TABLE IF NOT EXISTS %s ("
            + "%s integer primary key autoincrement,"
            + "%s text,"
            + "%s text,"
            + "%s text,"
            + "%s text,"
            + "%s integer,"
            + "%s text"
            + ");", TABLE_NAME, KEY_ID, KEY_ID_MES, KEY_TO, KEY_FROM, KEY_CONTENT_MES, KEY_TIMESTAMP, KEY_STATUS_MES);

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Log", "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }
    }

    ////////////УТИЛИТА ДЛЯ РАБОТЫ С БД/////////////////////////
    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }
    }
}
