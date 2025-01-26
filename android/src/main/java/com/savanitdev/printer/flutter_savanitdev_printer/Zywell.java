package com.savanitdev.printer.flutter_savanitdev_printer;

import static android.content.Context.BIND_AUTO_CREATE;
import static zywell.posprinter.utils.PosPrinterDev.PortType.Ethernet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;
import zywell.posprinter.posprinterface.IMyBinder;
import zywell.posprinter.posprinterface.ProcessData;
import zywell.posprinter.posprinterface.TaskCallback;
import zywell.posprinter.service.PosprinterService;
import zywell.posprinter.utils.BitmapProcess;
import zywell.posprinter.utils.BitmapToByteData;
import zywell.posprinter.utils.DataForSendToPrinterPos80;
import zywell.posprinter.utils.PosPrinterDev;

import android.graphics.BitmapFactory;

import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;

import io.flutter.plugin.common.MethodChannel.Result;

public class Zywell {
    List<byte[]> setPrinter = new ArrayList<>();
    private List<String> usbList, usblist;
    private BluetoothAdapter bluetoothAdapter;
    private Result pendingResult;

    public static IMyBinder myBinder;
    public static boolean ISCONNECT = false;
    public static String address = "";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
    UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    Context context;
    private Activity activity;
    private ActivityPluginBinding activityBinding;
    Context contextZyWell;

    public void initPrinterZyWell(Context context) {
        Intent intent = new Intent(context, PosprinterService.class);
        context.bindService(intent, mSerconnection, BIND_AUTO_CREATE);
        contextZyWell = context;
        Log.e("Zywell", "Start init Zywell");
    }

    ServiceConnection mSerconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (IMyBinder) service;
            Log.e("ZyWell", "connect");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Zywell", "disconnect");
            myBinder = null;
        }
    };

    public void connectZyWell(String address, String portType, @NonNull MethodChannel.Result result) {
        try {
            switch (portType) {
                case "network":
                    connectNet(address, result);
                    break;
                case "bluetooth":
                    connectBLE(address, result);
                    break;
                case "usb":
                    connectUSB(address, result);
                    break;
            }
        } catch (Exception e) {
            result.error("ERROR", "ERROR_CONNECT", "");
        }
    }
    private void connectBLE(String address, @NonNull Result result) {
        try {
            myBinder.ConnectBtPort(address, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    result.success(StatusPrinter.CONNECTED);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
        }
    }
    private void connectNet(String address, @NonNull Result result) {
        try {
            myBinder.ConnectNetPort(address, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    result.success(StatusPrinter.CONNECTED);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
        }
    }
    private void connectUSB(String address, @NonNull Result result) {
        try {
            myBinder.ConnectUsbPort(context.getApplicationContext(),address, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    result.success(StatusPrinter.CONNECTED);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "CONNECT_FAIL");
        }
    }
    public void getPrinterStatusZyWell(String printerName, @NonNull MethodChannel.Result result) {
        try {
            String status = String.valueOf(myBinder.GetPrinterStatus(printerName));
            result.success(status);
        } catch (Exception e) {
            result.error("ERROR", e.toString(), "");
        }
    }

    public void disconnectZyWell(@NonNull MethodChannel.Result result) {
        try {
            myBinder.DisconnectCurrentPort(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    result.success(StatusPrinter.DISCONNECT);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL,  StatusPrinter.DISCONNECT_FAIL);
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL,  StatusPrinter.DISCONNECT_FAIL);
        }
    }

    public void printRawZyWell(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
            myBinder.SendDataToPrinter(address, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    result.success(StatusPrinter.STS_NORMAL);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(bytes);
                    return list;
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
        }

    }
    public void printImgZyWell(String address, String encode, boolean isCut, int width, int cutCount,
                               @NonNull MethodChannel.Result result) {
        myBinder.SendDataToPrinter(address, new TaskCallback() {
            @Override
            public void OnSucceed() {
                result.success(StatusPrinter.STS_NORMAL);
            }
            @Override
            public void OnFailed() {
                result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                final Bitmap bitmap = BitmapProcess.compressBmpByYourWidth(Xprinter.decodeBase64ToBitmap(encode), width);
                final Bitmap bitmapToPrint = Xprinter.convertGreyImg(bitmap);
                List<byte[]> list = new ArrayList<>();
                list.add(DataForSendToPrinterPos80.printRasterBmp(
                        0, bitmapToPrint, BitmapToByteData.BmpType.Dithering,
                        BitmapToByteData.AlignType.Center, width));
                if(isCut){
                    DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42, 0x66);
                }
                return list;
            }
        });
    }
}
