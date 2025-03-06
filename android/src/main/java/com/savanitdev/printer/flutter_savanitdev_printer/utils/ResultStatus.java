package com.savanitdev.printer.flutter_savanitdev_printer.utils;

import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class ResultStatus {
    // Add a class-level map to track which results have been replied to
    private final Map<MethodChannel.Result, Boolean> resultReplied = new HashMap<>();

    private synchronized boolean hasReplied(MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Boolean.TRUE.equals(resultReplied.getOrDefault(result, false));
        }
        return false;
    }

    private synchronized void markReplied(MethodChannel.Result result) {
        resultReplied.put(result, true);
    }
    public void setResult(MethodChannel.Result result,boolean status) {
        if (!hasReplied(result)) {
            markReplied(result);
            result.success(status);
        } else {
            Log.w("Xprinter", "Attempted to reply to an already replied result with success");
        }
    }
    public void setResultMethod(MethodChannel.Result result,Object value) {
        if (!hasReplied(result)) {
            markReplied(result);
            result.success(value);
        } else {
            Log.w("Xprinter", "Attempted to reply to an already replied result with success");
        }
    }
    public void setResultErrorMethod(MethodChannel.Result result,String value) {
        if (!hasReplied(result)) {
            markReplied(result);
            result.error(StatusPrinter.ERROR,"call error",value);
        } else {
            Log.w("Xprinter", "Attempted to reply to an already replied result with success");
        }
    }

}
