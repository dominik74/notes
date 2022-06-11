package com.nickstudio.notes;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileTask extends AsyncTask<String, Integer, String> {

    public interface OnFinishedCallback {
        void call(boolean success);
    }

    private final Context context;
    private String saveLocation;
    private OnFinishedCallback onFinishedCallback;

    public DownloadFileTask(Context context, String saveLocation, OnFinishedCallback onFinishedCallback) {
        this.context = context;
        this.saveLocation = saveLocation;
        this.onFinishedCallback = onFinishedCallback;
    }

    @Override
    protected String doInBackground(String... urlString) {
        try {
            URL url = new URL(urlString[0]);

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(saveLocation);

            if (file.exists()) {
                if (!file.delete()) {
                    return "error: file not deleted";
                }
            }

            if (!file.createNewFile()) {
                return "error: file not created";
            }

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1)
            {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        super.onPostExecute(error);

        if (error != null) {
            Toast.makeText(context, "download error: " + error, Toast.LENGTH_LONG).show();
            Log.d(TAG, "onPostExecute: download error: " + error);
        }
        else {
            Toast.makeText(context, "file downloaded", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onPostExecute: file downloaded");
        }

        if (onFinishedCallback != null)
            onFinishedCallback.call(error == null);
    }
}
