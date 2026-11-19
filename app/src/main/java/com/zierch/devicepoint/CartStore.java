package com.zierch.devicepoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Keranjang lokal sederhana, disimpan di SharedPreferences. */
public final class CartStore {

    private static final String PREFS = "dp_cart";
    private static final String KEY = "items";

    public static final class Item {
        public final int id;
        public final String nama;
        public final String spec;
        public final int harga;
        public int qty;

        Item(int id, String nama, int harga, int qty, String spec) {
            this.id = id;
            this.nama = nama;
            this.harga = harga;
            this.qty = qty;
            this.spec = spec == null ? "" : spec;
        }
    }

    private CartStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static List<Item> get(Context context) {
        List<Item> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs(context).getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                items.add(new Item(o.getInt("id"), o.getString("nama"),
                        o.getInt("harga"), o.getInt("qty"), o.optString("spec", "")));
            }
        } catch (JSONException e) {
            Log.w("CartStore", "Data keranjang rusak, direset", e);
        }
        return items;
    }

    public static void save(Context context, List<Item> items) {
        JSONArray array = new JSONArray();
        for (Item item : items) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", item.id);
                o.put("nama", item.nama);
                o.put("harga", item.harga);
                o.put("qty", item.qty);
                o.put("spec", item.spec);
            } catch (JSONException e) {
                Log.e("CartStore", "Gagal serialize item " + item.id, e);
                continue;
            }
            array.put(o);
        }
        prefs(context).edit().putString(KEY, array.toString()).apply();
    }

    public static void add(Context context, int id, String nama, int harga, String spec) {
        List<Item> items = get(context);
        for (Item item : items) {
            if (item.id == id) {
                item.qty++;
                save(context, items);
                return;
            }
        }
        items.add(new Item(id, nama, harga, 1, spec));
        save(context, items);
    }

    public static void setQty(Context context, int id, int qty) {
        List<Item> items = get(context);
        for (Item item : items) {
            if (item.id == id) {
                item.qty = Math.max(1, Math.min(99, qty));
                break;
            }
        }
        save(context, items);
    }

    public static void remove(Context context, int id) {
        List<Item> items = get(context);
        items.removeIf(item -> item.id == id);
        save(context, items);
    }

    public static int count(Context context) {
        int total = 0;
        for (Item item : get(context)) {
            total += item.qty;
        }
        return total;
    }
}
