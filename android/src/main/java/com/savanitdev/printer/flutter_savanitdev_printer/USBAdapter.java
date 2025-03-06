package com.savanitdev.printer.flutter_savanitdev_printer;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.savanitdev.printer.flutter_savanitdev_printer.utils.ResultStatus;

import net.posprinter.POSConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class USBAdapter {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    static UsbManager mUsbManager;
    static ResultStatus resultStatus = new ResultStatus();
    public static void usbDiscovery(Context context,@NonNull MethodChannel.Result result) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                tryGetUsbPermission(context);
            }
            // List to hold discovered printers
            List<Map<String, String>> printersArray = new ArrayList<>();
            var usbLists = POSConnect.getUsbDevice(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                usbLists.forEach((usb)->{
                    // Create a map to store printer information
                    Map<String, String> printerInfo = new HashMap<>();
                    printerInfo.put("address", usb.getDeviceName());
                    printerInfo.put("name", usb.getProductName());
                    printerInfo.put("product", usb.getProductName());
                    printerInfo.put("vendorId", String.valueOf(usb.getVendorId()));
                    printerInfo.put("productId", String.valueOf(usb.getProductId()));
                    printerInfo.put("manufacturerName", String.valueOf(usb.getManufacturerName()));
                    // Add the printer info to the list
                    printersArray.add(printerInfo);
                });
                resultStatus.setResultMethod(result,printersArray);

            }
        } catch (Exception e) {
            result.error("ERROR", e.toString(), "");
        }
    }
    public static void getUSBAddress(Context context, Integer productId, Integer vendorId, @NonNull MethodChannel.Result result) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                tryGetUsbPermission(context);
            }
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
                        resultStatus.setResultMethod(result,address.get(0));
                    }

                }
            }
        } catch (Exception e) {
//            LogPrinter.writeTextFile(context, "statusXprinter.txt", String.valueOf(e));
            result.error("ERROR", e.toString(), "");
        }
    }

    private static final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            doYourOpenUsbDevice(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void tryGetUsbPermission(Context context) {
        Log.d("USB ","tryGetUsbPermission");
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), FLAG_IMMUTABLE);
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            if (mUsbManager.hasPermission(usbDevice)) {

            } else {
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);

            }
        }
    }
    private static void doYourOpenUsbDevice(UsbDevice usbDevice) {
        // now follow line will NOT show: User has not given permission to device
        // UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        // add your operation code here
    }
}
