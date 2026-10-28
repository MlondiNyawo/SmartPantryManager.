package com.example.smartpantry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Shared toolbar + navigation menu (Pantry / Suggested / Settings) for every screen. */
public abstract class BaseActivity extends AppCompatActivity {

    /** Keeps the toolbar below the status bar (Android 15 draws apps edge to edge). */
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected void setupToolbar(String title, boolean showBack) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(androidx.appcompat.R.style.ThemeOverlay_AppCompat_Light);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBack);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { finish(); return true; }
        Class<?> target = null;
        if (id == R.id.action_pantry) target = PantryListActivity.class;
        else if (id == R.id.action_suggested) target = SuggestedRecipesActivity.class;
        else if (id == R.id.action_settings) target = SettingsActivity.class;
        if (target != null) {
            if (!target.isInstance(this)) {
                Intent i = new Intent(this, target);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}