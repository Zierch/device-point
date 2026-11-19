package com.zierch.devicepoint;

import java.util.ArrayList;
import java.util.List;

/** Konten contoh dari desain FigCraft, dipakai saat database masih kosong. */
public final class DemoData {

    /** Angka ringkasan pada frame 03 dan 06 desain FigCraft. */
    public static final int TOTAL_PRODUK = 18;
    public static final int RESTOCK_SEGERA = 3;

    private DemoData() {
    }

    public static List<Object[]> laptops() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{101, "", "ASUS Vivobook 14 A1404", 8499000,
                "Ryzen 5 7520U · 8GB · 512GB", "12 Bulan", 3,
                "Laptop modern ultra-tipis dengan prosesor AMD Ryzen 5 7520U. Layar 14 inci FHD NanoEdge, engsel 180° lay-flat, dan perlindungan ASUS Antimicrobial Guard Plus."});
        data.add(new Object[]{102, "", "Lenovo IdeaPad Slim 3", 6999000,
                "Core i3-1215U · 8GB · 512GB", "12 Bulan", 5,
                "Laptop ringan untuk belajar dan bekerja sehari-hari."});
        data.add(new Object[]{103, "", "Acer Aspire 5 Slim A514", 7499000,
                "Core i5-12450H · 16GB · 512GB", "12 Bulan", 2,
                "Laptop bertenaga dengan layar 14 inci FHD NanoEdge."});
        data.add(new Object[]{104, "", "MacBook Air M2", 15999000,
                "Apple M2 · 8GB · 256GB", "12 Bulan", 1,
                "Laptop tipis dan bertenaga untuk kreator dengan baterai seharian."});
        return data;
    }

    public static String displayName(String username) {
        return "nabil".equalsIgnoreCase(username) ? "Nabil (Zierch)"
                : username.isEmpty() ? "Nabil (Zierch)" : username;
    }

    public static String handle(String username) {
        return "nabil".equalsIgnoreCase(username) || username.isEmpty()
                ? "zierch" : username.toLowerCase(java.util.Locale.ROOT);
    }

    /** Brand dan model mengikuti label pada kartu desain FigCraft. */
    public static String brandOf(String nama, String fallback) {
        if (nama != null && nama.startsWith("MacBook ")) return "APPLE";
        String model = modelOf(nama);
        if (!model.equals(nama)) {
            return nama.substring(0, nama.length() - model.length()).trim().toUpperCase(java.util.Locale.ROOT);
        }
        return fallback == null ? "" : fallback.toUpperCase(java.util.Locale.ROOT);
    }

    public static String modelOf(String nama) {
        if (nama == null) return "";
        for (String brand : new String[]{"ASUS", "Lenovo", "Acer", "Apple"}) {
            if (nama.startsWith(brand + " ")) return nama.substring(brand.length() + 1);
        }
        return nama;
    }

    public static String ratingOf(String nama, int id) {
        if (nama != null) {
            if (nama.contains("Vivobook")) return "4.8";
            if (nama.contains("IdeaPad")) return "4.7";
            if (nama.contains("Aspire")) return "4.6";
            if (nama.contains("MacBook")) return "4.9";
        }
        return String.format(java.util.Locale.US, "%.1f", 4.4 + (id % 6) * 0.1);
    }

    /** Nama merek lengkap pada frame 04 desain FigCraft. */
    public static String merekDetail(String nama) {
        String merek = brandOf(nama, "");
        return "ASUS".equals(merek) ? "ASUS Indonesia" : merek;
    }

    public static String osOf(String nama) {
        return nama != null && nama.contains("MacBook") ? "macOS Ventura" : "Windows 11 Home";
    }

    /** Spesifikasi ringkas pada baris keranjang (frame 05 FigCraft). */
    public static String cartSpec(String nama, String merek) {
        String warna = colorOf(nama);
        String spec = merek == null ? "" : merek;
        String ram = spec.contains("16GB") || spec.contains("16 GB") ? "16GB" : "8GB";
        String storage = spec.contains("1TB") || spec.contains("1 TB") ? "1TB"
                : spec.contains("256GB") || spec.contains("256 GB") ? "256GB" : "512GB";
        String detail = ram + " / " + storage;
        return warna == null ? detail : warna + " · " + detail;
    }

    private static String colorOf(String nama) {
        if (nama == null) return null;
        if (nama.contains("Vivobook")) return "Quiet Blue";
        if (nama.contains("IdeaPad")) return "Arctic Grey";
        if (nama.contains("Aspire")) return "Silver";
        if (nama.contains("MacBook")) return "Midnight";
        return null;
    }
}
