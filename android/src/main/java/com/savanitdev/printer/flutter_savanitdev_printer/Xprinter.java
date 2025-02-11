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
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.LogPrinter;
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

import zywell.posprinter.utils.BitmapProcess;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import io.flutter.plugin.common.MethodChannel;
import zywell.posprinter.utils.BitmapToByteData;

public class Xprinter {
    private final Map<String, IDeviceConnection> connections = new HashMap<>();
    int rety = 0;
    int maxRety = 3;
    Context contextX;
    public void initPrinter(Context context) {
        POSConnect.init(context);
        contextX = context;
        Log.e("Xprinter", "Start init");
    }
    public void connectMultiXPrinter(String address, String portType, @NonNull MethodChannel.Result result) {
        try {
            int type = POSConnect.DEVICE_TYPE_ETHERNET;
            if (Objects.equals(portType, "usb")) {
                type = POSConnect.DEVICE_TYPE_USB;
            } else if (Objects.equals(portType, "bluetooth")) {
                type = POSConnect.DEVICE_TYPE_BLUETOOTH;
            } else if (Objects.equals(portType, "serial")) {
                type = POSConnect.DEVICE_TYPE_SERIAL;
            }
            // checkInitConnection(address);
            IDeviceConnection connection = POSConnect.createDevice(type);
//                Log.d("TAG", " NEW connection : " + type);
            connections.put(address, connection);  // Store the connection with IP as key
            // Attempt to connect the device and use a callback listener
            connection.connect(address, (code, msg) -> connectListener(address, code,portType, result));
        } catch (Exception e) {
//            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(e));
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e.toString());
        }
    }
    public void disconnectXPrinter(String address, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                removeConnection(address,result);
                result.success(StatusPrinter.DISCONNECT);
            } else {
                result.error(StatusPrinter.ERROR, StatusPrinter.GET_ID_FAIL, StatusPrinter.GET_ID_FAIL_DETAIL);
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL,e.toString());
        }
    }
    public void removeConnection(String address,@NonNull MethodChannel.Result result) {
        try
        {
            // Check if the connection exists for the given IP
            if (!connections.isEmpty() && connections.containsKey(address)) {
                // Retrieve and close the connection before removing it
                IDeviceConnection connection = connections.get(address);
                if (connection != null) {
                    connection.close();  // Close the connection safely
                }
                // Remove the connection from the map
                connections.remove(address);
//               Log.d("TAG", "Connection removed for IP: " + address);
            } else {
//               Log.d("TAG", "No connection found for IP: " + address);
                result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        }catch (Exception e){
//           Log.d("TAG", "catch removeConnection : " + e);
            result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, e.toString());
        }
    }
    public void checkInitConnection(String address) {
        if (!connections.isEmpty() && connections.containsKey(address)) {
            // Retrieve and close the connection before removing it
            IDeviceConnection connection = connections.get(address);
            if (connection != null) {
                connection.close();  // Close the connection safely
            }
            connections.remove(address);
//               Log.d("TAG", "Connection removed for IP: " + address);
        }
    }
    private void connectListener(String address, int code,String portType, @NonNull MethodChannel.Result result) {
        try {
            if (code == POSConnect.CONNECT_SUCCESS) {
                rety = 0;
                result.success(StatusPrinter.CONNECTED);
            } else {
                if(code == POSConnect.CONNECT_INTERRUPT || code == POSConnect.CONNECT_FAIL){
                    if (rety < maxRety) {
                        rety++;
//                         Log.d("TAG", "Retrying connection, attempt: " + rety);
                        connectMultiXPrinter(address, portType, result);
                    } else {
//                        Log.d("TAG", " Failed to connect after 3 retries : ");
                        rety = 0; // Reset retry counter
                        result.error(StatusPrinter.ERROR, StatusPrinter.RETRY_FAILED,  StatusPrinter.RETRY_FAILED3);
                    }
                }else{
//                    LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf("connectListener" + StatusPrinter.PRINTER_DISCONNECT));
                    result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                }
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(e));
            result.error(StatusPrinter.ERROR,StatusPrinter.CONNECT_ERROR, e.toString());
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
//                Log.e("TAG", "================================== > statusXprinter :" + status);
//                LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(status));
                // Handle the received status here
                String msg = "";
//                try {
//                    Thread.sleep(500);
//                     checkInitConnection(address);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
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
//                                LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(status));
                                result.error(StatusPrinter.ERROR,  StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                            } else {
                                result.success(msg);
                            }
                        } else {
//                            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(StatusPrinter.PRINTER_DISCONNECT));
                            result.error(StatusPrinter.ERROR,  StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                        }
                        break;
                }
            });
        } catch (Exception e){
//            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(e));
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e.toString());
        }
    }
    public void statusPrint(POSPrinter printer, @NonNull MethodChannel.Result result){
        printer.printerCheck(POSConst.STS_TYPE_PRINT, 5000, new IDataCallback() {
            @Override
            public void receive(byte[] data) {
                if (data == null || data.length == 0) {
                    Log.e("PrinterCheck", "No response from printer");
//                    LogPrinter.writeTextFile(contextX,"statusXprinter.txt", "No response from printer");
                    result.error(StatusPrinter.ERROR,StatusPrinter.PRINT_FAIL ,"No response from printer");
                    return;
                }
                // Example: Bit 5 (0x20) in the first byte indicates "printing busy"
                boolean isPrinting = (data[0] & 0x20) != 0; // Replace 0x20 with your printer's flag
                if (isPrinting) {
//                    LogPrinter.writeTextFile(contextX,"statusXprinter.txt", "Printer is still busy...");
                    result.error(StatusPrinter.ERROR,StatusPrinter.PRINT_FAIL ,"Printer is still busy...");
                    Log.d("PrinterCheck", "Printer is still busy...");
                } else {
                    Log.d("PrinterCheck", "Print job likely completed successfully!");
                    result.success(StatusPrinter.STS_NORMAL);
                }
            }
        });
    }
    public void printImgESCX(String address, String base64String, boolean isDevicePOS, Integer width, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection.isConnect()) {
                POSPrinter printer = new POSPrinter(connection);
                Bitmap bmp = decodeBase64ToBitmap(base64String);
                final Bitmap bitmapToPrint = convertGreyImg(bmp);
//                List<Bitmap> blist = new ArrayList<>();
//                blist = BitmapProcess.cutBitmap(countCut, bitmapToPrint);
//                for (int i = 0; i < blist.size(); i++) {
//                    printer.printBitmap(blist.get(i), POSConst.ALIGNMENT_CENTER, width);
//                }
                printer.initializePrinter().printBitmap(bitmapToPrint,POSConst.ALIGNMENT_CENTER,width).cutHalfAndFeed(0);
                // Thread.sleep(500);
                statusXprinter(isDevicePOS,address, printer, connection, result);
            } else {
//                LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(StatusPrinter.PRINT_FAIL));
                result.error(StatusPrinter.ERROR,  StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(e));
            result.error(StatusPrinter.ERROR,StatusPrinter.PRINT_FAIL ,e.toString());
        }
    }

    public void printRawDataESC(String address, String encode,boolean isDevicePOS, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null && connection.isConnect()) {
                POSPrinter printer = new POSPrinter(connection);
                byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
                System.out.println("Sent of size: " + bytes.length + " bytes");
                printer.initializePrinter().sendData(bytes);
                // Thread.sleep(500);
                statusXprinter(isDevicePOS,address, printer, connection, result);
            } else {
//                LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(StatusPrinter.PRINT_FAIL));
                result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(contextX, "statusXprinter.txt", String.valueOf(e));
            result.error(StatusPrinter.ERROR,StatusPrinter.PRINT_FAIL ,e.toString());
        }
    }
    public void cutESCX(String address, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null && connection.isConnect()) {
                POSPrinter printer = new POSPrinter(connection);
                printer.cutHalfAndFeed(1);
                result.success(StatusPrinter.STS_NORMAL);
            } else {
                result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, e.toString(), e.toString());
        }
    }
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
                        msg = "Printer error";
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