package com.savanitdev.printer.flutter_savanitdev_printer.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LogPrinter {
    public static void writeTextFile(Context context, String fileName, String content) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                // Insert and get the Uri for the new file
                Uri uri = context.getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(content.getBytes());
                        outputStream.close();
//                        Toast.makeText(context, "File written to Downloads", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    throw new FileNotFoundException("Unable to create file in Downloads.");
                }
            } else {
                // For Android 9 and below
                File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsFolder.exists()) {
                    downloadsFolder.mkdirs();
                }
                File file = new File(downloadsFolder, fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
//                Toast.makeText(context, "File written to Downloads", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(context, "Failed to write file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
