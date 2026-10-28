package com.zierch.devicepoint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zierch.devicepoint.ListProdukAdapter;
import com.zierch.devicepoint.SQLiteHelper;
import com.zierch.devicepoint.databinding.ActivityListProdukBinding;

import java.util.ArrayList;
import java.util.List;

public class ListProdukActivity extends AppCompatActivity {
    protected ActivityListProdukBinding binding;
    private SQLiteHelper dbHelper;
    private ListProdukAdapter adapter;
    private String kategori;
    private String keyword = "";
    private String sort = SQLiteHelper.SORT_NEWEST;


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
            refresh(); // Memperbarui daftar produk
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

        kategori = getIntent().getStringExtra("kategori");
        dbHelper = new SQLiteHelper(this);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListProdukAdapter(new ArrayList<>(), new ListProdukAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object[] itemData) {
                Object[] objects = itemData.clone();

                Intent intent = new Intent(ListProdukActivity.this, ViewProdukActivity.class);
                intent.putExtra("detail", objects);
                startActivity(intent);
            }
        }, this);
        recyclerView.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s.toString();
                refresh();
            }
        });

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sort = sortFor(position);
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListProdukActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });
    }

    /** Posisi spinner mengikuti urutan di {@code R.array.urutan_produk}. */
    private String sortFor(int position) {
        switch (position) {
            case 1:
                return SQLiteHelper.SORT_NAME;
            case 2:
                return SQLiteHelper.SORT_PRICE_LOW;
            case 3:
                return SQLiteHelper.SORT_PRICE_HIGH;
            default:
                return SQLiteHelper.SORT_NEWEST;
        }
    }

    private void refresh() {
        List<Object[]> items = dbHelper.getProdukByKategori(kategori, keyword, sort);
        adapter.updateData(items);
        binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
