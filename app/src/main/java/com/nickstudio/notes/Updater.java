package com.nickstudio.notes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater {
    private static final String VERSION_FILE_DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=1I6AX03DGKyVN3hct7qvJkfHgB2QCzc-0";
    private static final String BUILD_FILE_DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=1TNNfhM2O8ix76mla2v-y6jLCp3MtH163";

    private static String currentVersion;

    public static void checkForUpdates(Context context) {
        new DownloadStringTask(context, (result) -> {
            if (result != null) {
                if (isUpdateAvailable(context, result)) {
                    new AlertDialog.Builder(context)
                            .setMessage("Update available! Update now?")
                            .setPositiveButton("Update", (dialog, which) -> update(context))
                            .setNegativeButton("Cancel", null)
                            .show();
                    currentVersion = result;
                }
            }
        }).execute(VERSION_FILE_DOWNLOAD_URL);
    }

    private static boolean isUpdateAvailable(Context context, String upstreamVersion) {
        return !areVersionsEqual(upstreamVersion, getAppVersion(context));
    }

    private static String getBuildFilePath(Context context) {
        //return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/build.apk";
        return context.getFilesDir().getAbsolutePath() + "/build.zip";
    }

    private static String getBuildFileApkPath(Context context) {
        return getApkTempDirectory() + "/build.apk";
    }

    private static String getApkTempDirectory() {
        return "/sdcard/.notes-temp";
    }

    private static boolean areVersionsEqual(String version1, String version2) {
        String[] splitVersion1 = version1.split("\\.");
        String[] splitVersion2 = version2.split("\\.");

        if (Integer.parseInt(splitVersion1[0]) == Integer.parseInt(splitVersion2[0])) {
            if (Integer.parseInt(splitVersion1[1]) == Integer.parseInt(splitVersion2[1])) {
                if (Integer.parseInt(splitVersion1[2]) == Integer.parseInt(splitVersion2[2])) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void update(Context context) {
        //String folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        //IO.writeFile(folder + "/testing.txt", "test");
        new DownloadFileTask(context, getBuildFilePath(context), (success) -> {
            if (success) {
                File tempDir = new File(getApkTempDirectory());
                if (!tempDir.exists()) {
                    tempDir.mkdir();
                }

                try {
                    unzip(new File(getBuildFilePath(context)), tempDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*File apkFile = new File(getBuildFileApkPath(context));
                apkFile.setReadable(true, false);
                apkFile.setExecutable(true, false);*/

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uriFromFile(context, new File(getBuildFileApkPath(context))), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }).execute(BUILD_FILE_DOWNLOAD_URL);
    }

    private static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    private static void unzip(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                }
            }
        }
    }

    private static String getAppVersion(Context context) {
        try{
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
