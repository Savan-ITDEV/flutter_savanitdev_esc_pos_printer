import 'dart:io';
import 'flutter_savanitdev_printer_platform_interface.dart';

class FlutterSavanitdevPrinter {
  Future<String?> getPlatformVersion() {
    return FlutterSavanitdevPrinterPlatform.instance.getPlatformVersion();
  }

  Future<String?> connectNet(String ip) {
    return FlutterSavanitdevPrinterPlatform.instance.connectNet(ip);
  }

  Future<String?> disconnectNet(String ip) {
    return FlutterSavanitdevPrinterPlatform.instance.disconnectNet(ip);
  }

  Future<String?> connectBLE(String macAddress) {
    return FlutterSavanitdevPrinterPlatform.instance.connectBLE(macAddress);
  }

  Future<String?> disconnectBLE() {
    return FlutterSavanitdevPrinterPlatform.instance.disconnectBLE();
  }

  Future<String?> onCreate() {
    return FlutterSavanitdevPrinterPlatform.instance.onCreate();
  }

  Future<String?> initBLE() {
    return FlutterSavanitdevPrinterPlatform.instance.initBLE();
  }

  Future<String?> startScanBLE() {
    return FlutterSavanitdevPrinterPlatform.instance.startScanBLE();
  }

  Future<String?> printBLEinPrinter(int isCut) {
    {
      return FlutterSavanitdevPrinterPlatform.instance.printBLEinPrinter(isCut);
    }
  }

  Future<String?> printRawData(String ip, String encode, bool isDisconnect) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printRawData(ip, encode, isDisconnect);
  }

  Future<String?> rawDataBLE(String encode) {
    if (Platform.isIOS) {
      return FlutterSavanitdevPrinterPlatform.instance.rawDataBLE(encode);
    } else {
      return FlutterSavanitdevPrinterPlatform.instance
          .printRawData("", encode, false);
    }
  }

  Future<String?> image64BaseBLE(String base64String, int width, int isBLE) {
    if (Platform.isIOS) {
      return FlutterSavanitdevPrinterPlatform.instance
          .image64BaseBLE(base64String, width, 0);
    } else {
      return FlutterSavanitdevPrinterPlatform.instance
          .image64BaseBLE(base64String, width, isBLE);
    }
  }

  Future<String?> printBLEImgAndSet(String base64String, int width, int isCut) {
    if (Platform.isIOS) {
      return FlutterSavanitdevPrinterPlatform.instance
          .printBLEImgAndSet(base64String, width, isCut);
    } else {
      return FlutterSavanitdevPrinterPlatform.instance
          .printBLEImgAndSet(base64String, width, isCut);
    }
  }

  Future<String?> printImgNet(
      String ip, String base64String, int width, bool isDisconnect) {
    if (Platform.isIOS) {
      return FlutterSavanitdevPrinterPlatform.instance
          .printImgNet(ip, base64String, width, isDisconnect);
    } else {
      return FlutterSavanitdevPrinterPlatform.instance
          .image64BaseBLE(base64String, 576, 1);
    }
  }

  Future findAvailableDevice() {
    if (Platform.isAndroid) {
      return FlutterSavanitdevPrinterPlatform.instance.findAvailableDevice();
    } else {
      return getListDevice();
    }
  }

  Future reqBlePermission() {
    return FlutterSavanitdevPrinterPlatform.instance.reqBlePermission();
  }

  Future<String?> checkStatus() {
    return FlutterSavanitdevPrinterPlatform.instance.checkStatus();
  }

  Future<List<dynamic>?> getListDevice() {
    return FlutterSavanitdevPrinterPlatform.instance.getListDevice();
  }

  Future<String?> printBitmap() {
    return FlutterSavanitdevPrinterPlatform.instance.printBitmap();
  }

  Future<String?> printLangPrinter() {
    return FlutterSavanitdevPrinterPlatform.instance.printLangPrinter();
  }

  Future<String?> setLang(String codePage) {
    return FlutterSavanitdevPrinterPlatform.instance.setLang(codePage);
  }

  Future<String?> cancelChinese() {
    return FlutterSavanitdevPrinterPlatform.instance.cancelChinese();
  }

  Future<String?> getLangModel() {
    return FlutterSavanitdevPrinterPlatform.instance.getLangModel();
  }

  Future<String?> tryGetUsbPermission() {
    return FlutterSavanitdevPrinterPlatform.instance.tryGetUsbPermission();
  }

  Future<String?> connectUSB(String usbAddress) {
    return FlutterSavanitdevPrinterPlatform.instance.connectUSB(usbAddress);
  }

  Future<String?> getUSB() {
    return FlutterSavanitdevPrinterPlatform.instance.getUSB();
  }
}
