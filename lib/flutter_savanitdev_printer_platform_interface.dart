import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_savanitdev_printer_method_channel.dart';

abstract class FlutterSavanitdevPrinterPlatform extends PlatformInterface {
  /// Constructs a FlutterSavanitdevPrinterPlatform.
  FlutterSavanitdevPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterSavanitdevPrinterPlatform _instance =
      MethodChannelFlutterSavanitdevPrinter();

  /// The default instance of [FlutterSavanitdevPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterSavanitdevPrinter].
  static FlutterSavanitdevPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterSavanitdevPrinterPlatform] when
  /// they register themselves.
  static set instance(FlutterSavanitdevPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> connectNet(String ip) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> disconnectNet(String ip) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> disconnectBLE() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> onCreate() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> initBLE() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> startScanBLE() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> connectBLE(String macAddress) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> connectUSB(String usbAddress) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future findAvailableDevice() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future reqBlePermission() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> checkStatus() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printBitmap() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printLangPrinter() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> setLang(String codePage) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> cancelChinese() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<dynamic>?> getListDevice() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getLangModel() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> tryGetUsbPermission() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printRawData(String ip, String encode, bool isDisconnect) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> rawDataBLE(String encode) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printBLEinPrinter(int isCut) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> image64BaseBLE(String base64String, int width, int isBLE) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printBLEImgAndSet(String base64String, int width, int isCut) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> printImgNet(
      String ip, String base64String, int width, bool isDisconnect) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getUSB() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
