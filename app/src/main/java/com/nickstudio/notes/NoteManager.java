package com.nickstudio.notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NoteManager {

    public interface NoteChangesListener {
        void onNotesChanged();
    }

    private final List<NoteChangesListener> listeners = new LinkedList<>();
    private final NoteAdapter noteAdapter;
    private final ArrayList<Note> adapterList = new ArrayList<>();
    private final ArrayList<Note> workingList = new ArrayList<Note>();
    private final Context context;
    private final SharedPreferences prefs;

    private String saveDirectory;
    private String errorMessage;

    public NoteManager(Context context, String saveDirectory) {
        this.context = context;
        this.saveDirectory = saveDirectory;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        noteAdapter = new NoteAdapter(context, R.layout.note, adapterList);
    }

    public static String generateDateFormat() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat
                = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }

    public void addNoteChangesListener(NoteChangesListener listener) {
        listeners.add(listener);
    }

    public void removeNoteChangesListener(NoteChangesListener listener) {
        listeners.remove(listener);
    }

    public int getNoteCount() {
        return workingList.size();
    }

    public NoteAdapter getNoteAdapter() {
        return noteAdapter;
    }

    public void setSaveDirectory(String value) {
        saveDirectory = value;
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
        File directory = new File(saveDirectory);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                errorMessage = "ERROR: unable to create directory";
                return null;
            }
        }

        String filePath = saveDirectory + "/note" + generateDateFormat() + ".txt";

        IO.writeFile(filePath, "");

        Note note = parseNote(new File(filePath));
        if (note != null)
            workingList.add(0, note);
        return note;
    }

    public void removeNote(Note note) {
        File file = new File(note.getFilePath());

        if (file.delete()) {
            workingList.remove(note);
            adapterList.remove(note);
            noteAdapter.notifyDataSetChanged();
            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
            notifyNotesChanged();
        }
    }

    public void removeNote(int index) {
        removeNote(getNoteAt(index));
    }

    public Note getNoteAt(int index) {
        return adapterList.get(index);
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

        notifyNotesChanged();
    }

    public void notifyDataSetChanged() {
        noteAdapter.notifyDataSetChanged();
        notifyNotesChanged();
    }

    private void notifyNotesChanged() {
        for (NoteChangesListener listener : listeners) {
            if (listener != null) {
                listener.onNotesChanged();
            }
        }
    }

}
