package com.example.aplikasi_katalog.ui.Admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.aplikasi_katalog.SQLiteHelper;
import com.example.aplikasi_katalog.databinding.FormTambahProdukBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FormProduk extends AppCompatActivity {
    protected FormTambahProdukBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private SQLiteHelper dbHelper;
    private Uri imageUri;
    private String kategori, imagePath, namaProduk, merek, deskripsi, garansi;
    private int hargaProduk, stok;
    private boolean isEditMode = false;
    private int produkId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FormTambahProdukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();

        // Cek apakah ini mode edit
        if (getIntent().hasExtra("produk_id")) {
            isEditMode = true;
            produkId = getIntent().getIntExtra("produk_id", -1);
            loadProdukData(produkId);  // Load data produk untuk diedit
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.cam.setVisibility(View.GONE);
            binding.image.setImageURI(imageUri);  // Preview image in ImageView
        }
    }

    private void initView(){
        dbHelper = new SQLiteHelper(this);
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnSave.setEnabled(false);
                check();
            }
        });

    }

    private void check() {
        boolean check = true;

        // Mendapatkan variabel
        kategori = binding.kategori.getSelectedItem().toString();
        namaProduk = binding.namaProduk.getText().toString();
        merek = binding.merekProduk.getText().toString();
        deskripsi = binding.deskripsi.getText().toString();
        garansi = binding.garansi.getText().toString() + " Bulan";
        hargaProduk = Integer.parseInt(binding.hargaProduk.getText().toString());
        stok = Integer.parseInt(binding.stok.getText().toString());

        // Jika imageUri tidak null, maka simpan gambar
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(FormProduk.this.getContentResolver(), imageUri);
                imagePath = saveImageToStorage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Jika dalam mode edit dan tidak ada gambar baru, gunakan gambar yang ada
            if (isEditMode) {
                imagePath = dbHelper.getExistingImagePath(produkId); // Ambil gambar yang ada dari database
            } else {
                imagePath = ""; // Tidak ada gambar baru dan bukan mode edit
            }
        }

        if (namaProduk.isEmpty()) {
            Toast.makeText(this, "Nama Produk Kosong !", Toast.LENGTH_SHORT).show();
            binding.namaProduk.setError("Isi Nama Produk !");
            check = false;
        } else if (merek.isEmpty()) {
            Toast.makeText(this, "Merek Kosong !", Toast.LENGTH_SHORT).show();
            binding.merekProduk.setError("Isi Merek !");
            check = false;
        } else if (deskripsi.isEmpty()) {
            Toast.makeText(this, "Deskripsi Kosong !", Toast.LENGTH_SHORT).show();
            binding.deskripsi.setError("Isi Deskripsi !");
            check = false;
        } else if (garansi.isEmpty()) {
            Toast.makeText(this, "Garansi Kosong !", Toast.LENGTH_SHORT).show();
            binding.garansi.setError("Isi Garansi !");
            check = false;
        } else if (hargaProduk == 0) {
            Toast.makeText(this, "Harga Kosong !", Toast.LENGTH_SHORT).show();
            binding.hargaProduk.setError("Isi Harga !");
            check = false;
        } else if (stok == 0) {
            Toast.makeText(this, "Stok Kosong !", Toast.LENGTH_SHORT).show();
            binding.stok.setError("Isi Stok !");
            check = false;
        }

        if (!check) {
            binding.btnSave.setEnabled(true);
        } else {
            if (isEditMode) {
                // Update produk
                dbHelper.updateProduct(produkId, imagePath, kategori, namaProduk, hargaProduk, merek, garansi, stok, deskripsi);
                Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
            } else {
                // Insert produk baru
                dbHelper.insertProduct(imagePath, kategori, namaProduk, hargaProduk, merek, garansi, stok, deskripsi);
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private String saveImageToStorage(Bitmap bitmap) throws IOException {
        File directory = new File(getExternalFilesDir(null), "ProductImages");
        if (!directory.exists()) {
            directory.mkdirs();  // Buat direktori jika belum ada
        }

        String fileName = System.currentTimeMillis() + ".png";
        File imageFile = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }

        return imageFile.getAbsolutePath();  // Kembalikan path gambar
    }

    // Load data produk jika dalam mode edit
    private void loadProdukData(int produkId) {
        if (produkId != -1) {
            // Panggil fungsi untuk mengambil produk berdasarkan ID dari database
            Object[] produkData = dbHelper.getProdukById(produkId);
            if (produkData != null) {
                // Isi form dengan data produk
                String imagePath = (String) produkData[0];
                String kategori = (String) produkData[1];
                String namaProduk = (String) produkData[2];
                int hargaProduk = (int) produkData[3];
                String merek = (String) produkData[4];
                String garansi = (String) produkData[5];
                int stok = (int) produkData[6];
                String deskripsi = (String) produkData[7];

                // Set data pada form
                binding.kategori.setSelection(getIndexForKategori(kategori));
                binding.namaProduk.setText(namaProduk);
                binding.hargaProduk.setText(String.valueOf(hargaProduk));
                binding.merekProduk.setText(merek);
                binding.deskripsi.setText(deskripsi);
                binding.garansi.setText(garansi);
                binding.stok.setText(String.valueOf(stok));
                binding.cam.setVisibility(View.GONE);

                // Untuk gambar
                Glide.with(this).load(new File(imagePath)).into(binding.image);
            }
        } else {
            Toast.makeText(this, "Produk tidak ditemukan!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getIndexForKategori(String kategori) {
        // Cari posisi kategori di Spinner (Optional)
        for (int i = 0; i < binding.kategori.getCount(); i++) {
            if (binding.kategori.getItemAtPosition(i).toString().equalsIgnoreCase(kategori)) {
                return i;
            }
        }
        return 0;
    }

}
