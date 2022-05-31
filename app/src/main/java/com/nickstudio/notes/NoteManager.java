package com.nickstudio.notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class NoteManager {

    public String saveDirectory;

    private NoteAdapter noteAdapter;
    private ArrayList<Note> adapterList;
    private ListView listView;

    private ArrayList<Note> workingList = new ArrayList<Note>();

    private Context context;

    private int noteCount;
    private String errorMessage;
    private SharedPreferences prefs;

    public NoteManager(NoteAdapter noteAdapter, ArrayList<Note> adapterList, Context context,
                       ListView listView, String saveDirectory) {
        this.noteAdapter = noteAdapter;
        this.adapterList = adapterList;
        this.context = context;
        this.listView = listView;
        this.saveDirectory = saveDirectory;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getNoteCount() {
        return workingList.size();
    }

    public void loadNotes() {
        File directory = new File(saveDirectory);

        if (!directory.exists()) {
            if (!directory.mkdir()) {
                errorMessage = "ERROR: unable to create directory";
                return;
            }
        }

        File[] files = directory.listFiles();

        if (files == null) {
            errorMessage = "ERROR: directory.listFiles() returned NULL, permissions not granted";
            return;
        }

        if (files.length == 0)
            return;

        noteCount = files.length;

        Arrays.sort(files, (object1, object2) -> object2.getName().compareTo(object1.getName()));

        for (File file : files) {
            Note note = parseNote(file);
            if (note != null) {
                workingList.add(note);
            }
        }
    }

    public void search(String searchTerm) {
        File directory = new File(saveDirectory);

        if (!directory.exists()) {
            if (!directory.mkdir()) {
                errorMessage = "ERROR: unable to create directory";
                return;
            }
        }

        File[] files = directory.listFiles();

        if (files == null) {
            errorMessage = "ERROR: directory.listFiles() returned NULL, permissions not granted";
            return;
        }

        Arrays.sort(files, (object1, object2) -> object2.getName().compareTo(object1.getName()));

        for (File file : files) {
            Note note = parseNote(file);
            if (note != null) {
                if (note.text.toLowerCase().contains(searchTerm.toLowerCase())) {
                    workingList.add(note);
                }
            }
        }
    }

    public Note parseNote(File file) {
        String fileName = file.getName();

        if (fileName.length() != 27)
            return null;

        String dateTime = fileName.substring(4, 23);
        String[] parts = dateTime.split("_");

        String[] date = parts[0].split("-");
        String[] time = parts[1].split("-");

        String dateString = date[2] + "." + date[1] + "." + date[0].substring(2);
        String timeString = time[0] + ":" + time[1];

        return new Note(dateString, timeString, IO.readFile(file.getPath()), file.getPath());
    }

    public void openNote(Note note, boolean autoFocusText) {
        if (prefs.getBoolean("use_built_in_text_editor", true)) {
            Intent intent = new Intent(context, TextEditorActivity.class);
            intent.putExtra("filePath", note.getFilePath());
            intent.putExtra("autoFocusText", autoFocusText);
            context.startActivity(intent);
            return;
        }

        Uri uri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                new File(note.getFilePath()));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/plain");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.startActivity(intent);
    }

    public Note addNote() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String format = simpleDateFormat.format(new Date());

        String filePath = saveDirectory + "/note" + format + ".txt";

        IO.writeFile(filePath, "");

        Note note = parseNote(new File(filePath));
        if (note != null)
            workingList.add(0, note);
        return note;
    }

    public void begin() {
        workingList.clear();
        //noteAdapter.notifyDataSetChanged();
    }

    public void finish() {
        adapterList.clear();
        adapterList.addAll(workingList);
        noteAdapter.notifyDataSetChanged();

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            errorMessage = null;
        }
    }

}
