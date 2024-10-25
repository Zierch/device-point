package com.example.aplikasi_katalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String databaseName = "katalog.db";
    private static final int databaseVersion = 4;

    public SQLiteHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    // Method untuk membuat tabel
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableUser = "CREATE TABLE users (id INTEGER Primary Key AUTOINCREMENT, username Text, password Text)";
        String createTableProduct = "CREATE TABLE products (id Integer Primary Key AUTOINCREMENT, image TEXT, kategori TEXT, nama_produk TEXT, harga INTEGER, merek TEXT, garansi TEXT, stok INTEGER, deskripsi TEXT)";
        db.execSQL(createTableUser);
        db.execSQL(createTableProduct);

//        set admin
        ContentValues values = new ContentValues();
        values.put("username", "admin");
        values.put("password", "admin123");
        db.insert("users", null, values);
    }

    // Method untuk mengubah database (jika versi di-upgrade)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < databaseVersion) {
            String createTableProduct = "CREATE TABLE IF NOT EXISTS products (id INTEGER Primary Key AUTOINCREMENT, image TEXT, kategori TEXT, nama_produk TEXT, harga INTEGER, merek TEXT, garansi TEXT, stok INTEGER, deskripsi TEXT)";
            db.execSQL(createTableProduct);
        }

        // Jika upgrade, periksa apakah admin sudah ada, jika belum tambahkan
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{"admin"});
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put("username", "admin");
            values.put("password", "admin123");
            db.insert("users", null, values);
        }
        cursor.close();
    }

    public void insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        db.insert("users", null, values);
        db.close();
    }

    public void insertProduct(String imagePath, String kategori, String namaProduk, int harga, String merek, String garansi, int stok, String deskripsi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("image", imagePath);  // Simpan path gambar
        values.put("kategori", kategori);
        values.put("nama_produk", namaProduk);
        values.put("harga", harga);
        values.put("merek", merek);
        values.put("garansi", garansi);
        values.put("stok", stok);
        values.put("deskripsi", deskripsi);

        db.insert("products", null, values);
        db.close();
    }
    public void updateProduct(int id, String imagePath, String kategori, String namaProduk, int hargaProduk, String merek, String garansi, int stok, String deskripsi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image", imagePath);
        values.put("kategori", kategori);
        values.put("nama_produk", namaProduk);
        values.put("harga", hargaProduk);
        values.put("merek", merek);
        values.put("garansi", garansi);
        values.put("stok", stok);
        values.put("deskripsi", deskripsi);

        db.update("products", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("products", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
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

    public boolean isAdmin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM users WHERE username = ? and password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean isAdmin = false;
        if (cursor.moveToFirst()) {
            String user = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            if (user.equals("admin")) {
                isAdmin = true;
            }
        }

        cursor.close();
        db.close();
        return isAdmin;
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

    public List<Object[]> getProdukByKategori(String kategori) {
        List<Object[]> produkList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, image, nama_produk, harga, merek, garansi, stok, deskripsi FROM products WHERE kategori=?", new String[]{kategori});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String image = cursor.getString(1);
                String namaProduk = cursor.getString(2);
                int harga = cursor.getInt(3);
                String merek = cursor.getString(4);
                String garansi = cursor.getString(5);
                int stok = cursor.getInt(6);
                String deskripsi = cursor.getString(7);

                // Tambahkan produk ke list
                produkList.add(new Object[]{id, image, namaProduk, harga, merek, garansi, stok, deskripsi});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return produkList;
    }

    public Object[] getProdukById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("products",
                new String[]{"image", "kategori", "nama_produk", "harga", "merek", "garansi", "stok", "deskripsi"},
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String image = cursor.getString(0);
            String kategori = cursor.getString(1);
            String namaProduk = cursor.getString(2);
            int hargaProduk = cursor.getInt(3);
            String merek = cursor.getString(4);
            String garansi = cursor.getString(5);
            int stok = cursor.getInt(6);
            String deskripsi = cursor.getString(7);

            cursor.close();
            return new Object[]{image, kategori, namaProduk, hargaProduk, merek, garansi, stok, deskripsi};
        } else {
            cursor.close();
            return null;  // Return null jika produk tidak ditemukan
        }
    }
    public String getExistingImagePath(int produkId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT image FROM products WHERE id=?", new String[]{String.valueOf(produkId)});

        if (cursor.moveToFirst()) {
            String imagePath = cursor.getString(0);
            cursor.close();
            return imagePath;
        }
        cursor.close();
        return null;
    }
}
