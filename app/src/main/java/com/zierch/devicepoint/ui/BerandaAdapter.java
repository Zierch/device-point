package com.zierch.devicepoint.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zierch.devicepoint.Helper;
import com.zierch.devicepoint.databinding.ItemProdukPilihanBinding;

import java.util.List;
import java.util.function.Consumer;

/** Kartu "Produk Pilihan" pada Beranda. */
public class BerandaAdapter extends RecyclerView.Adapter<BerandaAdapter.Holder> {

    private final List<Object[]> items;
    private final Consumer<Object[]> onClick;

    public BerandaAdapter(List<Object[]> items, Consumer<Object[]> onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemProdukPilihanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Object[] item = items.get(position);
        String nama = (String) item[2];
        int harga = (int) item[3];
        String merek = (String) item[4];
        int stok = (int) item[6];

        holder.binding.tvNama.setText(nama);
        holder.binding.tvSpesifikasi.setText(merek);
        holder.binding.tvHarga.setText("Rp " + Helper.currencyID(harga));
        holder.binding.tvStok.setText("● Stok: " + stok);
        holder.itemView.setOnClickListener(v -> onClick.accept(item));
    }

    @Override
    public int getItemCount() {
        return Math.min(items.size(), 3);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemProdukPilihanBinding binding;

        Holder(ItemProdukPilihanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
