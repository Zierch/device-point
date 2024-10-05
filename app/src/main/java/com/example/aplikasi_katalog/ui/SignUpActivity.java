package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasi_katalog.SQLiteHelper;
import com.example.aplikasi_katalog.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {
    protected ActivitySignUpBinding binding;
    private boolean backPressedOnce = false;
    private Toast backToast;
    private SQLiteHelper dbHelper;
    private String username, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
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
                    backToast = Toast.makeText(SignUpActivity.this, "Press back again to exit", Toast.LENGTH_SHORT);
                    backToast.show();

                    // Reset flag after 2 seconds
                    new Handler().postDelayed(() -> backPressedOnce = false, 2000);
                }
            }
        });

        dbHelper = new SQLiteHelper(this);

        binding.btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnSign.setEnabled(false);
                check();
            }
        });

        binding.tvSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void check() {
        boolean check = true;

//        Mendapatkan variabel
        username = binding.etUsername.getText().toString();
        password = binding.etPassword.getText().toString();
        confirmPassword = binding.etConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username Kosong !", Toast.LENGTH_SHORT).show();
            binding.etUsername.setError("Isi Username !");
            check = false;
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password Kosong !", Toast.LENGTH_SHORT).show();
            binding.etPassword.setError("Isi Password !");
            check = false;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Confirm Password dan Password tidak sesuai !", Toast.LENGTH_SHORT).show();
            check = false;
        }

        if (!check) {
            binding.btnSign.setEnabled(true);
        } else {
            signUp();
        }
    }

    private void signUp() {
        if (dbHelper.checkDuplicate(username)) {
            Toast.makeText(SignUpActivity.this, "Username sudah digunakan", Toast.LENGTH_SHORT).show();
            binding.btnSign.setEnabled(true);
            return;
        }

        // Simpan user ke database
        dbHelper.insertUser(username, password);
        Toast.makeText(SignUpActivity.this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show();

        // Setelah signup berhasil, arahkan ke halaman login
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);

    }
}