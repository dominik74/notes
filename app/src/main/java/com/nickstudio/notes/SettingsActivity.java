package com.nickstudio.notes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        setSupportActionBar(findViewById(R.id.toolbarSettings));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();
    }

    private void init() {
        findViewById(R.id.btnWipeSettings).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("Warning! This action will clear all application settings" +
                            " and can NOT be undone.")
                    .setPositiveButton("Wipe", (dialog, which) -> {
                        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                        Toast.makeText(this, "Settings cleared", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(getIntent());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}