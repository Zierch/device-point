package com.zierch.devicepoint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.zierch.devicepoint.CartStore;
import com.zierch.devicepoint.DemoData;
import com.zierch.devicepoint.Helper;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SystemBars;
import com.zierch.devicepoint.databinding.ActivityViewProdukBinding;

import java.io.File;

/** Detail perangkat sesuai frame 04 desain FigCraft. */
public class ViewProdukActivity extends AppCompatActivity {

    private ActivityViewProdukBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProdukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.apply(binding.getRoot());

        Object[] objects = (Object[]) getIntent().getSerializableExtra("detail");
        if (objects == null || objects.length < 8) {
            finish();
            return;
        }

        int id = (int) objects[0];
        String imagePath = (String) objects[1];
        String namaProduk = (String) objects[2];
        int hargaProduk = (int) objects[3];
        String merek = objects[4] == null ? "" : (String) objects[4];
        int stok = (int) objects[6];
        String deskripsi = objects[7] == null ? "" : (String) objects[7];

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this).load(new File(imagePath))
                    .placeholder(R.drawable.img_product_placeholder)
                    .error(R.drawable.img_product_placeholder)
                    .into(binding.image);
        }
        binding.image.setContentDescription(namaProduk);
        binding.namaProduk.setText(namaProduk);
        binding.hargaProduk.setText("Rp " + Helper.currencyID(hargaProduk));
        binding.isi1.setText(DemoData.merekDetail(namaProduk));
        binding.isiGaransi.setText(DemoData.osOf(namaProduk));
        binding.isiStok.setText("● Stok: " + stok + " Unit");
        binding.deskripsi.setText(deskripsi);

        String spec = merek + " " + deskripsi;
        binding.specProsesor.setText(merek.contains("Ryzen") ? "Ryzen 5"
                : merek.contains("Core i5") ? "Core i5"
                : merek.contains("Core i3") ? "Core i3" : "Prosesor");
        binding.specRam.setText(spec.contains("16GB") || spec.contains("16 GB") ? "16 GB" : "8 GB");
        binding.specStorage.setText(spec.contains("1TB") || spec.contains("1 TB") ? "1 TB" : "512 GB");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ContactActivity.class)));
        binding.btnShare.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.btnFav.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
        binding.btnAddCart.setOnClickListener(v -> {
            CartStore.add(this, id, namaProduk, hargaProduk,
                    DemoData.cartSpec(namaProduk, merek));
            Toast.makeText(this, "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
