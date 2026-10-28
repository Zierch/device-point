package com.zierch.devicepoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Menyimpan foto produk di penyimpanan aplikasi (bukan path galeri yang bisa hilang),
 * dengan downscale supaya tidak menelan puluhan MB per gambar.
 */
public final class ImageStore {

    private static final int MAX_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 85;
    private static final String DIRECTORY = "ProductImages";

    private ImageStore() {
    }

    /** Menyalin gambar dari galeri ke penyimpanan aplikasi, lalu mengembalikan path-nya. */
    public static String save(Context context, Uri source) throws IOException {
        Bitmap bitmap = decodeScaled(context, source);
        File directory = new File(context.getExternalFilesDir(null), DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Tidak bisa membuat folder gambar: " + directory);
        }

        File file = new File(directory, "produk_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                throw new IOException("Gagal mengompres gambar");
            }
        } finally {
            bitmap.recycle();
        }
        return file.getAbsolutePath();
    }

    /** Menghapus file gambar lama; aman dipanggil saat file sudah tidak ada. */
    public static void delete(String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    private static Bitmap decodeScaled(Context context, Uri source) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight);

        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null) {
                throw new IOException("Gambar tidak bisa dibaca");
            }
            return bitmap;
        }
    }

    /** Pangkat dua terbesar yang masih menyisakan sisi terpanjang >= MAX_DIMENSION. */
    private static int sampleSize(int width, int height) {
        int sample = 1;
        int longest = Math.max(width, height);
        while (longest / (sample * 2) >= MAX_DIMENSION) {
            sample *= 2;
        }
        return sample;
    }
}
