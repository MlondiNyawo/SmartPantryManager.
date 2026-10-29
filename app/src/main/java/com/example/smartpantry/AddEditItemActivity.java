package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Add a new ingredient, or edit an existing one when EXTRA_ITEM_ID is passed in the Intent. */
public class AddEditItemActivity extends BaseActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final List<String> UNITS = Arrays.asList("pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup");

    private EditText etName, etQty;
    private Spinner spUnit;
    private Button btnExpiry;
    private String expiry = "";
    private PantryItem editing; // null when adding
    private PantryDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = PantryDbHelper.getInstance(this);
        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        spUnit = findViewById(R.id.spUnit);
        btnExpiry = findViewById(R.id.btnExpiry);
        Button btnClear = findViewById(R.id.btnClearExpiry);
        Button btnSave = findViewById(R.id.btnSave);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        long id = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (id != -1) {
            editing = db.getItem(id);
        }
        if (editing != null) {
            setupToolbar("Edit Ingredient", true);
            etName.setText(editing.getName());
            etQty.setText(PantryAdapter.formatQty(editing.getQuantity()));
            spUnit.setSelection(Math.max(0, UNITS.indexOf(editing.getUnit())));
            expiry = editing.getExpiry() == null ? "" : editing.getExpiry();
        } else {
            setupToolbar("Add Ingredient", true);
            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
            spUnit.setSelection(Math.max(0, UNITS.indexOf(prefs.getString(SettingsActivity.KEY_DEFAULT_UNIT, "pcs"))));
        }
        showExpiry();

        btnExpiry.setOnClickListener(v -> pickDate());
        btnClear.setOnClickListener(v -> { expiry = ""; showExpiry(); });
        btnSave.setOnClickListener(v -> save());
    }

    private void showExpiry() {
        btnExpiry.setText(expiry.isEmpty() ? "Pick expiry date (optional)" : "Expiry: " + expiry);
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            expiry = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            showExpiry();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Input validation, then INSERT (new) or UPDATE (existing). */
    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();

        if (name.length() < 2) {
            etName.setError("Enter an ingredient name (at least 2 characters)");
            return;
        }
        if (!name.matches("[A-Za-z][A-Za-z \\-']*")) {
            etName.setError("Use letters only");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number");
            return;
        }
        if (!Double.isFinite(qty) || qty <= 0 || qty > 100000) {
            etQty.setError("Quantity must be greater than 0 and no more than 100,000");
            return;
        }

        PantryItem item = editing != null ? editing : new PantryItem();
        item.setName(name);
        item.setQuantity(qty);
        item.setUnit(spUnit.getSelectedItem().toString());
        item.setExpiry(expiry);

        if (editing != null) {
            db.updateItem(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        } else {
            db.insertItem(item);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
