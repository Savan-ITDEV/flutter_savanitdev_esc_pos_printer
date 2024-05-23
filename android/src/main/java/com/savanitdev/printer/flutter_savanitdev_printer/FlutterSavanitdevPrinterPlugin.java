package com.savanitdev.printer.flutter_savanitdev_printer;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Context.BIND_AUTO_CREATE;

import static net.posprinter.utils.BitmapToByteData.grayPixle;
import static net.posprinter.utils.BitmapToByteData.resizeImage;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;
import net.posprinter.utils.StringUtils;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** FlutterSavanitdevPrinterPlugin */
public class FlutterSavanitdevPrinterPlugin implements  FlutterPlugin, ActivityAware, MethodCallHandler, RequestPermissionsResultListener  {
  private MethodChannel channel;
  List<byte[]> setPrinter = new ArrayList<>();
  private List<String> usbList,usblist;
  private BluetoothAdapter bluetoothAdapter;
  private Result pendingResult;
  private DeviceReceiver BtReciever;
  public static IMyBinder myBinder;
  public static boolean ISCONNECT = false;
  public static String address = "";
  private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
  UsbManager mUsbManager;
  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
  Context context;
  private Activity activity;
  private ActivityPluginBinding activityBinding;
  ServiceConnection mSerconnection= new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      myBinder= (IMyBinder) service;
      Log.e("myBinder","connect");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      Log.e("myBinder","disconnect");
      Toast toast = Toast.makeText(context, "disconnect", Toast.LENGTH_SHORT);
      toast.show();
    }
  };
  public void onCreate() {
    Intent intent =new Intent(context, PosprinterService.class);
    context.bindService(intent,mSerconnection,BIND_AUTO_CREATE);


  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_savanitdev_printer");
    channel.setMethodCallHandler(this);
    context = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    if (call.method.equals("getPlatformVersion")) {
      onCreate();
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }
    else if(call.method.equals("onCreate"))
    {
      onCreate();
    }else if(call.method.equals("connectNet"))
    {
      String ip = call.arguments();
      connectNet(ip,result);
    }
    else if(call.method.equals("connectBLE"))
    {
      String macAddress = call.arguments();
      connectBLE(macAddress,result);
    }

    else if(call.method.equals("findAvailableDevice"))
    {
      findAvailableDevice(result);
    }
    else if(call.method.equals("checkStatus"))
    {
      checkStatus(result);
    }
    else if(call.method.equals("image64BaseBLE"))
    {
      String base64String = call.argument("base64String");
      int width = call.argument("width");
      int isBLE = call.argument("isBLE");
      printBitmap(base64String,width,isBLE,result);
    }
    else if(call.method.equals("printBLEImgAndSet"))
    {
        String base64String = call.argument("base64String");
        int width = call.argument("width");
        int isCut = call.argument("isCut");
        downLoadBmp(base64String,width,isCut,result);
    }
    else if(call.method.equals("printBLEinPrinter"))
    {
        int isCut = call.arguments();
        printFlashBmp(isCut,result);
    }


    else if(call.method.equals("printLangPrinter"))
    {
      printLangPrinter(result);
    }
    else if(call.method.equals("setLang"))
    {
      String codepage = call.arguments();
      setLang(codepage,result);
    }
    else if(call.method.equals("cancelChinese"))
    {
      cancelChinese();
    }
    else if(call.method.equals("getLangModel"))
    {
      getLangModel(result);
    }
    else if(call.method.equals("disConnect"))
    {
      disConnect(result);
    }
    else if(call.method.equals("tryGetUsbPermission"))
    {
      tryGetUsbPermission();
    }
    else if(call.method.equals("reqBlePermission"))
    {
      reqBlePermission(result);
    }
    else if(call.method.equals("printRawData"))
    {
      final String encode = call.argument("encode");
      printRawData("",encode,result);
    }
    else if(call.method.equals("connectUSB"))
    {
      String usbAddress = call.arguments();
      tryGetUsbPermission();
      connectUSB(usbAddress,result);
    }
    else if(call.method.equals("getUSB"))
    {
      tryGetUsbPermission();
      getUSB(result);
    }

    else {
      result.notImplemented();
    }
  }

    public void printFlashBmp(int isCut,@NonNull Result result) {
        
    }


  public static byte[] printBmpInFLASH(int i, int i2, int i3) {
       return new byte[]{64};
  }

 public void downLoadBmp(String base64String,int w1,int isCut ,@NonNull Result result) {
 
 }

 public void clearFlashBmp() {
       
 }

 public static byte[] downloadLogo(Bitmap bitmap, int i) {
         byte[] bArr = new byte[((1 * 1) + 32)];
        return bArr;
    
 }
  private void connectUSB(String usbAddress,@NonNull Result result){
    if (usbAddress!=null){
      myBinder.ConnectUsbPort(context.getApplicationContext(),usbAddress, new TaskCallback() {
        @Override
        public void OnSucceed() {
          ISCONNECT = true;
          result.success(Boolean.toString(ISCONNECT));
        }

        @Override
        public void OnFailed() {
          ISCONNECT = false;
          result.error("",Boolean.toString(ISCONNECT),"");

        }
      });


    }else {
      result.error("","No usbAddress","");
    }
  }

  private void getUSB(@NonNull Result result){
    try {
      usbList= PosPrinterDev.GetUsbPathNames(context);
      if (usbList==null){
        usbList=new ArrayList<>();
      }
      usblist=usbList;
      result.success(usblist.toString());
    }
    catch (Exception exe) {
      Log.d("TAG", "Exception--: " + exe);
      result.error("","get USB error","");
    }
  }
  public void checkStatus(@NonNull Result result){
    if(ISCONNECT ==true){
      result.success("connect");
    }else{
      disConnect(result);
      result.error("","disconnect","");

    }
  }
  private void findAvailableDevice(@NonNull Result result){
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
          reqBlePermission(result);
          return;
        }
      }
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (!bluetoothAdapter.isEnabled()) {
        Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();
        Log.d("TAG", "findAvalibleDevice: "+device.size());
      }else {

        if (!bluetoothAdapter.isDiscovering()) {
          bluetoothAdapter.startDiscovery();
        }

        IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(BtReciever, filterStart);
        context.registerReceiver(BtReciever, filterEnd);
        Set<BluetoothDevice> device=bluetoothAdapter.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();
        for(Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();){
          BluetoothDevice btd=it.next();

          list.add(btd.getName().toString() +','+ btd.getAddress().toString());
        }
        // Log.d("list", list.toString());
        result.success(list.toString());
      }
    }
    catch (Exception exe) {
      Log.d("TAG", "Exception--: " + exe);
      result.error("","find ble error",exe);
    }
  }
  private void disConnect(@NonNull Result result) {
    myBinder.DisconnectCurrentPort(new TaskCallback() {
      @Override
      public void OnSucceed() {
        ISCONNECT = false;
        result.success("disconnect");
      }

      @Override
      public void OnFailed() {
        ISCONNECT = true;
        result.error("0", "OnFailed disConnectBT","");
      }
    });
  }
  private void connectBLE(String macAddress,@NonNull Result result){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        reqBlePermission(result);
        return;
      }
    }
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
      reqBlePermission(result);
      return;
    }

    if (macAddress.equals(null)||macAddress.equals("")){
      result.error("","Error connect BTE","");
    }else {
      if(ISCONNECT==true && address == macAddress){
        result.success("connect BTE success");
      }else{
        myBinder.ConnectBtPort(macAddress, new TaskCallback() {
          @Override
          public void OnSucceed() {
            address = macAddress;
            ISCONNECT=true;
            result.success("connect BTE success");
          }

          @Override
          public void OnFailed() {
            address ="";
            disConnect(result);
            result.error("","Error connect BTE","");

          }
        } );
      }
    }
  }
  private void connectNet(String ip,@NonNull Result result){
    if (ip!=null){
      if (ISCONNECT) {
        myBinder.DisconnectCurrentPort(new TaskCallback() {
          @Override
          public void OnSucceed() {
            ISCONNECT = false;
            connectNet(ip,result);

          }

          @Override
          public void OnFailed() {
            ISCONNECT = true;
            Log.e("Log --> ","connect error");
            result.error("Log -> ",Boolean.toString(ISCONNECT),"");
          }
        });
      } else {
        myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
          @Override
          public void OnSucceed() {
            ISCONNECT = true;
            Log.e("Log --> ","connect done");
            result.success(Boolean.toString(ISCONNECT));

          }

          @Override
          public void OnFailed() {
            ISCONNECT = false;
            Log.e("Log --> ","connect error");
            result.error("Log -> ",Boolean.toString(ISCONNECT),"");

          }
        });
      }

    }
  }
  public static Bitmap decodeBase64ToBitmap(String base64String) {
    byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
  }
  private void printRawData(String ip,String encode,@NonNull Result result){
    byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
    myBinder.Write(bytes, new TaskCallback() {
      @Override
      public void OnSucceed() {
        result.success("success print raw");
      }
      @Override
      public void OnFailed() {
        Log.e("Log","error print raw");
        result.error("err","error print raw","");
      }
    });
  }
  private void printBitmap(String base64String,int w1,int isBLE ,@NonNull Result result){
    final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
            (decodeBase64ToBitmap(base64String),w1);

    if (ISCONNECT){
      myBinder.WriteSendData(new TaskCallback() {
        @Override
        public void OnSucceed() {
          result.success("1");
        }

        @Override
        public void OnFailed() {
          result.error("","OnFailed print img","");
          disConnect(result);
        }
      }, new ProcessData() {
        @Override
        public List<byte[]> processDataBeforeSend() {
          List<byte[]> list = new ArrayList<>();
          list.add(DataForSendToPrinterPos80.initializePrinter());
          List<Bitmap> blist= new ArrayList<>();
          blist = BitmapProcess.cutBitmap(w1,bitmap1);
          for (int i= 0 ;i<blist.size();i++){
            list.add(DataForSendToPrinterPos80.printRasterBmp(0,blist.get(i), BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,w1));
            // list.add(DataForSendToPrinterPos80.printRasterBmp(0,blist.get(i), BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,w2));
          }
          list.add(DataForSendToPrinterPos80.printAndFeedLine());

          if(isBLE == 0){
            list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
          }

          return list;
        }
      });
    }else {
      result.error("","OnFailed print img","");
      disConnect(result);

    }
  }
  private void printLangPrinter(@NonNull Result result){
   
  }


  private void setLang(String codepage,@NonNull Result result){
   
  }
  private void cancelChinese(){
   if (ISCONNECT){
  
    }
  }

  private void getLangModel(@NonNull Result result){
 
  }
  public void sendDataToPrinter(byte[] bArr) {
    if (ISCONNECT) {
      myBinder.Write(bArr, new TaskCallback() {
        public void OnFailed() {
          Log.e( "OnFailed: ", "");
        }
        public void OnSucceed() {
          Log.e( "OnSucceed: ", "");
        }
      });
    } else {
      Log.e("OnFailed: CONNECT", "");
    }
  }

  private void reqBlePermission(@NonNull Result rawResult) {

    reqBlePermissionLocation(rawResult);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activityBinding = binding;
    activityBinding.getActivity();
    activity = binding.getActivity();
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

  private static class MethodResultWrapper implements Result {
    private final Result methodResult;
    private final Handler handler;

    MethodResultWrapper(Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(() -> methodResult.success(result));
    }

    @Override
    public void error(@NonNull final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(() -> methodResult.error(errorCode, errorMessage, errorDetails));
    }

    @Override
    public void notImplemented() {
      handler.post(methodResult::notImplemented);
    }
  }

  private void getBondedDevices(Result result) {
    List<Map<String, Object>> list = new ArrayList<>();
    result.success(list);
  }



  private void reqBlePermissionLocation(@NonNull Result rawResult) {
    Result result = new MethodResultWrapper(rawResult);
    try {
      if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1024);
      }
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

          ActivityCompat.requestPermissions(activity,new String[]{
                  Manifest.permission.BLUETOOTH_SCAN,
                  Manifest.permission.BLUETOOTH_CONNECT,
                  Manifest.permission.ACCESS_FINE_LOCATION,
          }, 1);
          pendingResult = result;
        }
      } else {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

          ActivityCompat.requestPermissions(activity,
                  new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION }, 1451);

          pendingResult = result;
        }
      }


    } catch (Exception ex) {
      Log.e("error",ex.toString());
    }
  }


  private void tryGetUsbPermission() {
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
  private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            //user choose YES for your previously popup window asking for grant perssion for this usb device
            if (null != usbDevice) {
              afterGetUsbPermission(usbDevice);
            }
          } else {
            //user choose NO for your previously popup window asking for grant perssion for this usb device
            Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
          }
        }
      }
    }
  };
  private void doYourOpenUsbDevice(UsbDevice usbDevice) {
    UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
  }
  private void afterGetUsbPermission(UsbDevice usbDevice) {
    doYourOpenUsbDevice(usbDevice);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }


}
