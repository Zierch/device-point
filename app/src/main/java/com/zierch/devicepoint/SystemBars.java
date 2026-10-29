package com.zierch.devicepoint;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Keeps screen content clear of the status and navigation bars. Apps that target API 35+
 * draw edge to edge whether they want to or not, so every screen pads itself here.
 */
public final class SystemBars {

    private SystemBars() {
    }

    public static void apply(View root) {
        final int start = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int end = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(start + bars.left, top + bars.top, end + bars.right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
