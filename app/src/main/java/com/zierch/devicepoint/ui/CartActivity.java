package com.zierch.devicepoint.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zierch.devicepoint.CartStore;
import com.zierch.devicepoint.Helper;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SystemBars;
import com.zierch.devicepoint.databinding.ActivityCartBinding;

import java.util.ArrayList;
import java.util.List;

/** Keranjang belanja sesuai frame 05 desain FigCraft. */
public class CartActivity extends AppCompatActivity {

    private static final int VOUCHER = 500_000;

    private ActivityCartBinding binding;
    private List<CartStore.Item> items = new ArrayList<>();
    private boolean[] selected = new boolean[0];
    private CartAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SystemBars.apply(binding.getRoot());

        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        reload();

        binding.cbAll.setOnClickListener(v -> {
            boolean value = !allSelected();
            for (int i = 0; i < selected.length; i++) selected[i] = value;
            adapter.notifyDataSetChanged();
            updateSummary();
        });
        binding.btnHapus.setOnClickListener(v -> {
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) CartStore.remove(this, items.get(i).id);
            }
            reload();
        });
        binding.btnClear.setOnClickListener(v -> {
            for (CartStore.Item item : items) CartStore.remove(this, item.id);
            reload();
        });
        binding.btnCheckout.setOnClickListener(v ->
                Toast.makeText(this, R.string.dp_belum_tersedia, Toast.LENGTH_SHORT).show());
    }

    private void reload() {
        items = CartStore.get(this);
        selected = new boolean[items.size()];
        for (int i = 0; i < selected.length; i++) selected[i] = true;
        adapter = new CartAdapter(items, selected, this::updateSummary);
        binding.recyclerCart.setAdapter(adapter);
        binding.tvEmpty.setVisibility(items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        updateSummary();
    }

    private boolean allSelected() {
        for (boolean value : selected) if (!value) return false;
        return true;
    }

    private void updateSummary() {
        int count = 0;
        int subtotal = 0;
        for (int i = 0; i < items.size(); i++) {
            if (!selected[i]) continue;
            CartStore.Item item = items.get(i);
            count += item.qty;
            subtotal += item.harga * item.qty;
        }
        int diskon = Math.min(VOUCHER, subtotal);
        int total = subtotal - diskon;

        binding.tvSub.setText(count + " " + getString(R.string.dp_keranjang_sub));
        binding.tvSelectAll.setText(getString(R.string.dp_pilih_semua) + " (" + count + ")");
        binding.tvSubLabel.setText(getString(R.string.dp_subtotal) + " (" + count + " produk)");
        binding.tvSubtotal.setText("Rp " + Helper.currencyID(subtotal));
        binding.tvTotal.setText("Rp " + Helper.currencyID(total));
        binding.tvTotalBottom.setText("Rp " + Helper.currencyID(total));
        binding.btnCheckout.setText(getString(R.string.dp_pesan) + " (" + count + ")");
        binding.cbAll.setText(allSelected() ? "✓" : "");
    }
}
