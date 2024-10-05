package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasi_katalog.Helper;
import com.example.aplikasi_katalog.databinding.ActivityKategoriBinding;

public class KategoriActivity extends AppCompatActivity {
    protected ActivityKategoriBinding binding;
    private boolean backPressedOnce = false;
    private Toast backToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKategoriBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedOnce) {
                    if (backToast != null) backToast.cancel();
                    finish();
                } else {
                    backPressedOnce = true;
                    backToast = Toast.makeText(KategoriActivity.this, "Press back again to exit", Toast.LENGTH_SHORT);
                    backToast.show();

                    // Reset flag after 2 seconds
                    new Handler().postDelayed(() -> backPressedOnce = false, 2000);
                }
            }
        });

        Helper.setOnClickListener(KategoriActivity.this, binding.kategori1, ListProdukActivity.class, "kategori", "laptop");
        Helper.setOnClickListener(KategoriActivity.this, binding.kategori2, ListProdukActivity.class, "kategori", "smartphone");
        Helper.setOnClickListener(KategoriActivity.this, binding.kategori3, ListProdukActivity.class, "kategori", "monitor");
        Helper.setOnClickListener(KategoriActivity.this, binding.kategori4, ListProdukActivity.class, "kategori", "router");
        Helper.setOnClickListener(KategoriActivity.this, binding.kategori5, ListProdukActivity.class, "kategori", "printer");
        Helper.setOnClickListener(KategoriActivity.this, binding.kategori6, ListProdukActivity.class, "kategori", "aksesoris");

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KategoriActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }
}
