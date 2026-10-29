package com.example.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Launcher screen: shows every pantry item from the database. */
public class PantryListActivity extends BaseActivity implements PantryAdapter.Listener {

    private PantryDbHelper db;
    private PantryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setupToolbar("My Pantry", false);

        db = PantryDbHelper.getInstance(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvPantry);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(); // reload from the database every time the screen returns to the foreground
    }

    private void refresh() {
        List<PantryItem> items = db.getAllItems();
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        adapter.setItems(items, prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true));
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent i = new Intent(this, AddEditItemActivity.class);
        i.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(i);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    int deletedRows = db.deleteItem(item.getId());
                    refresh();
                    String message = deletedRows == 1
                            ? item.getName() + " deleted"
                            : "Could not delete " + item.getName();
                    Toast.makeText(this, message,
                            deletedRows == 1 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
