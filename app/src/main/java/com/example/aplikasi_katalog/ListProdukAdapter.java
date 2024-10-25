package com.example.aplikasi_katalog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aplikasi_katalog.databinding.FormTambahProdukBinding;
import com.example.aplikasi_katalog.databinding.ItemListProdukBinding;
import com.example.aplikasi_katalog.ui.Admin.FormProduk;

import java.io.File;
import java.util.List;


public class ListProdukAdapter extends RecyclerView.Adapter<ListProdukAdapter.ViewHolder> {
    private List<Object[]> itemList;
    private OnItemClickListener listener;
    private SQLiteHelper dbHelper;
    private String username, password;


    public ListProdukAdapter(List<Object[]> itemList, OnItemClickListener listener, Context context) {
        this.itemList = itemList;
        this.listener = listener;
        this.dbHelper = new SQLiteHelper(context);

        //    ambil data dari sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("DataUser", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        password = sharedPreferences.getString("password", "");
    }

    @NonNull
    @Override
    public ListProdukAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListProdukAdapter.ViewHolder(ItemListProdukBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListProdukAdapter.ViewHolder holder, int position) {
        Object[] item = itemList.get(position);
        int productId = (int) item[0];
        String imagePath = (String) item[1];
        String namaProduk = (String) item[2];
        int hargaProduk = (int) item[3];

        Glide.with(holder.itemView.getContext())
                .load(new File(imagePath))
                .into(holder.binding.imageProduk1);
        holder.binding.namaProduk.setText(namaProduk);
        holder.binding.hargaProduk.setText(Helper.currencyID(hargaProduk));

        boolean isAdmin = dbHelper.isAdmin(username, password);
        if (isAdmin){
            holder.binding.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Memanggil Activity atau Dialog untuk Edit Produk
                    Intent intent = new Intent(holder.itemView.getContext(), FormProduk.class);
                    intent.putExtra("produk_id", productId);
                    holder.itemView.getContext().startActivity(intent);
                }
            });

            holder.binding.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isDeleted = dbHelper.deleteProduct(productId);
                    int currentPosition = holder.getAdapterPosition();
                    if (isDeleted) {
                        itemList.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, itemList.size());
                    } else {
                        Toast.makeText(holder.itemView.getContext(), "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            holder.binding.aksi.setVisibility(View.GONE);
        }

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

    public void updateData(List<Object[]> newItemList) {
        itemList.clear();
        itemList.addAll(newItemList);
        notifyDataSetChanged(); // Memberitahu RecyclerView untuk memperbarui tampilan
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
