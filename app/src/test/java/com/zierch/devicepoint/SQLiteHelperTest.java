package com.zierch.devicepoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SQLiteHelperTest {

    private static final String DB_NAME = "katalog.db";

    private Context context;
    private SQLiteHelper helper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DB_NAME);
        helper = new SQLiteHelper(context);
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void seededAdminCanSignIn() {
        assertTrue(helper.checkUser("admin", "admin123"));
        assertFalse(helper.checkUser("admin", "salah"));
        assertFalse(helper.checkUser("tidak-ada", "admin123"));
        assertTrue(helper.isAdmin("admin"));
    }

    @Test
    public void newUserCanSignInButIsNotAdmin() {
        helper.insertUser("budi", "rahasia123");

        assertTrue(helper.checkUser("budi", "rahasia123"));
        assertFalse(helper.checkUser("budi", "rahasia124"));
        assertFalse(helper.isAdmin("budi"));
    }

    @Test
    public void duplicateUsernameIsIgnored() {
        helper.insertUser("budi", "rahasia123");
        helper.insertUser("budi", "password-lain");

        assertTrue(helper.checkUser("budi", "rahasia123"));
        assertFalse(helper.checkUser("budi", "password-lain"));
        assertEquals(1, countUsers("budi"));
    }

    @Test
    public void storedPasswordIsAHashedValue() {
        helper.insertUser("budi", "rahasia123");

        String stored = readPasswordHash("budi");
        assertNotNull(stored);
        assertFalse(stored.contains("rahasia123"));
        assertTrue(stored.startsWith("PBKDF2WithHmac"));
    }

    @Test
    public void version4DatabaseIsMigratedAndStillLogsIn() {
        // Siapkan database berskema lama (v4: kolom password berupa teks biasa).
        helper.close();
        context.deleteDatabase(DB_NAME);
        SQLiteDatabase legacy = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DB_NAME), null);
        legacy.execSQL("CREATE TABLE users (id INTEGER Primary Key AUTOINCREMENT, username Text, password Text)");
        legacy.execSQL("CREATE TABLE products (id Integer Primary Key AUTOINCREMENT, image TEXT, kategori TEXT, "
                + "nama_produk TEXT, harga INTEGER, merek TEXT, garansi TEXT, stok INTEGER, deskripsi TEXT)");
        legacy.execSQL("INSERT INTO users (username, password) VALUES ('admin', 'admin123')");
        legacy.execSQL("INSERT INTO users (username, password) VALUES ('lama', 'passlama123')");
        legacy.execSQL("INSERT INTO products (image, kategori, nama_produk, harga, merek, garansi, stok, deskripsi) "
                + "VALUES ('', 'Laptop', 'Laptop Lama', 5000000, 'Asus', '12 Bulan', 2, 'Produk sebelum migrasi')");
        legacy.setVersion(4);
        legacy.close();

        // Membuka helper dengan versi 5 memicu onUpgrade + migrasi hash.
        helper = new SQLiteHelper(context);

        assertTrue(helper.checkUser("admin", "admin123"));
        assertTrue(helper.checkUser("lama", "passlama123"));
        assertFalse(helper.checkUser("lama", "salah"));
        assertTrue(helper.isAdmin("admin"));
        assertFalse(helper.isAdmin("lama"));
        assertFalse(readPasswordHash("lama").contains("passlama123"));
        assertEquals(1, helper.getProdukByKategori("Laptop").size());
    }

    @Test
    public void searchMatchesNameAndBrand() {
        insertLaptop("Laptop Gaming", "Asus", 15000000);
        insertLaptop("Laptop Kantor", "Lenovo", 7000000);
        insertLaptop("Laptop Desain", "Asus", 12000000);

        assertEquals(3, helper.getProdukByKategori("Laptop", "", SQLiteHelper.SORT_NEWEST).size());
        assertEquals(2, helper.getProdukByKategori("Laptop", "asus", SQLiteHelper.SORT_NEWEST).size());
        assertEquals(1, helper.getProdukByKategori("Laptop", "kantor", SQLiteHelper.SORT_NEWEST).size());
        assertEquals(0, helper.getProdukByKategori("Laptop", "monitor", SQLiteHelper.SORT_NEWEST).size());
    }

    @Test
    public void sortOrdersAreApplied() {
        insertLaptop("Zebra", "Asus", 500);
        insertLaptop("Alpha", "Asus", 900);
        insertLaptop("Mango", "Asus", 100);

        List<Object[]> byName = helper.getProdukByKategori("Laptop", "", SQLiteHelper.SORT_NAME);
        assertEquals("Alpha", byName.get(0)[2]);
        assertEquals("Mango", byName.get(1)[2]);
        assertEquals("Zebra", byName.get(2)[2]);

        List<Object[]> cheapest = helper.getProdukByKategori("Laptop", "", SQLiteHelper.SORT_PRICE_LOW);
        assertEquals(100, cheapest.get(0)[3]);
        assertEquals(900, cheapest.get(2)[3]);

        List<Object[]> priciest = helper.getProdukByKategori("Laptop", "", SQLiteHelper.SORT_PRICE_HIGH);
        assertEquals(900, priciest.get(0)[3]);

        List<Object[]> newest = helper.getProdukByKategori("Laptop", "", SQLiteHelper.SORT_NEWEST);
        assertEquals("Mango", newest.get(0)[2]);
    }

    @Test
    public void updateAndDeleteProduct() {
        insertLaptop("Laptop Gaming", "Asus", 15000000);
        Object[] stored = helper.getProdukByKategori("Laptop").get(0);
        int id = (int) stored[0];

        helper.updateProduct(id, "path/baru.jpg", "Laptop", "Laptop Gaming Pro", 17000000,
                "Asus", "24 Bulan", 3, "Spesifikasi naik");

        Object[] updated = helper.getProdukById(id);
        assertNotNull(updated);
        assertEquals("Laptop Gaming Pro", updated[2]);
        assertEquals(17000000, updated[3]);
        assertEquals("24 Bulan", updated[5]);
        assertEquals("path/baru.jpg", updated[0]);

        assertTrue(helper.deleteProduct(id));
        assertNull(helper.getProdukById(id));
        assertEquals(0, helper.getProdukByKategori("Laptop").size());
    }

    @Test
    public void productInOtherCategoryIsNotListed() {
        insertLaptop("Laptop Gaming", "Asus", 15000000);
        helper.insertProduct("", "Monitor", "Monitor 24 inci", 2000000, "LG", "12 Bulan", 4, "Monitor kantor");

        assertEquals(1, helper.getProdukByKategori("Laptop").size());
        assertEquals(1, helper.getProdukByKategori("Monitor").size());
        assertEquals(0, helper.getProdukByKategori("Printer").size());
    }

    private void insertLaptop(String name, String brand, int price) {
        helper.insertProduct("", "Laptop", name, price, brand, "12 Bulan", 1, "Deskripsi " + name);
    }

    private int countUsers(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE username = ?", new String[]{username})) {
            assertTrue(cursor.moveToFirst());
            return cursor.getInt(0);
        }
    }

    private String readPasswordHash(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT password_hash FROM users WHERE username = ?", new String[]{username})) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }
}
