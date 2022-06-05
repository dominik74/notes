package com.nickstudio.notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference pref = findPreference("open_device_assistant_settings");
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS));
                return true;
            });
        }
    }
}