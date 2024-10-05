package com.example.aplikasi_katalog;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.cardview.widget.CardView;

import java.text.NumberFormat;
import java.util.Locale;

public class Helper {
    public static void setOnClickListener(Context context, CardView card, Class<?> destinationClass, String key, String value) {
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, destinationClass);
                intent.putExtra(key, value);
                context.startActivity(intent);
            }
        });
    }

    public static String currencyID(double amount) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("id", "ID"));
        return numberFormat.format(amount);
    }
}
