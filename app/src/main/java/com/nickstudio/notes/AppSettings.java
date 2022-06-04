package com.nickstudio.notes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.preference.PreferenceManager;

public class AppSettings {
    private static final String SAVE_DIRECTORY_DEFAULT =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .getPath();

    public static String getSaveDirectory(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("save_directory", SAVE_DIRECTORY_DEFAULT);
    }
}
