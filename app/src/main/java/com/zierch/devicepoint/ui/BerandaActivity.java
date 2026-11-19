package com.zierch.devicepoint.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zierch.devicepoint.BottomNav;
import com.zierch.devicepoint.DemoData;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SQLiteHelper;
import com.zierch.devicepoint.SystemBars;
import com.zierch.devicepoint.databinding.ActivityBerandaBinding;

import java.util.List;

/** Beranda sesuai frame 02 desain FigCraft. */
public class BerandaActivity extends AppCompatActivity {

    private ActivityBerandaBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBerandaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.apply(binding.getRoot());
        BottomNav.attach(this, binding.getRoot(), 0);

        SharedPreferences user = getSharedPreferences("DataUser", Context.MODE_PRIVATE);
        String username = user.getString("username", "");
        binding.tvNama.setText(DemoData.displayName(username));

        List<Object[]> products = new SQLiteHelper(this).getProdukByKategori("Laptop");
        if (products.isEmpty()) products = DemoData.laptops();

        BerandaAdapter adapter = new BerandaAdapter(products, item -> {
            Intent intent = new Intent(this, ViewProdukActivity.class);
            intent.putExtra("detail", item.clone());
            startActivity(intent);
        });
        binding.recyclerBeranda.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBeranda.setAdapter(adapter);

        View.OnClickListener bukaKatalog = v -> bukaKatalog("Laptop");
        binding.btnSearch.setOnClickListener(bukaKatalog);
        binding.btnLihatSemua.setOnClickListener(bukaKatalog);
        binding.chipLaptop.setOnClickListener(bukaKatalog);
        binding.btnCekPromo.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.btnNotif.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.chipPc.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.chipAksesoris.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.chipSemua.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
    }

    private void bukaKatalog(String kategori) {
        Intent intent = new Intent(this, ListProdukActivity.class);
        intent.putExtra("kategori", kategori);
        startActivity(intent);
    }

}
