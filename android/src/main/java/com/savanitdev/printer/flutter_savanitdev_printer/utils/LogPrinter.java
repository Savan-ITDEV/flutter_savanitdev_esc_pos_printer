package com.savanitdev.printer.flutter_savanitdev_printer.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogPrinter {
    public static void writeTextFile(Context context, String fileName, String content) {
        try {
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String contentWithDate = dateTime + " - " + content + "\n";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                Uri uri = null;
                // Check if the file exists
                String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
                String[] selectionArgs = new String[]{fileName};
                try (Cursor cursor = context.getContentResolver().query(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        null
                )) {
                    if (cursor != null && cursor.moveToFirst()) {
                        uri = ContentUris.withAppendedId(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        );
                    }
                }
                if (uri == null) {
                    // File does not exist, create a new file
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                }

                if (uri != null) {
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "wa")) {
                        if (outputStream != null) {
                            outputStream.write(contentWithDate.getBytes());
                        }
                    }
                } else {
                    throw new FileNotFoundException("Unable to access or create file in Downloads.");
                }
            } else {
                // For Android 9 and below
                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsFolder.exists()) {
                    downloadsFolder.mkdirs();
                }
                File file = new File(downloadsFolder, fileName);

                try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
                    outputStream.write(contentWithDate.getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Optional: Show a message to the user about the error
            //  Toast.makeText(context, "Failed to write file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
