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
  Future<List> discovery(String type, int timeout) {
    // TODO: implement connectMultiXPrinter
    throw UnimplementedError();
  }

  @override
  Future<bool> connect(
      String address, String type, bool isCloseConnection, int timeout) {
    // TODO: implement connectMultiXPrinter
    throw UnimplementedError();
  }

  @override
  Future<bool> disconnect(String address, int timeout) {
    // TODO: implement connectMultiXPrinter
    throw UnimplementedError();
  }

  @override
  Future<bool> printCommand(
      {String address = "",
      String iniCommand = "",
      String cutterCommands = "",
      String img = "",
      String encode = "",
      bool isCut = false,
      bool isDisconnect = false,
      bool isDevicePOS = false,
      int timeout = 30}) {
    // TODO: implement printCommand
    throw UnimplementedError();
  }

  @override
  Future<String?> connectMultiXPrinter(String address, String type) {
    // TODO: implement connectMultiXPrinter
    throw UnimplementedError();
  }

  @override
  Future<String?> cutESCX(String address) {
    // TODO: implement cutESCX
    throw UnimplementedError();
  }

  @override
  Future<String?> disconnectXPrinter(String address) {
    // TODO: implement disconnectXPrinter
    throw UnimplementedError();
  }

  @override
  Future<String?> pingDevice(String address, int timeout) {
    // TODO: implement pingDevice
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgCPCL(
      String address, String encode, int width, int x, int y) {
    // TODO: implement printImgCPCL
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgESCX(String address, String encode, int countCut,
      int width, bool isDevicePOS) {
    // TODO: implement printImgESCX
    throw UnimplementedError();
  }

  @override
  Future<String?> printRawDataESC(
      String address, String encode, bool isDevicePOS) {
    // TODO: implement printRawDataESC
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgTSPL(String address, String encode, int width,
      int widthBmp, int height, int m, int n, int x, int y) {
    // TODO: implement printImgTSPL
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgZPL(
      String address, String encode, int printCount, int width, int x, int y) {
    // TODO: implement printImgZPL
    throw UnimplementedError();
  }

  @override
  Future<String?> printRawDataCPCL(String address, String encode) {
    // TODO: implement printRawDataCPCL
    throw UnimplementedError();
  }

  @override
  Future<String?> printRawDataTSPL(String address, String encode) {
    // TODO: implement printRawDataTSPL
    throw UnimplementedError();
  }

  @override
  Future<String?> printerStatusZPL(String address, int timeout) {
    // TODO: implement printerStatusZPL
    throw UnimplementedError();
  }

  @override
  Future<String?> removeConnection(String address) {
    // TODO: implement removeConnection
    throw UnimplementedError();
  }

  @override
  Future<String?> setPrintDensity(String address, String density) {
    // TODO: implement setPrintDensity
    throw UnimplementedError();
  }

  @override
  Future<String?> setPrintOrientation(String address, String orientation) {
    // TODO: implement setPrintOrientation
    throw UnimplementedError();
  }

  @override
  Future<String?> setPrintSpeed(String address, int speed) {
    // TODO: implement setPrintSpeed
    throw UnimplementedError();
  }

  @override
  Future<List<int>> strTobytes(String text, String codePage) {
    // TODO: implement setPrintSpeed
    throw UnimplementedError();
  }

  @override
  Future<String?> startQuickDiscovery(int timeout) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List> USBDiscovery() {
    // TODO: implement USBDiscovery
    throw UnimplementedError();
  }

  @override
  Future<String?> getUSBAddress(int productId, int vendorId) {
    // TODO: implement getUSBAddress
    throw UnimplementedError();
  }

  // ============= ZyWell Printer ==================//

  @override
  Future<String?> connectZyWell(String address, String type) {
    // TODO: implement connectZyWell
    throw UnimplementedError();
  }

  @override
  Future<String?> disconnectZyWell(String address) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<String?> getPrinterStatusZyWell(String address) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<String?> printRawZyWell(String address, String encode) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<String?> printImgZyWell(
      String address, String encode, bool isCut, int width, int cutCount) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

// ============= ESC POS command ==================//

  @override
  Future<List<int>> selectAlignment(String align) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> selectCharacterSize(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> selectOrCancelBoldModel(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> selectCharacterCodePage(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> setBarcodeWidth(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> setBarcodeHeight(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> selectHRICharacterPrintPosition(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> selectInternationalCharacterSets(int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> printBarcode(int m, int n, String content) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> setAbsolutePrintPosition(int m, int n) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> text(String text, String codePage) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> printQRcode(int n, int errLevel, String code) {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> cut() {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> initializePrinter() {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> cancelChineseCharModel() {
    // TODO: implement startQuickDiscovery
    throw UnimplementedError();
  }

  @override
  Future<List<int>> printAndFeedLine() {
    // TODO: implement startQuickDiscovery
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
