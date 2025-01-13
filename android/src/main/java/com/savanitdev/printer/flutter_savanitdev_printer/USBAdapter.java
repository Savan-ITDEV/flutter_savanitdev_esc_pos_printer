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

public class USBAdapter {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    UsbManager mUsbManager;

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
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
    public void tryGetUsbPermission(Context context) {
        Log.d("USB ","tryGetUsbPermission");
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionActionReceiver, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), FLAG_IMMUTABLE);
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {

            if (mUsbManager.hasPermission(usbDevice)) {
            } else {
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }
    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        // now follow line will NOT show: User has not given permission to device
        // UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        // add your operation code here
    }
}
