package com.zierch.devicepoint.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zierch.devicepoint.BottomNav;
import com.zierch.devicepoint.DemoData;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SQLiteHelper;
import com.zierch.devicepoint.SystemBars;
import com.zierch.devicepoint.databinding.ActivityProfileBinding;
import com.zierch.devicepoint.ui.admin.FormProduk;
import com.zierch.devicepoint.ui.admin.HomeActivity;

/** Profil akun sesuai frame 06 desain FigCraft. */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.apply(binding.getRoot());
        BottomNav.attach(this, binding.getRoot(), 3);

        SharedPreferences user = getSharedPreferences("DataUser", Context.MODE_PRIVATE);
        String username = user.getString("username", "");
        String nama = DemoData.displayName(username);
        binding.tvNama.setText(nama);
        binding.tvHandle.setText("@" + DemoData.handle(username) + " · Local Admin");
        binding.tvInisial.setText(DemoData.handle(username).substring(0, 1).toUpperCase(java.util.Locale.ROOT));

        SQLiteHelper db = new SQLiteHelper(this);
        int total = count(db, "SELECT COUNT(*) FROM products");
        int restock = count(db, "SELECT COUNT(*) FROM products WHERE stok <= 2");
        db.close();
        binding.tvTotalProduk.setText(String.valueOf(total == 0 ? DemoData.TOTAL_PRODUK : total));
        binding.tvFavorit.setText("12");
        binding.tvRestock.setText(String.valueOf(total == 0
                ? DemoData.RESTOCK_SEGERA : restock));

        binding.rowTambah.setOnClickListener(v ->
                startActivity(new Intent(this, FormProduk.class)));
        binding.rowStok.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));
        android.view.View.OnClickListener toast = v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show();
        binding.rowTentang.setOnClickListener(toast);
        binding.btnSetting.setOnClickListener(toast);
        binding.btnLogout.setOnClickListener(v -> {
            getSharedPreferences("DataUser", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("IsAdmin", Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }

    private static int count(SQLiteHelper db, String sql) {
        Cursor cursor = db.getReadableDatabase().rawQuery(sql, null);
        int value = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return value;
    }

}
