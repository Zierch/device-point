package com.zierch.devicepoint.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zierch.devicepoint.BottomNav;
import com.zierch.devicepoint.DemoData;
import com.zierch.devicepoint.ListProdukAdapter;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SQLiteHelper;
import com.zierch.devicepoint.SystemBars;
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
        SystemBars.apply(binding.getRoot());
        BottomNav.attach(this, binding.getRoot(), 1);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            refresh();
        }
    }

    private void initView() {
        kategori = getIntent().getStringExtra("kategori");
        dbHelper = new SQLiteHelper(this);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ListProdukAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(this, ViewProdukActivity.class);
            intent.putExtra("detail", item.clone());
            startActivity(intent);
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

        View.OnClickListener brand = v -> {
            int id = v.getId();
            String merek = id == R.id.chipAsus ? "ASUS" : id == R.id.chipLenovo ? "Lenovo"
                    : id == R.id.chipAcer ? "Acer" : id == R.id.chipApple ? "Apple" : "";
            binding.etSearch.setText(merek);
        };
        binding.chipSemuaKatalog.setOnClickListener(brand);
        binding.chipAsus.setOnClickListener(brand);
        binding.chipLenovo.setOnClickListener(brand);
        binding.chipAcer.setOnClickListener(brand);
        binding.chipApple.setOnClickListener(brand);
        binding.btnHeaderMenu.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());

        refresh();
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
        boolean demo = items.isEmpty() && (keyword == null || keyword.isEmpty());
        if (demo) {
            items = DemoData.laptops();
        }
        adapter.updateData(items);
        binding.tvResultInfo.setText((demo ? DemoData.TOTAL_PRODUK : items.size())
                + " " + getString(R.string.dp_produk_tersedia));
        binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
