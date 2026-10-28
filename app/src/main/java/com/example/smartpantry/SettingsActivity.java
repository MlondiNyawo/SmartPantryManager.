package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

/** Preferences screen (SharedPreferences, as in the study guide's Preferences section). */
public class SettingsActivity extends BaseActivity {

    public static final String PREFS = "smartpantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_DEFAULT_UNIT = "default_unit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar("Settings", true);

        final SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        Switch sw = findViewById(R.id.swExpiry);
        sw.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        sw.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());

        Spinner sp = findViewById(R.id.spDefaultUnit);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                AddEditItemActivity.UNITS));
        sp.setSelection(Math.max(0, AddEditItemActivity.UNITS.indexOf(
                prefs.getString(KEY_DEFAULT_UNIT, "pcs"))));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                prefs.edit().putString(KEY_DEFAULT_UNIT, AddEditItemActivity.UNITS.get(pos)).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
