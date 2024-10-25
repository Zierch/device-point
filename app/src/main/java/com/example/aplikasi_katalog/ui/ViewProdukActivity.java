package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.aplikasi_katalog.Helper;
import com.example.aplikasi_katalog.databinding.ActivityViewProdukBinding;

import java.io.File;

public class ViewProdukActivity extends AppCompatActivity {
    protected ActivityViewProdukBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProdukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        Object[] objects = (Object[]) getIntent().getSerializableExtra("detail");
        String imagePath = (String) objects[1];
        String namaProduk = (String) objects[2];
        int hargaProduk = (int) objects[3];
        String merek = (String) objects[4];
        String garansi = (String) objects[5];
        int stok = (int) objects[6];
        String deskripsi = (String) objects[7];

        Glide.with(this).load(new File(imagePath)).into(binding.image);
        binding.hargaProduk.setText(Helper.currencyID(hargaProduk));
        binding.namaProduk.setText(namaProduk);
        binding.isi1.setText(merek);
        binding.isiGaransi.setText(garansi);
        binding.isiStok.setText(String.valueOf(stok));
        binding.deskripsi.setText(deskripsi);

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProdukActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }
}
