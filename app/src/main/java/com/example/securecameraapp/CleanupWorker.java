package com.example.securecameraapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class CleanupWorker extends Worker {
    private static final String TAG = "CleanupWorker";
    private static final int DAYS_TO_KEEP = 30;

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoDelete = prefs.getBoolean(SettingsActivity.PREF_AUTO_DELETE, false);

        if (!autoDelete) {
            Log.d(TAG, "Auto-delete is disabled, skipping cleanup");
            return Result.success();
        }

        Log.d(TAG, "Starting photo cleanup...");

        // Get photo directory
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null || !picturesDir.exists()) {
            Log.e(TAG, "Pictures directory not found");
            return Result.failure();
        }

        // Calculate cutoff date (30 days ago)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -DAYS_TO_KEEP);
        Date cutoffDate = calendar.getTime();

        Log.d(TAG, "Will delete photos older than " + cutoffDate);

        // Get all image files
        File[] files = picturesDir
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));

        int deletedCount = 0;
        if (files != null) {
            for (File file : files) {
                Date fileDate = new Date(file.lastModified());
                if (fileDate.before(cutoffDate)) {
                    Log.d(TAG, "Deleting old file: " + file.getName());
                    if (file.delete()) {
                        deletedCount++;
                    } else {
                        Log.e(TAG, "Failed to delete file: " + file.getName());
                    }
                }
            }
        }

        Log.d(TAG, "Cleanup complete. Deleted " + deletedCount + " old photos");
        return Result.success();
    }
}
