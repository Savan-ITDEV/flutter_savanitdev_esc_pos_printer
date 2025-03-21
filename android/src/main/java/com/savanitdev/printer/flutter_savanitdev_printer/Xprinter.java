package com.savanitdev.printer.flutter_savanitdev_printer;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.LogPrinter;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.ResultStatus;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import net.posprinter.CPCLPrinter;
import net.posprinter.IDeviceConnection;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;
import net.posprinter.TSPLConst;
import net.posprinter.TSPLPrinter;
import net.posprinter.ZPLPrinter;
import net.posprinter.model.AlgorithmType;
import net.posprinter.posprinterface.IDataCallback;
import net.posprinter.posprinterface.IStatusCallback;

import zywell.posprinter.utils.BitmapProcess;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import io.flutter.plugin.common.MethodChannel;
import zywell.posprinter.utils.BitmapToByteData;

public class Xprinter {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, IDeviceConnection> connections = new HashMap<>();
    int rety = 0;
    int maxRety = 3;
    Context contextX;
    ResultStatus resultStatus = new ResultStatus();

    // Add a class-level map to track which results have been replied to
    private final Map<MethodChannel.Result, Boolean> resultReplied = new HashMap<>();

    private synchronized boolean hasReplied(MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return resultReplied.getOrDefault(result, false);
        }
        return false;
    }

    private synchronized void markReplied(MethodChannel.Result result) {
        resultReplied.put(result, true);
    }
    private void safeSuccess(MethodChannel.Result result, Object value) {
        if (!hasReplied(result)) {
            markReplied(result);
            result.success(value);
        } else {
            Log.w("Xprinter", "Attempted to reply to an already replied result with success");
        }
    }

    private void safeError(MethodChannel.Result result, String errorCode, String errorMessage, Object errorDetails) {
        if (!hasReplied(result)) {
            markReplied(result);
            result.error(errorCode, errorMessage, errorDetails);
        } else {
            Log.w("Xprinter", "Attempted to reply to an already replied result with error: " + errorCode);
        }
    }
    public void initPrinter(Context context) {
        POSConnect.init(context);
        contextX = context;
        Log.e("Xprinter", "Start init");
    }
    public void connectMultiXPrinter(String address, String portType, @NonNull MethodChannel.Result result) {
        // Move connection operations off the main thread to prevent ANR
        new Thread(() -> {
            try {
                int type = POSConnect.DEVICE_TYPE_ETHERNET;
                if (Objects.equals(portType, "usb")) {
                    type = POSConnect.DEVICE_TYPE_USB;
                } else if (Objects.equals(portType, "bluetooth")) {
                    type = POSConnect.DEVICE_TYPE_BLUETOOTH;
                } else if (Objects.equals(portType, "serial")) {
                    type = POSConnect.DEVICE_TYPE_SERIAL;
                }

                // Special handling for Bluetooth connections
                if (type == POSConnect.DEVICE_TYPE_BLUETOOTH) {
                    // Ensure any existing Bluetooth connection is properly closed first
                    checkInitConnection(address);

                    // Create the connection on a background thread
                    final IDeviceConnection connection = POSConnect.createDevice(type);
                    if (connection == null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                safeError(result, StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Failed to create Bluetooth connection")
                        );
                        return;
                    }

                    connections.put(address, connection);

                    // Connect with timeout to prevent hanging
                    connection.connect(address, (code, msg) -> {
                        // Handle connection result on the main thread
                        new Handler(Looper.getMainLooper()).post(() ->
                                connectListener(address, code, portType, result)
                        );
                    });
                } else {
                    // Handle other connection types
                    checkInitConnection(address);
                    final IDeviceConnection connection = POSConnect.createDevice(type);
                    if (connection == null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                safeError(result, StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Failed to create device connection")
                        );
                        return;
                    }

                    connections.put(address, connection);
                    connection.connect(address, (code, msg) -> {
                        // Handle connection result on the main thread
                        new Handler(Looper.getMainLooper()).post(() ->
                                connectListener(address, code, portType, result)
                        );
                    });
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        safeError(result, StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e.toString())
                );
            }
        }).start();
    }
    public void disconnectXPrinter(String address, @NonNull MethodChannel.Result result) {
        // Move disconnection to background thread
        new Thread(() -> {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection != null) {
                    removeConnection(address, result);
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeSuccess(result, StatusPrinter.DISCONNECT)
                    );
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.GET_ID_FAIL, StatusPrinter.GET_ID_FAIL_DETAIL)
                    );
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL, e.toString())
                );
            }
        }).start();
    }
    public void removeConnection(String address, @NonNull MethodChannel.Result result) {
        try {
            // Check if the connection exists for the given IP
            if (!connections.isEmpty() && connections.containsKey(address)) {
                // Retrieve and close the connection before removing it
                IDeviceConnection connection = connections.get(address);
                if (connection != null) {
                    try {
                        // Add timeout for close operation to prevent hanging
                        Thread closeThread = new Thread(() -> {
                            try {
                                connection.close();  // Close the connection safely
                            } catch (Exception e) {
                                Log.e("Xprinter", "Error closing connection: " + e.getMessage());
                            }
                        });

                        closeThread.start();

                        // Wait for close to complete with timeout
                        closeThread.join(2000); // 2 second timeout

                        // If thread is still running after timeout, interrupt it
                        if (closeThread.isAlive()) {
                            closeThread.interrupt();
                        }
                    } catch (Exception e) {
                        Log.e("Xprinter", "Error in connection close: " + e.getMessage());
                    }
                }
                // Remove the connection from the map
                connections.remove(address);
            } else {
                safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        } catch (Exception e) {
            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, e.toString());
        }
    }
    public void checkInitConnection(String address) {
        if (!connections.isEmpty() && connections.containsKey(address)) {
            // Retrieve and close the connection before removing it
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                try {
                    // Add timeout for close operation to prevent hanging
                    Thread closeThread = new Thread(() -> {
                        try {
                            connection.close();  // Close the connection safely
                        } catch (Exception e) {
                            Log.e("Xprinter", "Error closing connection in checkInitConnection: " + e.getMessage());
                        }
                    });

                    closeThread.start();

                    // Wait for close to complete with timeout
                    closeThread.join(2000); // 2 second timeout

                    // If thread is still running after timeout, interrupt it
                    if (closeThread.isAlive()) {
                        closeThread.interrupt();
                    }
                } catch (Exception e) {
                    Log.e("Xprinter", "Error in checkInitConnection: " + e.getMessage());
                }
            }
            connections.remove(address);
            Log.d("Xprinter", "Connection removed for IP: " + address);
        }
    }
    private void connectListener(String address, int code, String portType, @NonNull MethodChannel.Result result) {
        try {
            if (code == POSConnect.CONNECT_SUCCESS) {
                rety = 0;
                safeSuccess(result, StatusPrinter.CONNECTED);
            } else {
                if(code == POSConnect.CONNECT_INTERRUPT || code == POSConnect.CONNECT_FAIL){
                    if (rety < maxRety) {
                        rety++;
                        // Use a handler instead of direct recursion to avoid stack overflow
                        // We need to be careful not to reply multiple times to the same result
                        new Handler(Looper.getMainLooper()).post(() -> {
                            // Only retry if we haven't replied yet
                            if (!hasReplied(result)) {
                                connectMultiXPrinter(address, portType, result);
                            }
                        });
                    } else {
                        rety = 0; // Reset retry counter
                        safeError(result, StatusPrinter.ERROR, StatusPrinter.RETRY_FAILED, StatusPrinter.RETRY_FAILED3);
                    }
                } else {
                    safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                }
            }
        } catch (Exception e) {
            safeError(result, StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e.toString());
        }
    }
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public void statusXprinter(boolean isDevicePOS,String address, POSPrinter printer, IDeviceConnection connection, @NonNull MethodChannel.Result result) {
        int type = connection.getConnectType();
        try
        {
            printer.printerStatus(status -> {
                // Handle the received status here
                String msg = "";
                try {
                    Thread.sleep(500);
                    checkInitConnection(address);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                rety = 0;
                switch (status) {
                    case 0:
                        msg = "STS_NORMAL";
                        result.success(msg);
                        break;
                    case 8:
                        msg = "STS_COVEROPEN";
                        result.error(StatusPrinter.ERROR, msg, StatusPrinter.STS_COVEROPEN);
                        break;
                    case 16:
                        msg = "STS_PAPEREMPTY";
                        result.error(StatusPrinter.ERROR, msg, StatusPrinter.STS_PAPEREMPTY);
                        break;
                    case 32:
                        msg = "STS_PRESS_FEED";
                        result.error(StatusPrinter.ERROR,msg,StatusPrinter.STS_PRESS_FEED);
                        break;
                    case 64:
                        result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.STS_PRINTER_ERR);
                        break;
                    default:
                        msg = "STS_NORMAL";
                        if (status > 0) {
                            result.success(msg);
                            // static check for iMin and Sunmi printer
                        } else if (status == -4 || status == -65) {
                            if (isDevicePOS) {
                                result.error(StatusPrinter.ERROR,  StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                            } else {
                                result.success(msg);
                            }
                        } else {
                            result.error(StatusPrinter.ERROR,  StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                        }
                        break;
                }
            });
        } catch (Exception e){
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString());
        }
    }
    public void printImgESCX(String address, String base64String, boolean isDevicePOS, Integer width, @NonNull MethodChannel.Result result) {
        // Move printing operations to background thread
        new Thread(() -> {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection == null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                    );
                    return;
                }

                if (connection.isConnect()) {
                    POSPrinter printer = new POSPrinter(connection);
                    Bitmap bmp = decodeBase64ToBitmap(base64String);
                    final Bitmap bitmapToPrint = convertGreyImg(bmp);

                    // Print bitmap with timeout protection
                    boolean printSuccess = false;
                    try {
                        printer.initializePrinter().printBitmap(bitmapToPrint, POSConst.ALIGNMENT_CENTER, width).cutHalfAndFeed(0);
                        printSuccess = true;
                    } catch (Exception e) {
                        Log.e("Xprinter", "Error printing bitmap: " + e.getMessage());
                        new Handler(Looper.getMainLooper()).post(() ->
                                safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                        );
                        return;
                    }

                    if (printSuccess) {
                        try {
                            // Wait a bit for the printer to process
                            Thread.sleep(500);

                            // Check printer status
                            printer.printerStatus(status -> {
                                String msg = "";
                                try {
                                    // Don't close the connection here
                                    rety = 0;
                                    switch (status) {
                                        case 0:
                                            msg = "STS_NORMAL";
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeSuccess(result, "STS_NORMAL")
                                            );
                                            break;
                                        case 8:
                                            msg = "STS_COVEROPEN";
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeError(result, StatusPrinter.ERROR, "STS_COVEROPEN", StatusPrinter.STS_COVEROPEN)
                                            );
                                            break;
                                        case 16:
                                            msg = "STS_PAPEREMPTY";
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeError(result, StatusPrinter.ERROR, "STS_PAPEREMPTY", StatusPrinter.STS_PAPEREMPTY)
                                            );
                                            break;
                                        case 32:
                                            msg = "STS_PRESS_FEED";
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeError(result, StatusPrinter.ERROR, "STS_PRESS_FEED", StatusPrinter.STS_PRESS_FEED)
                                            );
                                            break;
                                        case 64:
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.STS_PRINTER_ERR)
                                            );
                                            break;
                                        default:
                                            msg = "STS_NORMAL";
                                            if (status > 0) {
                                                new Handler(Looper.getMainLooper()).post(() ->
                                                        safeSuccess(result, "STS_NORMAL")
                                                );
                                            } else if (status == -4 || status == -65) {
                                                if (isDevicePOS) {
                                                    new Handler(Looper.getMainLooper()).post(() ->
                                                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                                                    );
                                                } else {
                                                    new Handler(Looper.getMainLooper()).post(() ->
                                                            safeSuccess(result, "STS_NORMAL")
                                                    );
                                                }
                                            } else {
                                                new Handler(Looper.getMainLooper()).post(() ->
                                                        safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                                                );
                                            }
                                            break;
                                    }
                                } catch (Exception e) {
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                                    );
                                }
                            });
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                            );
                        }
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL)
                    );
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                );
            }
        }).start();
    }
    public void printRawDataESC(String address, String encode, boolean isDevicePOS, @NonNull MethodChannel.Result result) {
        // Move printing operations to background thread
        new Thread(() -> {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection == null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                    );
                    return;
                }

                if (connection.isConnect()) {
                    POSPrinter printer = new POSPrinter(connection);
                    byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
                    System.out.println("Sent of size: " + bytes.length + " bytes");

                    // Send data with timeout protection
                    boolean sendSuccess = false;
                    try {
                        printer.initializePrinter().sendData(bytes);
                        sendSuccess = true;
                    } catch (Exception e) {
                        Log.e("Xprinter", "Error sending data: " + e.getMessage());
                        new Handler(Looper.getMainLooper()).post(() ->
                                safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                        );
                        return;
                    }

                    if (sendSuccess) {
                        // Use a handler for the delay instead of Thread.sleep
                        try {
                            // Wait a bit for the printer to process
                            Thread.sleep(500);

                            // Check printer status on the same background thread
                            final POSPrinter finalPrinter = printer;
                            final IDeviceConnection finalConnection = connection;

                            // Check printer status
                            printer.printerStatus(status -> {
                                String msg = "";
                                switch (status) {
                                    case 0:
                                        msg = "STS_NORMAL";
                                        new Handler(Looper.getMainLooper()).post(() ->
                                                safeSuccess(result, "STS_NORMAL")
                                        );
                                        break;
                                    case 8:
                                        msg = "STS_COVEROPEN";
                                        new Handler(Looper.getMainLooper()).post(() ->
                                                safeError(result, StatusPrinter.ERROR, "STS_COVEROPEN", StatusPrinter.STS_COVEROPEN)
                                        );
                                        break;
                                    case 16:
                                        msg = "STS_PAPEREMPTY";
                                        new Handler(Looper.getMainLooper()).post(() ->
                                                safeError(result, StatusPrinter.ERROR, "STS_PAPEREMPTY", StatusPrinter.STS_PAPEREMPTY)
                                        );
                                        break;
                                    default:
                                        if (status > 0) {
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeSuccess(result, "STS_NORMAL")
                                            );
                                        } else {
                                            new Handler(Looper.getMainLooper()).post(() ->
                                                    safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, "Status: " + status)
                                            );
                                        }
                                        break;
                                }
                            });
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                            );
                        }
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                    );
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        safeError(result, StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString())
                );
            }
        }).start();
    }
    public void cutESCX(String address, @NonNull MethodChannel.Result result) {
        // Move to background thread
        new Thread(() -> {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection == null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                    );
                    return;
                }

                if (connection.isConnect()) {
                    POSPrinter printer = new POSPrinter(connection);
                    printer.cutHalfAndFeed(1);
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeSuccess(result, StatusPrinter.STS_NORMAL)
                    );
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            safeError(result, StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT)
                    );
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        safeError(result, StatusPrinter.ERROR, e.toString(), e.toString())
                );
            }
        }).start();
    }


    // =========================== function species printer ===========================

    public void connect(String address, String portType,boolean isCloseConnection, @NonNull MethodChannel.Result result) {
        try {
            int type = POSConnect.DEVICE_TYPE_ETHERNET;
            if (Objects.equals(portType, "usb")) {
                type = POSConnect.DEVICE_TYPE_USB;
            } else if (Objects.equals(portType, "bluetooth")) {
                type = POSConnect.DEVICE_TYPE_BLUETOOTH;
            } else if (Objects.equals(portType, "serial")) {
                type = POSConnect.DEVICE_TYPE_SERIAL;
            }
            if(isCloseConnection){
                checkInitConnection(address);
            }
            IDeviceConnection connection = POSConnect.createDevice(type);
            connections.put(address, connection);
            connection.connect(address, (code, msg) -> listener(address, code,portType,isCloseConnection, result));

        } catch (Exception e) {
            resultStatus.setResultErrorMethod(result,StatusPrinter.CONNECT_ERROR);
        }

    }
    private void listener(String address, int code,String portType,boolean isCloseConnection, @NonNull MethodChannel.Result result) {
        try {
            if (code == POSConnect.CONNECT_SUCCESS) {
                rety = 0;
                resultStatus.setResult(result,true);
            } else {
                if(code == POSConnect.CONNECT_INTERRUPT || code == POSConnect.CONNECT_FAIL){
                    if (rety < maxRety) {
                        rety++;
                        connect(address, portType,isCloseConnection, result);
                    } else {
                        rety = 0; // Reset retry counter
                        resultStatus.setResultErrorMethod(result,StatusPrinter.RETRY_FAILED3);
                    }
                }else{
                    resultStatus.setResultErrorMethod(result,StatusPrinter.RETRY_FAILED);
                }
            }
        } catch (Exception e) {
            resultStatus.setResultErrorMethod(result,StatusPrinter.CONNECT_ERROR);
        }
    }
    public void print(String address,String iniCommand,String cutterCommands, String encode,String img,boolean isCut,boolean isDisconnect, boolean isDevicePOS,Integer width, @NonNull MethodChannel.Result result) {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection == null) {
                    resultStatus.setResultErrorMethod(result,StatusPrinter.CONNECT_ERROR);
                    return;
                }
                if (connection.isConnect()) {
                    POSPrinter printer = new POSPrinter(connection);
                    byte[] bytes = Base64.decode(iniCommand, Base64.DEFAULT);
                    byte[] endBytes = Base64.decode(cutterCommands, Base64.DEFAULT);
                    byte[] encodeBytes = Base64.decode(encode, Base64.DEFAULT);
                        printer.initializePrinter().setAlignment(POSConst.ALIGNMENT_CENTER);
                        if (!iniCommand.isEmpty()) {
                            printer.sendData(bytes);
                        }
                        if (!img.isEmpty()) {
                            Bitmap bmp = decodeBase64ToBitmap(img);
                            final Bitmap bitmapToPrint = convertGreyImg(bmp);
                            printer.printBitmap(bitmapToPrint, POSConst.ALIGNMENT_CENTER, width);
                        }
                        if (!encode.isEmpty()) {
                            printer.sendData(encodeBytes);
                        }
                        if (isCut && cutterCommands.isEmpty()) {
                            printer.cutHalfAndFeed(0);
                        }
                         if (isCut && !cutterCommands.isEmpty()) {
                         printer.sendData(endBytes);
                         }
                        Thread.sleep(500);
                         if(isDevicePOS){
                             printWithBufferCheck(printer,result);
                         }else{
                             status(isDisconnect, address, printer, result);
                         }
                } else {
                    resultStatus.setResultErrorMethod(result,StatusPrinter.CONNECT_ERROR);
                }
            } catch (Exception e) {
                resultStatus.setResultErrorMethod(result,StatusPrinter.PRINT_FAIL);
            }

    }
    public void printWithBufferCheck(POSPrinter printer, @NonNull MethodChannel.Result result) {
        executor.submit(() -> {
        try {
            printer.printString("                       "); // ส่งคำสั่งพิมพ์
            printer.feedLine(0); // บังคับให้ Buffer ทำงานทันที
            System.out.println("status printing success");
            resultStatus.setResult(result,true);
        } catch (Exception e) {
            System.out.println("status printing error: " + e.getMessage());
            resultStatus.setResultErrorMethod(result,e.getMessage());
        }
        });
    }
    public void status(boolean isDisconnect,String address, POSPrinter printer, @NonNull MethodChannel.Result result) {
            try {
                printer.printerStatus(status -> {
                   Log.d("status ","status printing ========> " + status);
                    try {
                        if (isDisconnect) {
                            Thread.sleep(500);
                            checkInitConnection(address);
                        }
                    } catch (InterruptedException e) {
                        resultStatus.setResultErrorMethod(result,"error :" + e);
                        throw new RuntimeException(e);
                    }
                    rety = 0;
                    switch (status) {
                        case 0:
                            resultStatus.setResult(result,true);
                            break;
                        case 8:
                            resultStatus.setResultErrorMethod(result,StatusPrinter.STS_COVEROPEN);
                            break;
                        case 16:
                            resultStatus.setResultErrorMethod(result,StatusPrinter.STS_PAPEREMPTY);
                            break;
                        case 32:
                            resultStatus.setResultErrorMethod(result,StatusPrinter.STS_PRESS_FEED);
                            break;
                        case 64:
                            resultStatus.setResultErrorMethod(result,StatusPrinter.STS_PRINTER_ERR);
                            break;
                        default:
                                resultStatus.setResult(result,true);
                            break;
                    }
                });
            } catch (Exception e) {
                resultStatus.setResultErrorMethod(result,"status :" + e);
            }
    }
    public void disconnect(String address, @NonNull MethodChannel.Result result) {
            try {
                IDeviceConnection connection = connections.get(address);
                if (connection != null) {
                    connection.close();
                    connections.remove(address);
                    resultStatus.setResult(result,true);
                } else {
                    resultStatus.setResult(result,false);
                }
            } catch (Exception e) {
                resultStatus.setResult(result,false);
            }
    }

    // =========================== function species printer ===========================


    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        // The arithmetic average of a grayscale image; a threshold
        double redSum = 0, greenSum = 0, blueSun = 0;
        double total = width * height;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                redSum += red;
                greenSum += green;
                blueSun += blue;
            }
        }
        int m = (int) (redSum / total);

        // Conversion monochrome diagram
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int alpha1 = 0xFF << 24;
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                if (red >= m) {
                    red = green = blue = 255;
                } else {
                    red = green = blue = 0;
                }
                grey = alpha1 | (red << 16) | (green << 8) | blue;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }
    private static Bitmap convertGreyImgByFloyd(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];

        int e;
        int i;
        int j;
        int g;
        for(e = 0; e < height; ++e) {
            for(i = 0; i < width; ++i) {
                j = pixels[width * e + i];
                g = (j & 16711680) >> 16;
                gray[width * e + i] = g;
            }
        }

        for(i = 0; i < height; ++i) {
            for(j = 0; j < width; ++j) {
                g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = -1;
                    e = g - 255;
                } else {
                    pixels[width * i + j] = -16777216;
                    e = g - 0;
                }

                if (j < width - 1 && i < height - 1) {
                    gray[width * i + j + 1] += 3 * e / 8;
                    gray[width * (i + 1) + j] += 3 * e / 8;
                    gray[width * (i + 1) + j + 1] += e / 4;
                } else if (j == width - 1 && i < height - 1) {
                    gray[width * (i + 1) + j] += 3 * e / 8;
                } else if (j < width - 1 && i == height - 1) {
                    gray[width * i + j + 1] += e / 4;
                }
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }
    public static byte[] baBmpToSendData(int m, Bitmap mBitmap, BitmapToByteData.BmpType bmpType) {
        // Convert the image to grayscale
        Bitmap bitmap = convertGreyImg(mBitmap);
        // Apply the selected dithering or grayscale conversion method
        switch (bmpType.ordinal()) {
            case 2:
                bitmap = convertGreyImgByFloyd(bitmap);
                break;
            default:
                bitmap = convertGreyImg(bitmap);
        }

        // Define the maximum width for 80mm paper (typically 576 pixels)
        int maxWidth = 300;

        // Get the current width and height of the bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight() / 2 - 20;

        // Resize the bitmap if it exceeds the paper width
        if (width > maxWidth) {
            // Scale the bitmap to fit within the paper width while maintaining aspect ratio
            float aspectRatio = (float) height / width;
            height = (int) (maxWidth * aspectRatio) + 50;
            width = maxWidth;
            bitmap = Bitmap.createScaledBitmap(bitmap, width , height, true);
        }
        // Calculate the number of rows of bytes
        int n = (height + 7) / 8;

        // Initialize a list to hold the byte data
        ArrayList<Byte> list = new ArrayList<>();

        // Process the pixels row by row
        for (int i = 0; i < n; ++i) {
            int[] perPix = new int[width * 8];
            // Extract 8 rows of pixels at a time
            for (int j = 0; j < perPix.length; ++j) {
                if (j + 8 * i * width < width * height) {
                    perPix[j] = bitmap.getPixel(j % width, j / width + 8 * i);
                } else {
                    perPix[j] = -1; // Padding for incomplete rows
                }
            }
            // Convert the pixels to printable byte data
            byte[] data = bagetbmpdata(perPix, width, m);
            for (byte b : data) {
                list.add(b);
            }
        }

        // Convert the list of bytes to a byte array
        byte[] newdata = new byte[list.size()];
        for (int i = 0; i < newdata.length; ++i) {
            newdata[i] = list.get(i);
        }

        return newdata;
    }
    private static byte[] bagetbmpdata(int[] b, int w, int m) {
        int nH = w / 256;
        int nL = w % 256;
        byte[] head = new byte[]{27, 42, (byte)m, (byte)nL, (byte)nH};
        byte[] end = new byte[]{27, 74, 16};
        byte mask = 1;
        byte[] perdata = new byte[w];

        int x;
        for(x = 0; x < w; ++x) {
            for(int y = 0; y < 8; ++y) {
                if ((b[y * w + x] & 16711680) >> 16 != 0) {
                    perdata[x] |= (byte)(mask << 7 - y);
                }
            }
        }

        for(x = 0; x < perdata.length; ++x) {
            perdata[x] = (byte)(~perdata[x]);
        }

        byte[] data = byteMerger(head, perdata);
        data = byteMerger(data, end);
        return data;
    }
    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
    public void printImgZPL(String address, String base64String, Integer width, Integer printCount, Integer x, Integer y, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    final Bitmap bitmapToPrint = convertGreyImg(decodeBase64ToBitmap(base64String));
                    ZPLPrinter printer = new ZPLPrinter(connection);
                    printer.addStart()
                            .downloadBitmap(width, "SAMPLE.GRF", bitmapToPrint)
                            .addBitmap(x, y, "SAMPLE.GRF")
                            .addPrintCount(printCount)
                            .addEnd();

                    statusZPL(printer, connection, result);
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }

            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }
    public void printImgCPCL(String address, String base64String, Integer width, Integer x, Integer y, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    CPCLPrinter printer = new CPCLPrinter(connection);
                    Bitmap bmp = decodeBase64ToBitmap(base64String);
                    printer.addCGraphics(x, y, width, bmp, AlgorithmType.Threshold).addPrint();
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {

            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }
    public void printImgTSPL(String address, String base64String, Integer width, Integer widthBmp, Integer height, Integer m, Integer n, Integer x, Integer y,
                             @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    TSPLPrinter printer = new TSPLPrinter(connection);
                    Bitmap bmp = decodeBase64ToBitmap(base64String);
                    printer.sizeMm(width, height)
                            .gapMm(m, n)
                            .cls()
                            .bitmapCompression(x, y, TSPLConst.BMP_MODE_OVERWRITE_C, widthBmp, bmp, AlgorithmType.Threshold)
                            .print(1);
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }
    public void statusZPL(ZPLPrinter printer, IDeviceConnection connection, @NonNull MethodChannel.Result result) {
        try {
            int type = connection.getConnectType();
            printer.printerStatus(status -> {
                String msg;
                switch (status) {
                    case 0:
                        msg = "STS_NORMAL";
                        result.success(msg);
                        break;
                    case 8:
                        msg = "STS_COVEROPEN";
                        result.success(msg);
                        break;
                    case 16:
                        msg = "STS_PAPEREMPTY";
                        result.success(msg);
                        break;
                    case 32:
                        msg = "STS_PRESS_FEED";
                        result.success(msg);
                        break;
                    case 64:
                        result.error(StatusPrinter.ERROR, "PRINT_FAIL", "");
                        break;
                    default:
                        msg = "UNKNOWN";
                        if (status > 0) {
                            result.success(msg);
                        } else if (status == -4) {
                            if (type == POSConnect.DEVICE_TYPE_ETHERNET || type == POSConnect.DEVICE_TYPE_BLUETOOTH) {
                                result.error(StatusPrinter.ERROR, "PRINT_FAIL", "");
                            } else {
                                result.success(msg);
                            }
                        } else {
                            result.error(StatusPrinter.ERROR, "PRINT_FAIL", "");
                        }
                        Log.e("STATUS PRINT", String.valueOf(status));
                        break;
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void setPrintSpeed(String address, Integer speed, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    ZPLPrinter printer = new ZPLPrinter(connection);
                    printer.setPrintSpeed(speed);
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void setPrintOrientation(String address, String orientation, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    ZPLPrinter printer = new ZPLPrinter(connection);
                    printer.setPrintOrientation(orientation);
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "FAIL", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void printerStatusZPL(String address, Integer timeout, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    ZPLPrinter printer = new ZPLPrinter(connection);
                    printer.printerStatus(timeout, i -> result.success(i));
                } else {
                    result.error(StatusPrinter.ERROR, "FAIL", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void printRawDataCPCL(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    CPCLPrinter printer = new CPCLPrinter(connection);
                    byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
                    printer.sendData(bytes).addPrint();
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void printRawDataTSPL(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    TSPLPrinter printer = new TSPLPrinter(connection);
                    byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
                    printer.sendData(bytes).print();
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }

    public void setPrintDensity(String address, Integer density, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                if (connection.isConnect()) {
                    ZPLPrinter printer = new ZPLPrinter(connection);
                    printer.setPrintDensity(density);
                    result.success("SUCCESS");
                } else {
                    result.error(StatusPrinter.ERROR, "DISCONNECT", "");
                }
            } else {
                result.error(StatusPrinter.ERROR, "GET_ID_FAIL", "");
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), "");
        }
    }
}
