package com.example.aplikasi_katalog.ui.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasi_katalog.Helper;
import com.example.aplikasi_katalog.databinding.ActivityHomeBinding;
import com.example.aplikasi_katalog.ui.ContactActivity;
import com.example.aplikasi_katalog.ui.KategoriActivity;

public class HomeActivity extends AppCompatActivity {
    protected ActivityHomeBinding binding;
    private boolean backPressedOnce = false;
    private Toast backToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
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
                    backToast = Toast.makeText(HomeActivity.this, "Press back again to exit", Toast.LENGTH_SHORT);
                    backToast.show();

                    // Reset flag after 2 seconds
                    new Handler().postDelayed(() -> backPressedOnce = false, 2000);
                }
            }
        });

        Helper.setOnClickListener(HomeActivity.this, binding.pageTambah, FormProduk.class,"","");
        Helper.setOnClickListener(HomeActivity.this, binding.pageKategori, KategoriActivity.class,"","");


        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }
}
