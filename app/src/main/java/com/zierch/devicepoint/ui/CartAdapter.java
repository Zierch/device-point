package com.zierch.devicepoint.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zierch.devicepoint.CartStore;
import com.zierch.devicepoint.DemoData;
import com.zierch.devicepoint.Helper;
import com.zierch.devicepoint.R;
import com.zierch.devicepoint.databinding.ItemCartBinding;

import java.util.List;

/** Baris keranjang dengan checkbox dan stepper jumlah. */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {

    private final List<CartStore.Item> items;
    private final boolean[] selected;
    private final Runnable onChange;

    public CartAdapter(List<CartStore.Item> items, boolean[] selected, Runnable onChange) {
        this.items = items;
        this.selected = selected;
        this.onChange = onChange;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CartStore.Item item = items.get(position);
        holder.binding.tvNama.setText(item.nama);
        holder.binding.tvSpec.setText(item.spec.isEmpty()
                ? DemoData.cartSpec(item.nama, "") : item.spec);
        holder.binding.tvHarga.setText("Rp " + Helper.currencyID(item.harga));
        holder.binding.tvQty.setText(String.valueOf(item.qty));
        holder.binding.cbItem.setText(selected[position] ? "✓" : "");
        holder.binding.cbItem.setTextColor(holder.binding.cbItem.getContext().getColor(
                selected[position] ? R.color.dp_bg : R.color.dp_muted));
        holder.binding.cbItem.setBackgroundResource(selected[position]
                ? R.drawable.dp_pill_blue : R.drawable.dp_circle_border);

        holder.binding.cbItem.setOnClickListener(v -> {
            selected[position] = !selected[position];
            notifyItemChanged(position);
            onChange.run();
        });
        holder.binding.btnMinus.setOnClickListener(v -> {
            item.qty = Math.max(1, item.qty - 1);
            CartStore.setQty(v.getContext(), item.id, item.qty);
            notifyItemChanged(position);
            onChange.run();
        });
        holder.binding.btnPlus.setOnClickListener(v -> {
            item.qty = Math.min(99, item.qty + 1);
            CartStore.setQty(v.getContext(), item.id, item.qty);
            notifyItemChanged(position);
            onChange.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;

        Holder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
