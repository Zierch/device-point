package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasi_katalog.ListProdukAdapter;
import com.example.aplikasi_katalog.R;
import com.example.aplikasi_katalog.databinding.ActivityListProdukBinding;

import java.util.ArrayList;
import java.util.List;

public class ListProdukActivity extends AppCompatActivity {
    protected ActivityListProdukBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListProdukBinding.inflate(getLayoutInflater());
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

        List<Object[]> itemList = new ArrayList<>();
        String kategori = getIntent().getStringExtra("kategori");
        if (kategori.equals("laptop")) {
            itemList.add(new Object[]{R.drawable.latop1, "Advan Laptop Soulmate 8GB/EMMC128GB", 2599000, "Advan", "11 Bulan", "10", "Ringan dan portabel, laptop Advan Soulmate hadir dengan RAM 8GB dan penyimpanan EMMC 128GB, ideal untuk aktivitas sehari-hari dan produktivitas."});
            itemList.add(new Object[]{R.drawable.latop2, "HP 15.6 inch Laptop 15s-fq5374TU", 7149000, "HP", "30 Bulan", "7", "Laptop HP 15,6 inci ini menawarkan performa handal dengan prosesor terbaru, cocok untuk bekerja dan hiburan dengan layar yang luas dan tajam."});
        } else if (kategori.equals("smartphone")) {
            itemList.add(new Object[]{R.drawable.hp1, "Xiaomii HP murah M11 Ultra 8+256G", 5000000, "Xiaomi", "10 Bulan", "3", "HP Xiaomi M11 Ultra dengan RAM 8GB dan penyimpanan 256GB menghadirkan performa cepat dan kamera berkualitas tinggi untuk fotografi luar biasa."});
            itemList.add(new Object[]{R.drawable.hp2, "HP REDMI 13C RAM 8/256GB", 3400000, "Xiaomi", "13 Bulan", "12", "Dengan RAM 8GB dan 256GB memori, HP Redmi 13C menawarkan kombinasi sempurna antara performa dan penyimpanan, ideal untuk multitasking."});
        } else if (kategori.equals("monitor")) {
            itemList.add(new Object[]{R.drawable.moni1, "Monitor LED Business Monitor SKYWORTH", 720000, "SKYWORTH", "11 Bulan", "14", "Monitor LED SKYWORTH dirancang untuk penggunaan bisnis dengan kualitas tampilan yang tajam dan hemat energi, sempurna untuk meningkatkan produktivitas."});
            itemList.add(new Object[]{R.drawable.moni2, "KOORUI 27 inches Gaming Monitor FHD R1500 180Hz 99% sRGB", 1100000, "KOORUI", "24 Bulan", "4", "Monitor gaming KOORUI 27 inci dengan refresh rate 180Hz dan 99% sRGB memberikan pengalaman bermain yang imersif dan responsif."});
        } else if (kategori.equals("router")) {
            itemList.add(new Object[]{R.drawable.img_router, "Archer AX50", 1499000, "TP-Link", "12 Bulan", "5", "Next-Gen Gigabit Wi-Fi 6 Kecepatan—2402 Mbps pada 5 GHz dan 574 Mbps pada pita 2,4 GHz memastikan streaming yang lebih lancar dan unduhan yang lebih cepat."});
            itemList.add(new Object[]{R.drawable.img_router, "Archer AX30", 1000000, "TP-Link", "13 Bulan", "3", "Router Archer AX30 dari Tplink menghadirkan kecepatan Wi-Fi 6 yang super cepat untuk koneksi stabil dan jangkauan luas, cocok untuk rumah pintar."});
        } else if (kategori.equals("printer")) {
            itemList.add(new Object[]{R.drawable.print1, "Printer Epson WF-C5390", 2300000, "Epson", "12 Bulan", "1", "Printer Epson WF-C5390 adalah solusi cetak bisnis yang efisien dengan kecepatan tinggi dan kualitas cetak yang mengesankan, hemat biaya per halaman."});
            itemList.add(new Object[]{R.drawable.print2, "Printer Canon Pixma TS307", 1300000, "Canon", "24 Bulan", "5", "Printer Canon Pixma TS307 menghadirkan cetakan berkualitas tinggi dengan konektivitas nirkabel yang mudah, sempurna untuk penggunaan rumahan."});
        } else if (kategori.equals("aksesoris")) {
            itemList.add(new Object[]{R.drawable.akse1, "ZIFRIEND T62 Mechanical Keyboard", 230000, "ZIFRIEND", "12 Bulan", "10", "Keyboard mekanik ZIFRIEND T62 menawarkan pengalaman mengetik yang responsif dengan desain kompak dan pencahayaan RGB yang menarik."});
            itemList.add(new Object[]{R.drawable.akse2, "MOFii Mouse Wireless", 560000, "MOFii", "36 Bulan", "12", "Mouse nirkabel MOFii hadir dengan desain ergonomis dan responsif, ideal untuk penggunaan sehari-hari dengan konektivitas tanpa kabel yang praktis."});
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListProdukAdapter adapter = new ListProdukAdapter(itemList, new ListProdukAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object[] itemData) {
                Object[] objects = itemData.clone();

                Intent intent = new Intent(ListProdukActivity.this, ViewProdukActivity.class);
                intent.putExtra("detail", objects);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListProdukActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }
}
