package com.savanitdev.printer.flutter_savanitdev_printer;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.savanitdev.printer.flutter_savanitdev_printer.Xprinter.convertGreyImg;
import static com.savanitdev.printer.flutter_savanitdev_printer.Xprinter.decodeBase64ToBitmap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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

import com.savanitdev.printer.flutter_savanitdev_printer.utils.ResultStatus;
import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;

import net.posprinter.POSConst;
import net.posprinter.utils.StringUtils;

import io.flutter.plugin.common.MethodChannel.Result;

public class Zywell {
    public static IMyBinder myBinder;
    Context contextZyWell;
    ResultStatus resultStatus = new ResultStatus();
    Boolean isConnect = false;
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
                    result.success(true);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, false);
                }
            });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e);
        }
    }
    private void connectNet(String address, @NonNull Result result) {
        try {
            myBinder.ConnectNetPort(address, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {

                    result.success(true);
                }
                @Override
                public void OnFailed() {
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, false);
                }
            });
        } catch (Exception e) {

            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e);
        }
    }

    private void connectUSB(String address, @NonNull Result result) {
        try {
            String usbAddress = address.trim();
//if(isConnect){
//    result.success(true);
//    return;
//}
           myBinder.ConnectUsbPort(contextZyWell, usbAddress, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    isConnect = true;   
                    result.success(true);
                }

                @Override
                public void OnFailed() {
                    isConnect = false;
                    result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, false);
                }
            });
        } catch (Exception e) {
            isConnect = false;
            result.error(StatusPrinter.ERROR, StatusPrinter.CONNECT_ERROR, e);
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

    public void disconnect(@NonNull MethodChannel.Result result) {
        try {
                Thread.sleep(500);
                myBinder.DisconnetNetPort(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                        resultStatus.setResult(result,true);
                    }

                    @Override
                    public void OnFailed() {
                        result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL,false);
                    }
                });
        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL,e);
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
                    result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL, StatusPrinter.DISCONNECT_FAIL);
                }
            });


        } catch (Exception e) {
            result.error(StatusPrinter.ERROR, StatusPrinter.DISCONNECT_FAIL, StatusPrinter.DISCONNECT_FAIL);
        }
    }

    public void printRawZyWell(String address, String encode, @NonNull MethodChannel.Result result) {
        try {
            byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
            myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    try {
                        Thread.sleep(1000);
                        disconnect(result);
                        result.success(StatusPrinter.STS_NORMAL);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void OnFailed() {
                    disconnect(result);
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
            disconnect(result);
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
        }

    }

    public void printImgZyWell(String iniCommand,String cutterCommands,String img,String encode, boolean isDisconnect, boolean isCut, int width,
                               @NonNull MethodChannel.Result result) {
        try {
           
            myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    if (isDisconnect) {
                        disconnect(result);
                    }
                }
                @Override
                public void OnFailed() {
                   disconnect(result);
//                 result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL,false);
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {

                   
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    if (!iniCommand.isEmpty()) {
                        byte[] bytes = Base64.decode(iniCommand, Base64.DEFAULT);
                        list.add(bytes);
                    }
                    list.add(DataForSendToPrinterPos80.selectAlignment(1));
                    if (!img.isEmpty()) {
                        Bitmap bmp = decodeBase64ToBitmap(img);
                        final Bitmap bitmapToPrint = convertGreyImg(bmp);
                        List<Bitmap> blist= new ArrayList<>();
                        blist = BitmapProcess.cutBitmap(150,bitmapToPrint);
                        for (int i= 0 ;i<blist.size();i++){
                            list.add(DataForSendToPrinterPos80.printRasterBmp(0,blist.get(i), BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Center,width));
                        }
                    }
                    if (!encode.isEmpty()) {
                        byte[] encodeBytes = Base64.decode(encode, Base64.DEFAULT);
                        list.add(encodeBytes);
                    }

                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    if (isCut && cutterCommands.isEmpty()) {
                        list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
                    }
                    if (isCut && !cutterCommands.isEmpty()) {
                        byte[] endBytes = Base64.decode(cutterCommands, Base64.DEFAULT);
                        list.add(endBytes);
                    }
                    return list;
                }
            });

        } catch (Exception e) {
          
//            disconnect(result);
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, e);
        }
    }
}
