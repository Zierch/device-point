package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasi_katalog.SQLiteHelper;
import com.example.aplikasi_katalog.databinding.ActivitySignInBinding;


public class SignInActivity extends AppCompatActivity {
    protected ActivitySignInBinding binding;
    private boolean backPressedOnce = false;
    private Toast backToast;
    private SQLiteHelper dbHelper;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
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
                    backToast = Toast.makeText(SignInActivity.this, "Press back again to exit", Toast.LENGTH_SHORT);
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
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void check() {
        boolean check = true;

//        Mendapatkan variabel
        username = binding.etUsername.getText().toString();
        password = binding.etPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username Kosong !", Toast.LENGTH_SHORT).show();
            binding.etUsername.setError("Isi Username !");
            check = false;
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password Kosong !", Toast.LENGTH_SHORT).show();
            binding.etPassword.setError("Isi Password !");
            check = false;
        }

        if (!check) {
            binding.btnSign.setEnabled(true);
        } else {
            signIn();
        }
    }

    private void signIn() {
        boolean isValid = dbHelper.checkUser(username, password);

        if (isValid) {
            // Jika valid, arahkan ke halaman berikutnya
            Toast.makeText(SignInActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInActivity.this, KategoriActivity.class); // Ganti dengan activity tujuan setelah login
            startActivity(intent);
            intent.setFlags()
            finish();
        } else {
            // Jika tidak valid, tampilkan pesan error
            Toast.makeText(SignInActivity.this, "Username atau Password salah", Toast.LENGTH_SHORT).show();
            binding.btnSign.setEnabled(true);
        }
    }
}