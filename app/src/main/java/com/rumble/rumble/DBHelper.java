package com.rumble.rumble;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "phone.db";

    public DBHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
        Log.d("LogNumber", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("LogNumber", "onCreate");
        sqLiteDatabase.execSQL("CREATE TABLE Person(phoneNumber TEXT primary key)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Person");
        onCreate(sqLiteDatabase);
    }

    public void insert(String phoneNumber) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Person VALUES('" + phoneNumber + "')");
        db.close();
    }

    public void Delete(String phoneNumber) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Person WHERE phoneNumber = '" + phoneNumber + "'");
        db.close();
    }

    public List<String> getResult() {
        List<String> ret = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Person", null);
        while(cursor.moveToNext()) {
            ret.add(cursor.getString(0));
        }

        db.close();
        return ret;
    }
}

