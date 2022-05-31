package com.nickstudio.notes;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class TextEditorActivity extends AppCompatActivity {

    private EditText textEditorEdit;
    private Toolbar toolbar;
    private TextView titleText;

    private String filePath;
    private String fileName;
    private String content;

    private boolean isToolbarColorDefault = true;
    private boolean isDirty;

    private ToolbarController toolbarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        processIntent();
    }

    private void init() {
        textEditorEdit = findViewById(R.id.textEditorEditText);
        toolbar = findViewById(R.id.textEditorToolbar);
        titleText = findViewById(R.id.textEditorTitle);

        toolbarController = new ToolbarController(toolbar, getWindow());

        textEditorEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(content))
                    markDirty();
                else
                    markClean();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ScrollView sv = findViewById(R.id.textEditorScrollView);
        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (sv.getScrollY() == 0) {
                    if (!isToolbarColorDefault) {
                        toolbarController.animateColor(fetchThemeColor(R.attr.defaultBackground));
                        isToolbarColorDefault = true;
                    }
                }
                else {
                    if (isToolbarColorDefault) {
                        toolbarController.animateColor(fetchThemeColor(R.attr.defaultForeground));
                        isToolbarColorDefault = false;
                    }
                }
            }
        });
    }

    private void processIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filePath = extras.getString("filePath");

            load();

            if (extras.getBoolean("autoFocusText")) {
                textEditorEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textEditorEdit, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void save() {
        content = textEditorEdit.getText().toString();
        IO.writeFile(filePath, content);
        markClean();
        Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
    }

    private void reload() {
        if (isDirty) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to discard unsaved changes to this document?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        load();
                        Toast.makeText(this, "File reloaded", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Continue editing", null)
                    .show();
            return;
        }

        load();
        Toast.makeText(this, "File reloaded", Toast.LENGTH_SHORT).show();
    }

    private void load() {
        fileName = filePath.substring(filePath.lastIndexOf("/")+1);

        String text = IO.readFile(filePath);
        textEditorEdit.setText(text);
        content = text;

        markClean();
    }

    private void markDirty() {
        titleText.setText(String.format("*%s", fileName));
        isDirty = true;
    }

    private void markClean() {
        titleText.setText(fileName);
        isDirty = false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (isToolbarColorDefault) {
            toolbarController.setColor(fetchThemeColor(R.attr.defaultBackground));
        }
        else {
            toolbarController.setColor(fetchThemeColor(R.attr.defaultForeground));
        }
    }

    @Override
    public void onBackPressed() {

        if (isDirty) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to discard unsaved changes to this document?")
                    .setPositiveButton("Exit", (dialog, which) -> finish())
                    .setNegativeButton("Continue editing", null)
                    .show();
            return;
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_text_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnTextEditorSave:
                save();
                return true;
            case R.id.btnTextEditorReload:
                reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int fetchThemeColor(int resId) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { resId });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}