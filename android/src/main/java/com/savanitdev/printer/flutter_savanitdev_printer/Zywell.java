package com.savanitdev.printer.flutter_savanitdev_printer;

import static android.content.Context.BIND_AUTO_CREATE;
import static zywell.posprinter.utils.PosPrinterDev.PortType.Ethernet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
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

public class Zywell {
    public static IMyBinder myZyWell;
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
            myZyWell = (IMyBinder) service;
            Log.e("ZyWell", "connect");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Zywell", "disconnect");
            myZyWell = null;
        }
    };

    // multiple connection
    private void AddPrinter(PosPrinterDev.PrinterInfo printer, @NonNull MethodChannel.Result result) {
        myZyWell.AddPrinter(printer, new TaskCallback() {
            @Override
            public void OnSucceed() {
                // Create a Map to hold the result data
//                Map<String, Object> response = new HashMap<>();
//                response.put("printerName", printer.printerName);
//                response.put("portInfo", printer.portInfo);
//                response.put("printerType", printer.printerType);
//                response.put("status", printer.status);

                // Send the success response to Flutter
                result.success("CONNECTED");
            }

            @Override
            public void OnFailed() {
                result.error("ERROR", "CONNECT_FAIL", "");
            }
        });
    }

    public void connectZyWell(String address, String portType, @NonNull MethodChannel.Result result) {
        try {
            if (!Objects.equals(address, "")) {
                boolean isConnected = findPrinterByName(address);
                Log.e("isConnected", String.valueOf(isConnected));
                Log.e("address", String.valueOf(address));
                String status = String.valueOf(myZyWell.GetPrinterStatus(address));
                Log.e("status", status);
                if (isConnected) {
                    result.success("CONNECTED");
                } else {
                    PosPrinterDev.PrinterInfo printer;
                    switch (portType) {
                        case "network":
                            printer = new PosPrinterDev.PrinterInfo(address, Ethernet, address);
                            AddPrinter(printer, result);
                            break;
                        case "bluetooth":
                            printer = new PosPrinterDev.PrinterInfo(address, PosPrinterDev.PortType.Bluetooth, address);
                            AddPrinter(printer, result);
                            break;
                        case "usb":
                            printer = new PosPrinterDev.PrinterInfo(address, PosPrinterDev.PortType.USB, address);
                            AddPrinter(printer, result);
                            printer.context = contextZyWell;
                            break;
                    }
                }
            } else {
                result.error("ERROR", "CONNECT_ADDRESS_FAIL_NULL", "");
            }
        } catch (Exception e) {
            result.error("ERROR", "ERROR_CONNECT", "");
        }
    }

    // Method to find a printer by its name
    public boolean findPrinterByName(String printerName) {
        for (int i = 0; i < myZyWell.GetPrinterInfoList().size(); i++) {
//            Log.e("printerName", printerName);
//            Log.e("GET GetPrinterInfoList", myZyWell.GetPrinterInfoList().get(i).printerName);
//            Log.e("GetPrinterInfoList ",
//                    String.valueOf(myZyWell.GetPrinterInfoList().get(i).printerName.equals(printerName.toString())));
            if (myZyWell.GetPrinterInfoList().get(i).printerName.equals(printerName.toString())) {

                return true;
            }
        }
        return false;
    }

    public void getPrinterStatusZyWell(String printerName, @NonNull MethodChannel.Result result) {
        try {
            String status = String.valueOf(myZyWell.GetPrinterStatus(printerName));
            result.success(status);
        } catch (Exception e) {
            result.error("ERROR", e.toString(), "");
        }
    }

    public void disconnectZyWell(String printerName, @NonNull MethodChannel.Result result) {
        try {
            if (printerName != null) {
                boolean isValidate = findPrinterByName(printerName);
                Log.e("isValidate", String.valueOf(isValidate));
                if (isValidate) {
                    myZyWell.RemovePrinter(printerName, new TaskCallback() {
                        @Override
                        public void OnSucceed() {
                            result.success("DISCONNECT");
                        }

                        @Override
                        public void OnFailed() {
                            result.error("ERROR", "REMOVE_FAIL", "");
                        }
                    });
                } else {
                    result.error("ERROR", "NO_PRINTER_IN_LIST", "");
                }
            } else {
                result.error("ERROR", "NOT_FOUND_NAME", "");
            }

        } catch (Exception e) {
            result.error("", "ERROR", e);
        }
    }

    public void removePrinter(String printerName, @NonNull MethodChannel.Result result) {
        try {
            if (printerName != null) {
                boolean isConnected = findPrinterByName(printerName);
                Log.e("isConnected", String.valueOf(isConnected));
                if (isConnected) {
                    myZyWell.RemovePrinter(printerName, new TaskCallback() {
                        @Override
                        public void OnSucceed() {
                        }

                        @Override
                        public void OnFailed() {
                            result.error("ERROR", "REMOVE_FAIL", "");
                        }
                    });
                } else {
                    result.error("ERROR", "NO_PRINTER_IN_LIST", "");
                }
            } else {
                result.error("ERROR", "NOT_FOUND_NAME", "");
            }

        } catch (Exception e) {
            result.error("", "ERROR", e);
        }
    }

    public void printRawZyWell(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
            myZyWell.SendDataToPrinter(address, new TaskCallback() {
                @Override
                public void OnSucceed() {

//                    disconnectZyWell(address, result);
                   result.success("PRINT_DONE");
                }

                @Override
                public void OnFailed() {
//                    disconnectZyWell(address, result);
                    result.error("ERROR", "PRINT_FAIL", "");
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
            Log.d("printRawZyWell", String.valueOf(e));
            disconnectZyWell(address, result);
        }

    }

    public void printImgZyWell(String address, String encode, boolean isCut, int width, int cutCount,
                               @NonNull MethodChannel.Result result) {

        final Bitmap bitmap = BitmapProcess.compressBmpByYourWidth(Xprinter.decodeBase64ToBitmap(encode), width);
        final Bitmap bitmapToPrint = Xprinter.convertGreyImg(bitmap);
        myZyWell.SendDataToPrinter(address, new TaskCallback() {
            @Override
            public void OnSucceed() {
//                disconnectZyWell(address, result);
                result.success("SEND_SUCCESS");
            }

            @Override
            public void OnFailed() {
//                disconnectZyWell(address, result);
                result.error("ERROR", "PRINT_FAIL", "");
            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list = new ArrayList<>();
                List<Bitmap> blist = new ArrayList<>();
                blist = BitmapProcess.cutBitmap(cutCount, bitmapToPrint);
                for (int i = 0; i < blist.size(); i++) {
                    list.add(DataForSendToPrinterPos80.printRasterBmp(
                            0, blist.get(i), BitmapToByteData.BmpType.Dithering,
                            BitmapToByteData.AlignType.Center, width));
                }
                if (isCut) {
                    list.add(
                            DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(
                                    0x42, 0x66));
                }
                return list;
            }
        });

    }
    public static Bitmap convertBytesToBitmap(byte[] uint8List) {
        if (uint8List == null || uint8List.length == 0) {
            return null; // Return null if the byte array is empty or null
        }
        return BitmapFactory.decodeByteArray(uint8List, 0, uint8List.length);
    }
}
