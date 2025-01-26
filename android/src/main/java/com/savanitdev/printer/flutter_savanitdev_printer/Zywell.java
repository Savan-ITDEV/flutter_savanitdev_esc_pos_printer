package com.savanitdev.printer.flutter_savanitdev_printer;

import static android.content.Context.BIND_AUTO_CREATE;

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

import com.savanitdev.printer.flutter_savanitdev_printer.utils.StatusPrinter;

import io.flutter.plugin.common.MethodChannel.Result;

public class Zywell {
    public static IMyBinder myBinder;
    Context context;
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
            myBinder.ConnectUsbPort(context.getApplicationContext(), address, new TaskCallback() {
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

    public void disconnect() {
        try {
            try {
                Thread.sleep(500);
                myBinder.DisconnetNetPort(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                    }

                    @Override
                    public void OnFailed() {
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
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
                        disconnect();
                        result.success(StatusPrinter.STS_NORMAL);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void OnFailed() {
                    disconnect();
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
            disconnect();
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
        }

    }

    public void printImgZyWell(String address, String encode, boolean isCut, int width, int cutCount,
                               @NonNull MethodChannel.Result result) {
        try {
            Bitmap bmp = Xprinter.decodeBase64ToBitmap(encode);
            final Bitmap bitmapToPrint = Xprinter.convertGreyImg(bmp);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    disconnect();
                    result.success(StatusPrinter.STS_NORMAL);
                }

                @Override
                public void OnFailed() {
                    disconnect();
                    result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    list.add(DataForSendToPrinterPos80.printRasterBmp(
                            0, bmp, BitmapToByteData.BmpType.Dithering,
                            BitmapToByteData.AlignType.Center, width));
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    if (isCut) {
                        list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42, 0x66));
                    }
                    return list;
                }
            });

        } catch (Exception e) {
            disconnect();
            result.error(StatusPrinter.ERROR, StatusPrinter.PRINT_FAIL, StatusPrinter.PRINT_FAIL);
        }
    }
}
