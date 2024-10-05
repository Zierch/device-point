package com.example.aplikasi_katalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplikasi_katalog.databinding.ItemListProdukBinding;

import java.util.List;


public class ListProdukAdapter extends RecyclerView.Adapter<ListProdukAdapter.ViewHolder> {
    private List<Object[]> itemList;
    private OnItemClickListener listener;

    public ListProdukAdapter(List<Object[]> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListProdukAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListProdukAdapter.ViewHolder(ItemListProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListProdukAdapter.ViewHolder holder, int position) {
        Object[] item = itemList.get(position);
        int imageRes = (int) item[0];
        String namaProduk = (String) item[1];
        int hargaProduk = (int) item[2];

        holder.binding.imageProduk1.setImageResource(imageRes); // Set gambar ke ImageView
        holder.binding.namaProduk.setText(namaProduk);
        holder.binding.hargaProduk.setText(Helper.currencyID(hargaProduk));

        // Menambahkan OnClickListener untuk item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item); // Panggil listener dengan data item
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Object[] itemData);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ItemListProdukBinding binding;


        public ViewHolder(@NonNull ItemListProdukBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
