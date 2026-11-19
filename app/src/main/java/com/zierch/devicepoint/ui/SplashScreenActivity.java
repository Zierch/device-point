package com.zierch.devicepoint.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zierch.devicepoint.SystemBars;
import com.zierch.devicepoint.databinding.ActivitySplashScreenBinding;

/** Layar onboarding sesuai frame 01 desain FigCraft. */
@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashScreenBinding binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.apply(binding.getRoot());

        android.view.View.OnClickListener lanjut = v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        };
        binding.btnMulai.setOnClickListener(lanjut);
        binding.btnLewati.setOnClickListener(lanjut);
    }
}
