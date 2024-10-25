package com.example.aplikasi_katalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasi_katalog.ListProdukAdapter;
import com.example.aplikasi_katalog.R;
import com.example.aplikasi_katalog.SQLiteHelper;
import com.example.aplikasi_katalog.databinding.ActivityListProdukBinding;

import java.util.ArrayList;
import java.util.List;

public class ListProdukActivity extends AppCompatActivity {
    protected ActivityListProdukBinding binding;
    private SQLiteHelper dbHelper;
    private ListProdukAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListProdukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            updateProductList(); // Memperbarui daftar produk
        }
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

        String kategori = getIntent().getStringExtra("kategori");
        dbHelper = new SQLiteHelper(this);
        List<Object[]> itemList = dbHelper.getProdukByKategori(kategori);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListProdukAdapter(itemList, new ListProdukAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object[] itemData) {
                Object[] objects = itemData.clone();

                Intent intent = new Intent(ListProdukActivity.this, ViewProdukActivity.class);
                intent.putExtra("detail", objects);
                startActivity(intent);
            }
        }, this);
        recyclerView.setAdapter(adapter);

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListProdukActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }

    private void updateProductList() {
        String kategori = getIntent().getStringExtra("kategori");
        List<Object[]> updatedItemList = dbHelper.getProdukByKategori(kategori);
        adapter.updateData(updatedItemList); // Pastikan adapter memiliki metode ini
    }
}
