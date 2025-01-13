package com.savanitdev.printer.flutter_savanitdev_printer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;
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
import zywell.posprinter.utils.BitmapProcess;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import io.flutter.plugin.common.MethodChannel;

public class Xprinter {
    private final Map<String, IDeviceConnection> connections = new HashMap<>();
    int rety = 0;
    int maxRety = 3;

    public void initPrinter(Context context) {
        POSConnect.init(context);
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
                checkInitConnection(address);
                IDeviceConnection connection = POSConnect.createDevice(type);
//                Log.d("TAG", " NEW connection : " + type);
                connections.put(address, connection);  // Store the connection with IP as key
                // Attempt to connect the device and use a callback listener
                connection.connect(address, (code, msg) -> connectListener(address, code,portType, result));
        } catch (Exception e) {
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
                    result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
                }
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR,StatusPrinter.CONNECT_ERROR, e.toString());
        }
    }
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void statusXprinter(String address, POSPrinter printer, IDeviceConnection connection, @NonNull MethodChannel.Result result) {
        int type = connection.getConnectType();

       try
       {
           printer.printerStatus(status -> {
               // Handle the received status here
               String msg;
               if(type == POSConnect.DEVICE_TYPE_USB){
                   checkInitConnection(address);
               }
               rety = 0;
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
                       result.error(StatusPrinter.ERROR, msg, StatusPrinter.STS_PAPEREMPTY);
                       break;
                   case 32:
                       msg = "STS_PRESS_FEED";
                       result.success(msg);
                       break;
                   case 64:
                       result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.STS_PRINTER_ERR);
                       break;
                   default:
                       msg = "STS_NORMAL";
                       if (status > 0) {
                           result.success(msg);
                       } else if (status == -4) {
                           if (type == POSConnect.DEVICE_TYPE_ETHERNET || type == POSConnect.DEVICE_TYPE_BLUETOOTH) {
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

    public void printImgESCX(String address, String base64String, Integer countCut, Integer width, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);

            if (connection.isConnect()) {
                POSPrinter printer = new POSPrinter(connection);
                Bitmap bmp = decodeBase64ToBitmap(base64String);
                final Bitmap bitmapToPrint = convertGreyImg(bmp);
                List<Bitmap> blist = new ArrayList<>();
                blist = BitmapProcess.cutBitmap(countCut, bitmapToPrint);
                for (int i = 0; i < blist.size(); i++) {
                    printer.printBitmap(blist.get(i), POSConst.ALIGNMENT_CENTER, width);
                }
                printer.cutHalfAndFeed(1);
                statusXprinter(address, printer, connection, result);

            } else {
                result.error(StatusPrinter.ERROR,  StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR,StatusPrinter.PRINT_FAIL ,e.toString());
        }
    }
    public void printRawDataESC(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            IDeviceConnection connection = connections.get(address);
            if (connection != null && connection.isConnect()) {
                POSPrinter printer = new POSPrinter(connection);
                byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
                printer.sendData(bytes);
                statusXprinter(address, printer, connection, result);
            } else {
                result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT, StatusPrinter.PRINTER_DISCONNECT);
            }
        } catch (Exception e) {
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
