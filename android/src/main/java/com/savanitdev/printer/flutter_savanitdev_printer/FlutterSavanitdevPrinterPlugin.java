package com.savanitdev.printer.flutter_savanitdev_printer;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;

import net.posprinter.POSConnect;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterSavanitdevPrinterPlugin
 */
public class FlutterSavanitdevPrinterPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    Context context;
    Xprinter xprinter = new Xprinter();
    Zywell zywell = new Zywell();
    USBAdapter usbAdapter = new USBAdapter();

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion" -> {
                result.success("Android " + android.os.Build.VERSION.RELEASE);
            }
            //  ================>      Xprinter libray method        <================    //
            case "connectMultiXPrinter" -> {
                String address = call.argument("address");
                String type = call.argument("type");
                  if (address == null || type == null) {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                    return;
                }
                xprinter.connectMultiXPrinter(address, type, result);
            }
            case "disconnectXPrinter" -> {
                String address = call.argument("address");
                 if (address == null) {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                    return;
                }
                xprinter.disconnectXPrinter(address, result);
            }
            case "removeConnection" -> {
                String address = call.argument("address");
                xprinter.removeConnection(address,result);
            }
            case "printRawDataESC" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                boolean isDevicePOS = Boolean.TRUE.equals(call.argument("isDevicePOS"));
                 if (address == null || encode == null) {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                    return;
                }
                xprinter.printRawDataESC(address, encode,isDevicePOS, result);
            }
            case "printImgESCX" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                Integer width = call.argument("width");
                boolean isDevicePOS = Boolean.TRUE.equals(call.argument("isDevicePOS"));
                 if (address == null || encode == null || width == null ) {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                    return;
                }
                xprinter.printImgESCX(address, encode, isDevicePOS, width, result);
            }
            case "cutESCX" -> {
                String address = call.argument("address");
                xprinter.cutESCX(address, result);
            }
            case "pingDevice" -> {
                String address = call.argument("address");
                Integer timeout = call.argument("timeout");
                pingDevice(address, timeout, result);
            }
            case "startQuickDiscovery" -> {
                Integer timeout = call.argument("timeout");
                startQuickDiscovery(timeout, result);
            }  case "USBDiscovery" -> {
                USBDiscovery(result);
            }
            case "printImgZPL" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                Integer printCount = call.argument("printCount");
                Integer width = call.argument("width");
                Integer x = call.argument("x");
                Integer y = call.argument("y");
                xprinter.printImgZPL(address, encode, width, printCount, x, y, result);
            }
            case "printImgCPCL" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                Integer width = call.argument("width");
                Integer x = call.argument("x");
                Integer y = call.argument("y");
                xprinter.printImgCPCL(address, encode, width, x, y, result);
            }
            case "printImgTSPL" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                Integer width = call.argument("width");
                Integer widthBmp = call.argument("widthBmp");
                Integer height = call.argument("height");
                Integer m = call.argument("m");
                Integer n = call.argument("n");
                Integer x = call.argument("x");
                Integer y = call.argument("y");
                xprinter.printImgTSPL(address, encode, width, widthBmp, height, m, n, x, y, result);
            }
            case "setPrintSpeed" -> {
                String address = call.argument("address");
                Integer speed = call.argument("speed");
                xprinter.setPrintSpeed(address, speed, result);
            }
            case "setPrintOrientation" -> {
                String address = call.argument("address");
                String orientation = call.argument("orientation");
                xprinter.setPrintOrientation(address, orientation, result);
            }
            case "printRawDataCPCL" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                xprinter.printRawDataCPCL(address, encode, result);
            }
            case "printRawDataTSPL" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                xprinter.printRawDataTSPL(address, encode, result);
            }
            case "setPrintDensity" -> {
                String address = call.argument("address");
                Integer density = call.argument("density");
                xprinter.setPrintDensity(address, density, result);
            }
            case "printerStatusZPL" -> {
                String address = call.argument("address");
                Integer timeout = call.argument("timeout");
                xprinter.printerStatusZPL(address, timeout, result);
            }
            case "getUSBAddress" -> {
                Integer productId = call.argument("productId");
                Integer vendorId = call.argument("vendorId");
                getUSBAddress(productId,vendorId,result);
            }

            case "tryGetUsbPermission" ->{
                usbAdapter.tryGetUsbPermission(context);

            }
            //  ================>      ZyWell libray method        <================    //

            case "connectZyWell" -> {
                String address = call.argument("address");
                String type = call.argument("type");
                zywell.connectZyWell(address,type, result);
            }
            case "disconnectZyWell" -> {
                String address = call.argument("address");
                zywell.disconnectZyWell(result);
            }
            case "getPrinterStatusZyWell" -> {
                String address = call.argument("address");
                zywell.getPrinterStatusZyWell(address, result);
            }
            case "printRawZyWell" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                zywell.printRawZyWell(address,encode, result);
            }
            case "printImgZyWell" -> {
                String address = call.argument("address");
                String encode = call.argument("encode");
                boolean isCut = call.argument("isCut");
                int width = call.argument("width");
                int cutCount = call.argument("cutCount");
                zywell.printImgZyWell(address,encode,isCut,width,cutCount,result);
            }
            default -> result.notImplemented();
        }
    }
    public void USBDiscovery( @NonNull Result result) {
        try {
            // List to hold discovered printers
            List<Map<String, String>> printersArray = new ArrayList<>();
            var usbLists = POSConnect.getUsbDevice(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                usbLists.forEach((usb)->{
                    // Create a map to store printer information
                    Map<String, String> printerInfo = new HashMap<>();
                    printerInfo.put("address", usb.getDeviceName());
                    printerInfo.put("product", usb.getProductName());
                    printerInfo.put("vendorId", String.valueOf(usb.getVendorId()));
                    printerInfo.put("productId", String.valueOf(usb.getProductId()));
                    printerInfo.put("manufacturerName", String.valueOf(usb.getManufacturerName()));
                    // Add the printer info to the list
                    printersArray.add(printerInfo);
                });
                result.success(printersArray);
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(context, "statusXprinter.txt", String.valueOf(e));
            result.error("ERROR", e.toString(), "");
        }
    }

    public void getUSBAddress(Integer productId ,Integer vendorId, @NonNull Result result) {
        try {
            var usbLists = POSConnect.getUsbDevice(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if(usbLists.isEmpty()){
                    result.error("ERROR", "NOT_FOUND_PRINTER", "");
                }else{
                    List<String> address = new ArrayList<>();
                    usbLists.forEach((usb)->{
                        if(usb.getVendorId() == vendorId && usb.getProductId() == productId){
                            address.add(usb.getDeviceName());
                        }
                    });
                    if(address.isEmpty()){
                        result.error("ERROR", "NOT_FOUND_PRINTER", "");
                    }else{
                        result.success(address.get(0));
                    }

                }
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(context, "statusXprinter.txt", String.valueOf(e));
            result.error("ERROR", e.toString(), "");
        }
    }

    private void pingDevice(String address, int timeout, @NonNull Result result) {
        try {
            NetworkUtils.fastPingAndGetNetworkSpeed(address, timeout, result);
        } catch (Exception exe) {
            Log.d("TAG", "Exception--: " + exe);
            result.error("ERROR", exe.toString(), "");
        }
    }


    public void startQuickDiscovery(Integer timeout, @NonNull Result result) {
        new Thread(() -> {
            DatagramSocket socket = null;
            try {
                // Close any existing socket
                if (socket != null) {
                    socket.isClosed();
                }

                // Create a new DatagramSocket
                socket = new DatagramSocket(5001);
                socket.setSoTimeout(timeout); // Set the timeout for receiving data
                byte[] sendData = "ZY0001FIND".getBytes();

                // Create and send a broadcast UDP packet
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName("255.255.255.255"),
                        1460
                );
                socket.send(sendPacket);

                // Prepare buffer for receiving data
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // List to hold discovered printers
                List<Map<String, String>> printersArray = new ArrayList<>();
                boolean listening = true;

                while (listening) {
                    try {
                        socket.receive(receivePacket); // Wait for a response
                        String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Create a map to store printer information
                        Map<String, String> printerInfo = new HashMap<>();
                        printerInfo.put("ipAddress", receivePacket.getAddress().toString());
                        printerInfo.put("message", receivedString);

                        // Add the printer info to the list
                        printersArray.add(printerInfo);
                    } catch (SocketTimeoutException e) {
                        listening = false; // Stop listening on timeout
                    } catch (IOException e) {
                        Log.e("PrinterDiscovery", "Error receiving packet", e);
                        result.error("IO_EXCEPTION", e.toString(), null);
                    }
                }

                result.success(printersArray);

            } catch (IOException e) {
                Log.e("PrinterDiscovery", "Error during discovery", e);
                result.error("DISCOVERY_ERROR", e.toString(), null);
            } finally {
                // Close the socket
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }).start();
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_savanitdev_printer");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
        xprinter.initPrinter(context);
        zywell.initPrinterZyWell(context);

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
