package com.zierch.devicepoint;

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
import com.zierch.devicepoint.databinding.FormTambahProdukBinding;
import com.zierch.devicepoint.databinding.ItemProdukKatalogBinding;
import com.zierch.devicepoint.ui.admin.FormProduk;

import java.io.File;
import java.util.List;


public class ListProdukAdapter extends RecyclerView.Adapter<ListProdukAdapter.ViewHolder> {
    private List<Object[]> itemList;
    private OnItemClickListener listener;
    private SQLiteHelper dbHelper;
    private String username;


    public ListProdukAdapter(List<Object[]> itemList, OnItemClickListener listener, Context context) {
        this.itemList = itemList;
        this.listener = listener;
        this.dbHelper = new SQLiteHelper(context);

        //    ambil data dari sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("DataUser", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
    }

    @NonNull
    @Override
    public ListProdukAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListProdukAdapter.ViewHolder(ItemProdukKatalogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListProdukAdapter.ViewHolder holder, int position) {
        Object[] item = itemList.get(position);
        int productId = (int) item[0];
        String imagePath = (String) item[1];
        String namaProduk = (String) item[2];
        int hargaProduk = (int) item[3];

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(new File(imagePath))
                    .placeholder(R.drawable.img_product_placeholder)
                    .error(R.drawable.img_product_placeholder)
                    .into(holder.binding.imageProduk1);
        }
        String merek = (String) item[4];
        int stok = (int) item[6];
        holder.binding.tvMerek.setText(DemoData.brandOf(namaProduk, merek));
        holder.binding.namaProduk.setText(DemoData.modelOf(namaProduk));
        holder.binding.hargaProduk.setText("Rp " + Helper.currencyID(hargaProduk));
        holder.binding.imageProduk1.setContentDescription(namaProduk);
        holder.binding.tvRating.setText("★ " + DemoData.ratingOf(namaProduk, productId));
        holder.binding.tvStok.setText("● Stok: " + stok);
        int tag;
        if (namaProduk != null && namaProduk.contains("Vivobook")) tag = 0;
        else if (namaProduk != null && namaProduk.contains("IdeaPad")) tag = 1;
        else if (namaProduk != null && namaProduk.contains("MacBook")) tag = 2;
        else tag = -1;
        holder.binding.tvTag.setVisibility(tag < 0 ? android.view.View.GONE : android.view.View.VISIBLE);
        holder.binding.tvTag.setBackgroundResource(tag == 0 ? R.drawable.dp_pill_blue
                : tag == 1 ? R.drawable.dp_tag_amber : R.drawable.dp_tag_red);
        holder.binding.tvTag.setText(tag == 0 ? "BARU" : tag == 1 ? "PROMO" : "HOT");
        holder.binding.btnDetail.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        boolean isAdmin = dbHelper.isAdmin(username);
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
                        ImageStore.delete(holder.itemView.getContext(), imagePath);
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
        protected ItemProdukKatalogBinding binding;


        public ViewHolder(@NonNull ItemProdukKatalogBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
