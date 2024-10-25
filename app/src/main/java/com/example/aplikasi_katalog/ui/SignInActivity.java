package com.example.aplikasi_katalog.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplikasi_katalog.SQLiteHelper;
import com.example.aplikasi_katalog.databinding.ActivitySignInBinding;
import com.example.aplikasi_katalog.ui.Admin.HomeActivity;


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
        
        binding.tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this, "404, Belum dibuat !", Toast.LENGTH_SHORT).show();
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

//            save ke sharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("DataUser", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();
        }
    }

    private void signIn() {
        boolean isValid = dbHelper.checkUser(username, password);
        boolean isAdmin = dbHelper.isAdmin(username, password);
//        save untuk admin
        SharedPreferences sharedPreferences = getSharedPreferences("IsAdmin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAdmin", isAdmin);
        editor.apply();
        Intent intent = null;

        if (isValid) {
            // Jika valid, arahkan ke halaman berikutnya
            Toast.makeText(SignInActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
            if (isAdmin){
                intent = new Intent(SignInActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SignInActivity.this, KategoriActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Jika tidak valid, tampilkan pesan error
            Toast.makeText(SignInActivity.this, "Username atau Password salah", Toast.LENGTH_SHORT).show();
            binding.btnSign.setEnabled(true);
        }
    }
}