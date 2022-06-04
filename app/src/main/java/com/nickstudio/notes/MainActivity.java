package com.nickstudio.notes;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "APP_LOG";

    private ListView listView;
    private Toolbar toolbar;
    private EditText editText;
    private TextView txtNoteCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View shadowView;
    private View bottomBar;

    private NoteManager noteManager;

    private boolean ignoreNextTextChange;

    private AsyncTask<Void, Integer, ArrayList<Note>> asyncNotesLoader;

    private boolean isToolbarColorDefault = true;
    private boolean isRefreshPending;

    private SoftKeyboardStateWatcher softKeyboardStateWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean hasPermissions = checkAppPermissions();
        if (!hasPermissions)
            requestAppPermissions();
        Log.d(TAG, "onCreate: hasPermissions = " + hasPermissions);

        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        reloadNotes(true);
    }

    private boolean checkAppPermissions() {
        if (Build.VERSION.SDK_INT >= 30) {
            return Environment.isExternalStorageManager();
        }
        else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= 30) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    }, 1);
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
        }
    }

    private void init() {
        noteManager = new NoteManager(this, AppSettings.getSaveDirectory(this));
        noteManager.addNoteChangesListener(this::updateGui);

        listView = findViewById(R.id.listView);
        listView.setAdapter(noteManager.getNoteAdapter());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            isRefreshPending = true;
            noteManager.openNote((Note) parent.getItemAtPosition(position), false);
        });

        listView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.note_options_menu, menu);
        });

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            isRefreshPending = true;
            noteManager.openNote(noteManager.addNote(), true);
            noteManager.finish();
            txtNoteCount.setText(String.valueOf(noteManager.getNoteCount()));
        });

        toolbar = findViewById(R.id.toolbar);

        ignoreNextTextChange = true;

        editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignoreNextTextChange) {
                    ignoreNextTextChange = false;
                    return;
                }

                reloadNotes(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            editText.requestFocus();
            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        });

        txtNoteCount = findViewById(R.id.txtNoteCount);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadNotes(false);
        });

        TypedValue typedValue1 = new TypedValue();
        getTheme().resolveAttribute(R.attr.defaultForeground, typedValue1, true);
        int defaultForeground = ContextCompat.getColor(this, typedValue1.resourceId);

        TypedValue typedValue2 = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue2, true);
        int colorPrimary = ContextCompat.getColor(this, typedValue2.resourceId);


        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(defaultForeground);
        swipeRefreshLayout.setColorSchemeColors(colorPrimary);

        //ToolbarController toolbarController = new ToolbarController(toolbar, getWindow());

        listView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View firstChild = listView.getChildAt(0);
                if (firstChild == null)
                    return;

                if (firstChild.getTop() == 0) {
                    if (!isToolbarColorDefault) {
                        //toolbarController.setColor(fetchThemeColor(R.attr.defaultBackground));
                        toolbar.setBackgroundColor(fetchThemeColor(R.attr.defaultBackground));
                        toolbar.setElevation(0);
                        isToolbarColorDefault = true;
                    }
                }
                else {
                    if (isToolbarColorDefault) {
                        //toolbarController.setColor(fetchThemeColor(R.attr.defaultForeground));
                        if (isDarkTheme()) {
                            toolbar.setBackgroundResource(R.drawable.border_bottom);
                        } else {
                            toolbar.setBackgroundColor(fetchThemeColor(R.attr.defaultBackground));
                        }
                        toolbar.setElevation(50);
                        isToolbarColorDefault = false;
                    }
                }

                refreshBottomShadowState();
            }
        });

        shadowView = findViewById(R.id.shadowView);

        ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> refreshBottomShadowState());
        bottomBar = findViewById(R.id.bottomBar);

        softKeyboardStateWatcher
                = new SoftKeyboardStateWatcher(this, findViewById(android.R.id.content));
        softKeyboardStateWatcher.addSoftKeyboardStateListener(new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened() {}

            @Override
            public void onSoftKeyboardClosed() {
                editText.clearFocus();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnReload:
                reloadNotes(true);
                return true;
            case R.id.btnSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnDelete:
                new AlertDialog.Builder(this)
                        .setMessage("Confirm delete.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            AdapterView.AdapterContextMenuInfo info
                                    = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                            noteManager.removeNote(info.position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            case R.id.btnCopy:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Note note = noteManager.getNoteAt(info.position);

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("note contents", note.getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private int fetchThemeColor(int resId) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { resId });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    private boolean listViewScrollable() {
        return !(listView.getLastVisiblePosition() == listView.getAdapter().getCount() -1 &&
                listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight());
    }

    private void refreshBottomShadowState() {
        if (listView.getAdapter().getCount() == 0 || !listViewScrollable())
        {
            if (isDarkTheme()) {
                bottomBar.setBackgroundColor(fetchThemeColor(R.attr.defaultBackground));
            }

            ViewGroup.LayoutParams params = shadowView.getLayoutParams();
            params.height = 0;
            shadowView.setLayoutParams(params);
        } else {
            if (isDarkTheme()) {
                bottomBar.setBackgroundResource(R.drawable.border_top);
            }

            ViewGroup.LayoutParams params = shadowView.getLayoutParams();
            params.height = 40;
            shadowView.setLayoutParams(params);
        }
    }

    private boolean isDarkTheme() {
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRefreshPending) {
            reloadNotes(true);
            isRefreshPending = false;
        }
    }

    private void updateGui() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        txtNoteCount.setText(String.valueOf(noteManager.getNoteCount()));
    }

    private void reloadNotes(boolean showProgressbar) {
        noteManager.setSaveDirectory(AppSettings.getSaveDirectory(this));
        asyncNotesLoader = new AsyncNotesLoader(this, noteManager, false,
                "", showProgressbar).execute();
    }

    private void reloadNotes(String searchTerm) {
        if (asyncNotesLoader != null)
            asyncNotesLoader.cancel(true);
        asyncNotesLoader = new AsyncNotesLoader(this, noteManager, true,
                searchTerm, false).execute();
    }
}