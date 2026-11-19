package com.zierch.devicepoint;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zierch.devicepoint.ui.BerandaActivity;
import com.zierch.devicepoint.ui.CartActivity;
import com.zierch.devicepoint.ui.ListProdukActivity;
import com.zierch.devicepoint.ui.ProfileActivity;

/** Nav bawah 4 tab sesuai desain FigCraft. Dipakai lewat include di setiap layar. */
public final class BottomNav {

    private static final int[] TABS = {
            R.id.tabBeranda, R.id.tabKatalog, R.id.tabKeranjang, R.id.tabProfil
    };
    private static final int[] ICONS = {
            R.id.tabIcon1, R.id.tabIcon2, R.id.tabIcon3, R.id.tabIcon4
    };
    private static final int[] LABELS = {
            R.id.tabLabel1, R.id.tabLabel2, R.id.tabLabel3, R.id.tabLabel4
    };
    private static final Class<?>[] TARGETS = {
            BerandaActivity.class, ListProdukActivity.class,
            CartActivity.class, ProfileActivity.class
    };

    private BottomNav() {
    }

    public static void attach(AppCompatActivity activity, View root, int active) {
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            View icon = root.findViewById(ICONS[i]);
            TextView label = root.findViewById(LABELS[i]);
            if (i == active) {
                icon.setBackgroundResource(R.drawable.dp_pill_blue);
                label.setTextColor(activity.getColor(R.color.dp_primary));
            } else {
                icon.setBackgroundColor(Color.TRANSPARENT);
                label.setTextColor(activity.getColor(R.color.dp_muted));
            }

            View tab = root.findViewById(TABS[i]);
            tab.setOnClickListener(v -> {
                if (index == active) return;
                Intent intent = new Intent(activity, TARGETS[index]);
                if (index == 1) intent.putExtra("kategori", "Laptop");
                activity.startActivity(intent);
            });
        }

        TextView badge = root.findViewById(R.id.cartBadgeText);
        int count = CartStore.count(activity);
        badge.setText(String.valueOf(count));
        badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }
}
