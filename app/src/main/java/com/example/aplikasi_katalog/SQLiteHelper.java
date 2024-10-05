package com.example.aplikasi_katalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String databaseName = "katalog.db";
    private static final int databaseVersion = 1;

    public SQLiteHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    // Method untuk membuat tabel
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableUser = "CREATE TABLE users (id Integer Primary Key AUTOINCREMENT, username Text, password Text)";
        db.execSQL(createTableUser);
    }

    // Method untuk mengubah database (jika versi di-upgrade)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        db.insert("users", null, values);
        db.close();
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username = ? and password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();

        return exists;
    }

    public boolean checkDuplicate(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();

        return exists;
    }
}
