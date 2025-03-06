package com.savanitdev.printer.flutter_savanitdev_printer;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.savanitdev.printer.flutter_savanitdev_printer.utils.ResultStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.flutter.plugin.common.MethodChannel;

public class BluetoothAdapters {
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    static ResultStatus resultStatus = new ResultStatus();
    static int rety = 0;
    private static final int PERMISSION_REQUEST_CODE = 1024;

    public static void openBluetoothSettings(Context context, @NonNull MethodChannel.Result result) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
        Toast.makeText(context, "กรุณาเปิดใช้งานการอนุญาตบลูทูธก่อน!", Toast.LENGTH_LONG).show();
        resultStatus.setResultMethod(result, new ArrayList<>());
    }
    public static void reqBlePermission(BluetoothAdapter bluetoothAdapter, Activity activity, @NonNull MethodChannel.Result result) {
       try{
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
            }, PERMISSION_REQUEST_CODE);
        }
        bluetoothAdapter.enable();
        enableBluetooth(activity, bluetoothAdapter, result);
       } catch(Exception e) {
        Log.e("error", e.toString());
//           resultStatus.setResultErrorMethod(result, e.toString());
           resultStatus.setResultMethod(result, new ArrayList<>());
//           openBluetoothSettings(activity);
       }
    }

    @SuppressLint("MissingPermission")
    public static void enableBluetooth(Activity activity, BluetoothAdapter bluetoothAdapter, @NonNull MethodChannel.Result result) {
      try {
          if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
              bluetoothAdapter.enable();
              Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
              activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
          } else {
              bluetoothScan(bluetoothAdapter,result);
          }
      } catch (Exception e) {
          Log.d("TAG", "Exception--: " + e);
          resultStatus.setResultMethod(result, new ArrayList<>());

      }
    }

    static void bluetoothDiscovery(BluetoothAdapter bluetoothAdapter, Activity activity, @NonNull MethodChannel.Result result) {
        try {
            if (!bluetoothAdapter.isEnabled()) {
                reqBlePermission(bluetoothAdapter, activity, result);
            } else {
                reqBlePermission(bluetoothAdapter, activity, result);
//                bluetoothScan(bluetoothAdapter, result);
//                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    reqBlePermission(bluetoothAdapter, activity, result);
//                } else {
//                    bluetoothScan(bluetoothAdapter, result);
//                }
            }
        } catch (Exception e) {
            Log.d("TAG", "Exception--: " + e);
            resultStatus.setResultMethod(result, new ArrayList<>());
        }
    }

    @SuppressLint("MissingPermission")
    static void bluetoothScan(BluetoothAdapter bluetoothAdapter,@NonNull MethodChannel.Result result) {
        try {
            if (!bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.startDiscovery();
                Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();
                List<Map<String, String>> printersArray = new ArrayList<>();
                for (BluetoothDevice bluetoothDevice : device) {
                    Map<String, String> printerInfo = new HashMap<>();
                    printerInfo.put("address", bluetoothDevice.getAddress());
                    printerInfo.put("name", bluetoothDevice.getName());
                    printersArray.add(printerInfo);
                }
                if (printersArray.isEmpty()) {
                    if (rety < 3) {
                        rety++;
                        bluetoothScan(bluetoothAdapter,result);
                    } else {
                        rety = 0;
                    }
                } else {
                    resultStatus.setResultMethod(result, printersArray);
                }
            }
        } catch (Exception e) {
            resultStatus.setResultMethod(result, new ArrayList<>());
        }
    }
}