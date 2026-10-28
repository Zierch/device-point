package com.zierch.devicepoint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String databaseName = "katalog.db";
    private static final int databaseVersion = 5;

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE users (id INTEGER Primary Key AUTOINCREMENT, username Text UNIQUE NOT NULL, password_hash Text NOT NULL, is_admin INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_TABLE_PRODUCTS =
            "CREATE TABLE products (id Integer Primary Key AUTOINCREMENT, image TEXT, kategori TEXT, nama_produk TEXT, harga INTEGER, merek TEXT, garansi TEXT, stok INTEGER, deskripsi TEXT)";

    /** Demo account so the app is usable straight after install; documented in the README. */
    private static final String SEED_ADMIN_USER = "admin";
    private static final String SEED_ADMIN_PASSWORD = "admin123";

    public static final String SORT_NEWEST = "terbaru";
    public static final String SORT_NAME = "nama_asc";
    public static final String SORT_PRICE_LOW = "harga_asc";
    public static final String SORT_PRICE_HIGH = "harga_desc";

    public SQLiteHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
    }

    // Method untuk membuat tabel
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);

//        set admin
        insertUserWithHash(db, SEED_ADMIN_USER, SEED_ADMIN_PASSWORD, true);
    }

    // Method untuk mengubah database (jika versi di-upgrade)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL(CREATE_TABLE_PRODUCTS.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS"));
        }

        if (oldVersion < 5) {
            migrateUsersToHashedPasswords(db);
        }

        // Jika upgrade, periksa apakah admin sudah ada, jika belum tambahkan
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{SEED_ADMIN_USER});
        boolean adminMissing = cursor.getCount() == 0;
        cursor.close();
        if (adminMissing) {
            insertUserWithHash(db, SEED_ADMIN_USER, SEED_ADMIN_PASSWORD, true);
        }
    }

    /**
     * Rebuilds users so the plain-text password column becomes password_hash + is_admin,
     * hashing whatever the old rows held on the way. The table is recreated because the
     * SQLite build shipped with API 24 has no DROP COLUMN.
     */
    private void migrateUsersToHashedPasswords(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE users RENAME TO users_legacy");
        db.execSQL(CREATE_TABLE_USERS);

        Cursor cursor = db.rawQuery("SELECT username, password FROM users_legacy", null);
        while (cursor.moveToNext()) {
            String username = cursor.getString(0);
            String plainPassword = cursor.getString(1);
            if (username != null && plainPassword != null) {
                insertUserWithHash(db, username, plainPassword, SEED_ADMIN_USER.equals(username));
            }
        }
        cursor.close();

        db.execSQL("DROP TABLE users_legacy");
    }

    public void insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        insertUserWithHash(db, username, password, false);
        db.close();
    }

    private void insertUserWithHash(SQLiteDatabase db, String username, String password, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password_hash", PasswordHasher.hash(password));
        values.put("is_admin", isAdmin ? 1 : 0);

        db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE);
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
    public boolean updateProduct(int id, String imagePath, String kategori, String namaProduk, int hargaProduk, String merek, String garansi, int stok, String deskripsi) {
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

        int rows = db.update("products", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("products", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String storedHash = null;
        Cursor cursor = db.rawQuery("SELECT password_hash FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            storedHash = cursor.getString(0);
        }
        cursor.close();
        db.close();

        // Hanya hash yang dibandingkan; password mentah tidak pernah masuk ke SQL.
        return PasswordHasher.verify(password, storedHash);
    }

    public boolean isAdmin(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT is_admin FROM users WHERE username = ?", new String[]{username});

        boolean isAdmin = cursor.moveToFirst() && cursor.getInt(0) == 1;

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
        return getProdukByKategori(kategori, "", SORT_NEWEST);
    }

    /**
     * Produk dalam satu kategori, dengan pencarian nama/merek dan urutan pilihan.
     * Kata kunci selalu lewat placeholder, dan urutan hanya boleh dari konstanta SORT_*.
     */
    public List<Object[]> getProdukByKategori(String kategori, String keyword, String sort) {
        List<Object[]> produkList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder(
                "SELECT id, image, nama_produk, harga, merek, garansi, stok, deskripsi FROM products WHERE kategori = ?");
        List<String> args = new ArrayList<>();
        args.add(kategori);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (nama_produk LIKE ? OR merek LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY ").append(orderBy(sort));

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

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

    /** Urutan di-whitelist supaya pilihan sort tidak bisa jadi SQL injection. */
    private String orderBy(String sort) {
        if (SORT_NAME.equals(sort)) {
            return "nama_produk COLLATE NOCASE ASC";
        }
        if (SORT_PRICE_LOW.equals(sort)) {
            return "harga ASC";
        }
        if (SORT_PRICE_HIGH.equals(sort)) {
            return "harga DESC";
        }
        return "id DESC";
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
