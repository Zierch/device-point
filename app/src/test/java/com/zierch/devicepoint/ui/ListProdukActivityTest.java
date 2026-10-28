package com.zierch.devicepoint.ui;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.zierch.devicepoint.R;
import com.zierch.devicepoint.SQLiteHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ListProdukActivityTest {

    private static final String DB_NAME = "katalog.db";

    private Context context;
    private SQLiteHelper helper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DB_NAME);
        helper = new SQLiteHelper(context);
        helper.insertProduct("", "Laptop", "Laptop Gaming", 15000000, "Asus", "12 Bulan", 1, "Gaming");
        helper.insertProduct("", "Laptop", "Laptop Kantor", 7000000, "Lenovo", "12 Bulan", 2, "Kantor");
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void listIsAlreadyFilledWhenTheScreenOpens() {
        ListProdukActivity activity = launch();

        assertEquals(2, adapterCount(activity));
    }

    @Test
    public void typingInTheSearchBoxFiltersTheList() {
        ListProdukActivity activity = launch();
        EditText search = activity.findViewById(R.id.etSearch);

        search.setText("lenovo");

        assertEquals(1, adapterCount(activity));
    }

    @Test
    public void clearingTheSearchBringsEverythingBack() {
        ListProdukActivity activity = launch();
        EditText search = activity.findViewById(R.id.etSearch);

        search.setText("lenovo");
        search.setText("");

        assertEquals(2, adapterCount(activity));
    }

    private ListProdukActivity launch() {
        Intent intent = new Intent(context, ListProdukActivity.class).putExtra("kategori", "Laptop");
        return Robolectric.buildActivity(ListProdukActivity.class, intent).setup().get();
    }

    private int adapterCount(ListProdukActivity activity) {
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
        return recyclerView.getAdapter() == null ? 0 : recyclerView.getAdapter().getItemCount();
    }
}
