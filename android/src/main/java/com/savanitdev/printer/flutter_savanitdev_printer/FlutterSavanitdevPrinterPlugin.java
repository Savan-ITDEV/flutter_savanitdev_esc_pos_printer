package com.savanitdev.printer.flutter_savanitdev_printer;


import static com.savanitdev.printer.flutter_savanitdev_printer.BluetoothAdapters.bluetoothDiscovery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.DeviceReceiver;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.ResultStatus;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;

import net.posprinter.POSConnect;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterSavanitdevPrinterPlugin
 */
public class FlutterSavanitdevPrinterPlugin implements  FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    private MethodChannel channel;
    Context context;
    Xprinter xprinter = new Xprinter();
    Zywell zywell = new Zywell();
    USBAdapter usbAdapter = new USBAdapter();
    private List<String> usbList,usblist;
    private Activity activity;
    private BluetoothAdapter bluetoothAdapter;
    private MethodChannel.Result pendingResult;
    private DeviceReceiver BtReciever;
    private ActivityPluginBinding activityBinding;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
    static ResultStatus resultStatus = new ResultStatus();
    private static final int PERMISSION_REQUEST_CODE = 1024;
    ExecutorService executor;
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            switch (call.method) {
                case "getPlatformVersion" -> {
                    result.success("Android " + Build.VERSION.RELEASE);
                }
                //  ================>      Xprinter function species printer        <================    //
                case "connect" -> {
                    String address = call.argument("address");
                    boolean isCloseConnection = Boolean.TRUE.equals(call.argument("isCloseConnection"));
                    String type = call.argument("type");
                    if (address == null || type == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    xprinter.connect(address, type, isCloseConnection, result);
                }

                case "connectZyWell" -> {
                    String address = call.argument("address");
                    String type = call.argument("type");
                    if (address == null || type == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    zywell.connectZyWell(address, type, result);
                }

                case "disconnectZyWell" -> {
                    String address = call.argument("address");
                    if (address == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    zywell.disconnect(result);
                } case "printImgZyWell" -> {
                    String iniCommand = call.argument("iniCommand");
                    String cutterCommands = call.argument("cutterCommands");
                    String img = call.argument("img");
                    Integer width = call.argument("width");
                    String encode = call.argument("encode");
                    boolean isCut = Boolean.TRUE.equals(call.argument("isCut"));
                    boolean isDisconnect = Boolean.TRUE.equals(call.argument("isDisconnect"));
                    if (iniCommand == null || cutterCommands == null || img == null || encode == null || width == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null parameter!");
                        return;
                    }
                    zywell.printImgZyWell(iniCommand,cutterCommands,img,encode,isDisconnect, isCut, width, result);

                }  case "disconnect" -> {
                    String address = call.argument("address");
                    if (address == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    xprinter.disconnect(address, result);
                }
                case "printCommand" -> {
                    String address = call.argument("address");
                    String iniCommand = call.argument("iniCommand");
                    String cutterCommands = call.argument("cutterCommands");
                    String img = call.argument("img");
                    Integer width = call.argument("width");
                    String encode = call.argument("encode");
                    boolean isCut = Boolean.TRUE.equals(call.argument("isCut"));
                    boolean isDelay = Boolean.TRUE.equals(call.argument("isDelay"));
                    boolean isDisconnect = Boolean.TRUE.equals(call.argument("isDisconnect"));
                    boolean isDevicePOS = Boolean.TRUE.equals(call.argument("isDevicePOS"));
                    if (address == null || iniCommand == null || cutterCommands == null || img == null || encode == null || width == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null parameter");
                        return;
                    }
                    xprinter.print(address, iniCommand, cutterCommands, encode, img, isCut, isDisconnect, isDevicePOS, isDelay, width, result);
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
                    xprinter.removeConnection(address, result);
                }
                case "printRawDataESC" -> {
                    String address = call.argument("address");
                    String encode = call.argument("encode");
                    boolean isDevicePOS = Boolean.TRUE.equals(call.argument("isDevicePOS"));
                    if (address == null || encode == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    xprinter.printRawDataESC(address, encode, isDevicePOS, result);
                }
                case "printImgESCX" -> {
                    String address = call.argument("address");
                    String encode = call.argument("encode");
                    Integer width = call.argument("width");
                    boolean isDevicePOS = Boolean.TRUE.equals(call.argument("isDevicePOS"));
                    if (address == null || encode == null || width == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    xprinter.printImgESCX(address, encode, isDevicePOS, width, result);
                }
                case "cutESCX" -> {
                    String address = call.argument("address");
                    xprinter.cutESCX(address, result);
                }

                case "USBDiscovery" -> {
                    USBDiscovery(result);
                }
                case "getUSBAddress" -> {
                    Integer productId = call.argument("productId");
                    Integer vendorId = call.argument("vendorId");
                    USBAdapter.getUSBAddress(context, productId, vendorId, result);
                }

                case "tryGetUsbPermission" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        USBAdapter.tryGetUsbPermission(context);
                    }
                }
                case "openBluetoothSettings" -> {
                    if (!hasBluetoothScan()) {
                        BluetoothAdapters.openBluetoothSettings(activity, result);
                    } else {
                        result.success(new ArrayList<>());
                    }
                }
                case "discovery" -> {
                    String type = call.argument("type");
                    Integer timeout = call.argument("timeout");
                    if (type == null || timeout == null) {
                        result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, "Printer get null");
                        return;
                    }
                    discovery(type, timeout, result);
                }

                default -> result.notImplemented();
            }
        });
    }
    public boolean hasBluetoothScan() {
        boolean hasBluetoothScan = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        boolean hasBluetoothConnect = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        if (hasBluetoothScan || hasBluetoothConnect) {
            return true;
        }else{
            return false;
        }
    }
    public void discovery(String type, Integer timeout, @NonNull Result result) {
        if(Objects.equals(type, "usb")){
            USBAdapter.usbDiscovery(context,result);
        }else if(Objects.equals(type, "bluetooth")){
            pendingResult = result;
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(BtReciever, filterStart);
            context.registerReceiver(BtReciever, filterEnd);

            if (bluetoothAdapter == null) {
                result.error("BLUETOOTH_UNAVAILABLE", "Bluetooth is not available on this device", null);
                return;
            }
            if (!hasBluetoothScan()) {
                BluetoothAdapters.reqBlePermission(bluetoothAdapter, activity,result);
                return;
            }
            BluetoothAdapters.bluetoothDiscovery( bluetoothAdapter, activity,result);
        }else{
            NetworkUtils.netDiscovery(timeout,result);
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
            result.error("ERROR", e.toString(), "");
        }
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

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        try{
            activityBinding = binding;
            activityBinding.getActivity();
            activity = binding.getActivity();
            // Register for activity result
            binding.addActivityResultListener(
                    (requestCode, resultCode, data) -> {
                        if (pendingResult != null){
                            if(resultCode == Activity.RESULT_OK) {
                                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                bluetoothDiscovery(bluetoothAdapter, activity, pendingResult);
                                pendingResult = null;
                                return true;
                            }
                            pendingResult.success(new ArrayList<>());
                            return false;
                        }else{
                            return false;
                        }
                    }
            );
        }catch (Exception e){
            if(pendingResult != null){
                pendingResult.error("ERROR", e.toString(), "");
                pendingResult = null;
            }
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
    private void getBondedDevices(Result result) {
        List<Map<String, Object>> list = new ArrayList<>();
        result.success(list);
    }
    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_COARSE_LOCATION_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getBondedDevices(pendingResult);
            } else {
                pendingResult.error("no_permissions", "this plugin requires location permissions for scanning", null);
                pendingResult = null;
            }
            return true;
        }
        return false;
    }

}
