import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_savanitdev_printer/flutter_savanitdev_printer.dart';
import 'package:flutter_savanitdev_printer/flutter_savanitdev_printer_platform_interface.dart';
import 'package:flutter_savanitdev_printer/flutter_savanitdev_printer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterSavanitdevPrinterPlatform
    with MockPlatformInterfaceMixin
    implements FlutterSavanitdevPrinterPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');



  @override
  Future<String?> cancelChinese() {
    // TODO: implement cancelChinese
    throw UnimplementedError();
  }

  @override
  Future<String?> checkStatus() {
    // TODO: implement checkStatus
    throw UnimplementedError();
  }

  @override
  Future<String?> connectBLE(String macAddress) {
    // TODO: implement connectBLE
    throw UnimplementedError();
  }

  @override
  Future<String?> connectNet(String ip) {
    // TODO: implement connectNet
    throw UnimplementedError();
  }

  @override
  Future<String?> connectUSB(String usbAddress) {
    // TODO: implement connectUSB
    throw UnimplementedError();
  }

  @override
  Future<String?> disconnectBLE() {
    // TODO: implement disconnectBLE
    throw UnimplementedError();
  }

  @override
  Future<String?> disconnectNet(String ip) {
    // TODO: implement disconnectNet
    throw UnimplementedError();
  }

  @override
  Future findAvailableDevice() {
    // TODO: implement findAvailableDevice
    throw UnimplementedError();
  }

  @override
  Future<String?> getLangModel() {
    // TODO: implement getLangModel
    throw UnimplementedError();
  }

  @override
  Future<List?> getListDevice() {
    // TODO: implement getListDevice
    throw UnimplementedError();
  }

  @override
  Future<String?> getUSB() {
    // TODO: implement getUSB
    throw UnimplementedError();
  }

  @override
  Future<String?> image64BaseBLE(String base64String, int width, int isBLE) {
    // TODO: implement image64BaseBLE
    throw UnimplementedError();
  }

  @override
  Future<String?> initBLE() {
    // TODO: implement initBLE
    throw UnimplementedError();
  }

  @override
  Future<String?> onCreate() {
    // TODO: implement onCreate
    throw UnimplementedError();
  }

  @override
  Future<String?> printBitmap() {
    // TODO: implement printBitmap
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgNet(
      String ip, String base64String, int width, bool isDisconnect) {
    // TODO: implement printImgNet
    throw UnimplementedError();
  }

  @override
  Future<String?> printLangPrinter() {
    // TODO: implement printLangPrinter
    throw UnimplementedError();
  }

  @override
  Future<String?> printRawData(String ip, String encode, bool isDisconnect) {
    // TODO: implement printRawData
    throw UnimplementedError();
  }

  @override
  Future<String?> rawDataBLE(String encode) {
    // TODO: implement rawDataBLE
    throw UnimplementedError();
  }

  @override
  Future reqBlePermission() {
    // TODO: implement reqBlePermission
    throw UnimplementedError();
  }

  @override
  Future<String?> setLang(String codePage) {
    // TODO: implement setLang
    throw UnimplementedError();
  }

  @override
  Future<String?> startScanBLE() {
    // TODO: implement startScanBLE
    throw UnimplementedError();
  }

  @override
  Future<String?> tryGetUsbPermission() {
    // TODO: implement tryGetUsbPermission
    throw UnimplementedError();
  }

  @override
  Future<String?> printBLEinPrinter(int isCut) {
    // TODO: implement printBLEinPrinter
    throw UnimplementedError();
  }

  @override
  Future<String?> printBLEImgAndSet(String base64String, int width, int isCut) {
    // TODO: implement printBLEImgAndSet
    throw UnimplementedError();
  }
}

void main() {
  final FlutterSavanitdevPrinterPlatform initialPlatform =
      FlutterSavanitdevPrinterPlatform.instance;

  test('$MethodChannelFlutterSavanitdevPrinter is the default instance', () {
    expect(
        initialPlatform, isInstanceOf<MethodChannelFlutterSavanitdevPrinter>());
  });

  test('getPlatformVersion', () async {
    FlutterSavanitdevPrinter flutterSavanitdevPrinterPlugin =
        FlutterSavanitdevPrinter();
    MockFlutterSavanitdevPrinterPlatform fakePlatform =
        MockFlutterSavanitdevPrinterPlatform();
    FlutterSavanitdevPrinterPlatform.instance = fakePlatform;

    expect(await flutterSavanitdevPrinterPlugin.getPlatformVersion(), '42');
  });
}
