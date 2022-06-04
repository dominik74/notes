package com.nickstudio.notes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telecom.Call;

import java.util.ArrayList;
import java.util.List;

public class AsyncNotesLoader extends AsyncTask<Void, Integer, ArrayList<Note>> {

    private ProgressDialog progressBar;
    private Context context;
    private NoteManager noteManager;

    private boolean search;
    private String searchTerm;

    private boolean showProgressBar;

    public AsyncNotesLoader(Context context, NoteManager noteManager, boolean search,
                            String searchTerm, boolean showProgressBar) {
        this.context = context;
        this.noteManager = noteManager;
        this.search = search;
        this.searchTerm = searchTerm;
        this.showProgressBar = showProgressBar;
    }

    @Override
    protected void onPreExecute() {
        if (showProgressBar) {
            progressBar = new ProgressDialog(context);
            progressBar.setCancelable(false);
            progressBar.setMessage("Loading notes...");
            progressBar.setIndeterminate(true);
            progressBar.show();
        }

        noteManager.begin();
    }

    @Override
    protected ArrayList<Note> doInBackground(Void... voids) {
        if (search) {
            noteManager.search(searchTerm);
        }
        else {
            noteManager.loadNotes();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ArrayList<Note> notes) {
        noteManager.finish();
        if (progressBar != null)
            progressBar.dismiss();
    }
}
