package com.nickstudio.notes;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadStringTask extends AsyncTask<String, Integer, String> {

    public interface OnFinishedCallback {
        void call(String result);
    }

    private final Context context;
    private OnFinishedCallback onFinished;

    public DownloadStringTask(Context context, OnFinishedCallback onFinished) {
        this.context = context;
        this.onFinished = onFinished;
    }

    @Override
    protected String doInBackground(String... urlString) {
        try {
            URL url = new URL(urlString[0]);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder result = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                Log.d(TAG, "doInBackground: output:" + str);
                result.append(str);
            }
            in.close();
            return result.toString();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (onFinished != null)
            onFinished.call(result);
    }
}
