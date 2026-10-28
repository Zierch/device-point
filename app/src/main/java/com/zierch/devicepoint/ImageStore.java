package com.zierch.devicepoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Menyimpan foto produk di penyimpanan aplikasi (bukan path galeri yang bisa hilang),
 * diperkecil, dikompres, dan diputar sesuai EXIF supaya tidak miring.
 */
public final class ImageStore {

    private static final int MAX_DIMENSION = 1024;
    private static final int COMPRESS_QUALITY = 85;
    private static final String DIRECTORY = "ProductImages";

    private ImageStore() {
    }

    /** Menyalin gambar dari galeri ke penyimpanan aplikasi, lalu mengembalikan path-nya. */
    public static String save(Context context, Uri source) throws IOException {
        Bitmap bitmap = decodeScaled(context, source);
        bitmap = applyExifRotation(context, source, bitmap);

        boolean png = "image/png".equals(context.getContentResolver().getType(source));
        Bitmap.CompressFormat format = png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
        String extension = png ? ".png" : ".jpg";

        File directory = new File(context.getExternalFilesDir(null), DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            bitmap.recycle();
            throw new IOException("Tidak bisa membuat folder gambar: " + directory);
        }

        File file = new File(directory, "produk_" + UUID.randomUUID() + extension);
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(format, COMPRESS_QUALITY, out)) {
                throw new IOException("Gagal mengompres gambar");
            }
        } finally {
            bitmap.recycle();
        }
        return file.getAbsolutePath();
    }

    /**
     * Menghapus file gambar milik aplikasi. Path di luar folder ProductImages ditolak,
     * supaya nilai database yang aneh tidak bisa menghapus file lain.
     */
    public static void delete(Context context, String path) {
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        File file = new File(path);
        try {
            File directory = new File(context.getExternalFilesDir(null), DIRECTORY);
            String allowedPrefix = directory.getCanonicalPath() + File.separator;
            if (!file.getCanonicalPath().startsWith(allowedPrefix)) {
                return;
            }
        } catch (IOException e) {
            return;
        }

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

    /** Pangkat dua terkecil supaya sisi terpanjang hasil decode tidak melebihi MAX_DIMENSION. */
    private static int sampleSize(int width, int height) {
        int sample = 1;
        int longest = Math.max(width, height);
        while (longest / sample > MAX_DIMENSION) {
            sample *= 2;
        }
        return sample;
    }

    private static Bitmap applyExifRotation(Context context, Uri source, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in == null) {
                return bitmap;
            }
            ExifInterface exif = new ExifInterface(in);
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.postRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.postRotate(270);
                    matrix.postScale(-1, 1);
                    break;
                default:
                    return bitmap;
            }
        } catch (IOException | RuntimeException e) {
            return bitmap;
        }

        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotated != bitmap) {
            bitmap.recycle();
        }
        return rotated;
    }
}
