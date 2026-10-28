package com.zierch.devicepoint.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.zierch.devicepoint.ImageStore;
import com.zierch.devicepoint.SQLiteHelper;
import com.zierch.devicepoint.databinding.FormTambahProdukBinding;

import java.io.File;
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
        if (!isCurrentUserAdmin()) {
            finish();
            return;
        }
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

    /** Pintu admin tetap terkunci kecuali akun yang login memang admin di database. */
    private boolean isCurrentUserAdmin() {
        String username = getSharedPreferences("DataUser", MODE_PRIVATE).getString("username", "");
        return !username.isEmpty() && new SQLiteHelper(this).isAdmin(username);
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

        // Mendapatkan variabel (angka baru di-parse setelah lolos validasi)
        kategori = binding.kategori.getSelectedItem().toString();
        namaProduk = binding.namaProduk.getText().toString().trim();
        merek = binding.merekProduk.getText().toString().trim();
        deskripsi = binding.deskripsi.getText().toString().trim();
        String garansiBulan = binding.garansi.getText().toString().trim();
        String hargaInput = binding.hargaProduk.getText().toString().trim();
        String stokInput = binding.stok.getText().toString().trim();

        hargaProduk = parseNumber(hargaInput);
        stok = parseNumber(stokInput);

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
        } else if (garansiBulan.isEmpty()) {
            Toast.makeText(this, "Garansi Kosong !", Toast.LENGTH_SHORT).show();
            binding.garansi.setError("Isi Garansi !");
            check = false;
        } else if (hargaProduk <= 0) {
            Toast.makeText(this, "Harga harus angka lebih dari 0 !", Toast.LENGTH_SHORT).show();
            binding.hargaProduk.setError("Isi Harga !");
            check = false;
        } else if (stok < 0) {
            Toast.makeText(this, "Stok harus angka 0 atau lebih !", Toast.LENGTH_SHORT).show();
            binding.stok.setError("Isi Stok !");
            check = false;
        }

        if (!check) {
            binding.btnSave.setEnabled(true);
            return;
        }

        garansi = garansiBulan + " Bulan";

        try {
            imagePath = resolveImagePath();
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
            binding.btnSave.setEnabled(true);
            return;
        }

        if (isEditMode) {
            String previousImage = dbHelper.getExistingImagePath(produkId);
            dbHelper.updateProduct(produkId, imagePath, kategori, namaProduk, hargaProduk, merek, garansi, stok, deskripsi);
            if (previousImage != null && !previousImage.equals(imagePath)) {
                ImageStore.delete(previousImage);
            }
            Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            dbHelper.insertProduct(imagePath, kategori, namaProduk, hargaProduk, merek, garansi, stok, deskripsi);
            Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private int parseNumber(String value) {
        if (value.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Gambar baru disalin ke penyimpanan aplikasi; saat edit tanpa gambar baru, path lama dipakai. */
    private String resolveImagePath() throws IOException {
        if (imageUri != null) {
            return ImageStore.save(this, imageUri);
        }
        if (isEditMode) {
            return dbHelper.getExistingImagePath(produkId);
        }
        return "";
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
                binding.garansi.setText(garansi.replaceAll("(?i)\\s*bulan$", ""));
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
