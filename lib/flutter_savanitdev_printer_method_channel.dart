import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_savanitdev_printer_platform_interface.dart';

/// An implementation of [FlutterSavanitdevPrinterPlatform] that uses method channels.
class MethodChannelFlutterSavanitdevPrinter
    extends FlutterSavanitdevPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_savanitdev_printer');

  @override
  Future<String?> connectMultiXPrinter(String address, String type) async {
    final version = await methodChannel
        .invokeMethod<String>('connectMultiXPrinter', <String, dynamic>{
      'address': address,
      'type': type,
    });
    return version;
  }

  @override
  Future<String?> disconnectXPrinter(String address) async {
    final version = await methodChannel
        .invokeMethod<String>('disconnectXPrinter', <String, dynamic>{
      'address': address,
    });
    return version;
  }

  @override
  Future<String?> removeConnection(String address) async {
    final version = await methodChannel
        .invokeMethod<String>('removeConnection', <String, dynamic>{
      'address': address,
    });
    return version;
  }

  @override
  Future<String?> printRawDataESC(
      String address, String encode, bool isDevicePOS) async {
    final version = await methodChannel
        .invokeMethod<String>('printRawDataESC', <String, dynamic>{
      'address': address,
      'encode': encode,
      'isDevicePOS': isDevicePOS,
    });
    return version;
  }

  @override
  Future<String?> printImgESCX(String address, String encode, int countCut,
      int width, bool isDevicePOS) async {
    final version = await methodChannel
        .invokeMethod<String>('printImgESCX', <String, dynamic>{
      'address': address,
      'encode': encode,
      'width': width,
      'isDevicePOS': isDevicePOS,
    });

    return version;
  }

  @override
  Future<String?> cutESCX(String address) async {
    final version =
        await methodChannel.invokeMethod<String>('cutESCX', <String, dynamic>{
      'address': address,
    });
    return version;
  }

  @override
  Future<String?> pingDevice(String address, int timeout) async {
    final version = await methodChannel
        .invokeMethod<String>('pingDevice', <String, dynamic>{
      'address': address,
      'timeout': timeout,
    });
    return version;
  }

  @override
  Future<String?> startQuickDiscovery(int timeout) async {
    final version = await methodChannel
        .invokeMethod<String>('startQuickDiscovery', <String, dynamic>{
      'timeout': timeout,
    });
    return version;
  }

  @override
  Future<List> USBDiscovery() async {
    final version = await methodChannel.invokeMethod('USBDiscovery');
    return version;
  }

  @override
  Future<String?> printImgZPL(String address, String encode, int printCount,
      int width, int x, int y) async {
    final version = await methodChannel
        .invokeMethod<String>('printImgZPL', <String, dynamic>{
      'address': address,
      'encode': encode,
      'printCount': printCount,
      'width': width,
      'x': x,
      'y': y,
    });
    return version;
  }

  @override
  Future<String?> printImgCPCL(
      String address, String encode, int width, int x, int y) async {
    final version = await methodChannel
        .invokeMethod<String>('printImgCPCL', <String, dynamic>{
      'address': address,
      'encode': encode,
      'width': width,
      'x': x,
      'y': y,
    });
    return version;
  }

  @override
  Future<String?> printImgTSPL(
    String address,
    String encode,
    int width,
    int widthBmp,
    int height,
    int m,
    int n,
    int x,
    int y,
  ) async {
    final version = await methodChannel
        .invokeMethod<String>('printImgTSPL', <String, dynamic>{
      'address': address,
      'encode': encode,
      'width': width,
      'widthBmp': widthBmp,
      'height': height,
      'm': m,
      'n': n,
      'x': x,
      'y': y,
    });
    return version;
  }

  @override
  Future<String?> setPrintSpeed(String address, int speed) async {
    final version = await methodChannel
        .invokeMethod<String>('setPrintSpeed', <String, dynamic>{
      'address': address,
      'speed': speed,
    });
    return version;
  }

  @override
  Future<String?> setPrintOrientation(
      String address, String orientation) async {
    final version = await methodChannel
        .invokeMethod<String>('setPrintOrientation', <String, dynamic>{
      'address': address,
      'orientation': orientation,
    });
    return version;
  }

  @override
  Future<String?> printRawDataCPCL(String address, String encode) async {
    final version = await methodChannel
        .invokeMethod<String>('printRawDataCPCL', <String, dynamic>{
      'address': address,
      'encode': encode,
    });
    return version;
  }

  @override
  Future<String?> printRawDataTSPL(String address, String encode) async {
    final version = await methodChannel
        .invokeMethod<String>('printRawDataTSPL', <String, dynamic>{
      'address': address,
      'encode': encode,
    });
    return version;
  }

  @override
  Future<String?> setPrintDensity(String address, String density) async {
    final version = await methodChannel
        .invokeMethod<String>('setPrintDensity', <String, dynamic>{
      'address': address,
      'density': density,
    });
    return version;
  }

  @override
  Future<String?> printerStatusZPL(String address, int timeout) async {
    final version = await methodChannel
        .invokeMethod<String>('printerStatusZPL', <String, dynamic>{
      'address': address,
      'timeout': timeout,
    });
    return version;
  }

  @override
  Future<String?> getUSBAddress(int productId, int vendorId) async {
    final version = await methodChannel.invokeMethod('getUSBAddress', {
      'productId': productId,
      'vendorId': vendorId,
    });
    return version;
  }

// ============= ZyWell Printer ==================//

  @override
  Future<String?> connectZyWell(String address, String type) async {
    final version = await methodChannel
        .invokeMethod<String>('connectZyWell', <String, dynamic>{
      'address': address,
      'type': type,
    });
    return version;
  }

  @override
  Future<String?> disconnectZyWell(String address) async {
    final version = await methodChannel
        .invokeMethod<String>('disconnectZyWell', <String, dynamic>{
      'address': address,
    });
    return version;
  }

  @override
  Future<String?> getPrinterStatusZyWell(String address) async {
    final version = await methodChannel
        .invokeMethod<String>('getPrinterStatusZyWell', <String, dynamic>{
      'address': address,
    });
    return version;
  }

  @override
  Future<String?> printRawZyWell(String address, String encode) async {
    final version =
        await methodChannel.invokeMethod('printRawZyWell', <dynamic, dynamic>{
      'address': address,
      'encode': encode,
    });
    return version;
  }

  @override
  Future<String?> printImgZyWell(String address, String encode, bool isCut,
      int width, int cutCount) async {
    final version =
        await methodChannel.invokeMethod('printImgZyWell', <dynamic, dynamic>{
      'address': address,
      'encode': encode,
      'isCut': isCut,
      'width': width,
      'cutCount': cutCount,
    });
    return version;
  }

// ============= ESC POS command ==================//

  @override
  Future<List<int>> selectAlignment(String align) async {
    int num = 1;
    if (align == "center") {
      num = 1;
    }
    if (align == "right") {
      num = 2;
    }
    if (align == "left") {
      num = 0;
    }
    final version =
        await methodChannel.invokeMethod('selectAlignment', {'n': num});
    return version;
  }

  @override
  Future<List<int>> selectCharacterSize(int n) async {
    final version =
        await methodChannel.invokeMethod('selectCharacterSize', {'n': n});
    return version;
  }

  @override
  Future<List<int>> selectOrCancelBoldModel(int n) async {
    final version =
        await methodChannel.invokeMethod('selectOrCancelBoldModel', {'n': n});
    return version;
  }

  @override
  Future<List<int>> selectCharacterCodePage(int n) async {
    final version =
        await methodChannel.invokeMethod('selectCharacterCodePage', {'n': n});
    return version;
  }

  @override
  Future<List<int>> setBarcodeWidth(int n) async {
    final version =
        await methodChannel.invokeMethod('setBarcodeWidth', {'n': n});
    return version;
  }

  @override
  Future<List<int>> setBarcodeHeight(int n) async {
    final version =
        await methodChannel.invokeMethod('setBarcodeHeight', {'n': n});
    return version;
  }

  @override
  Future<List<int>> selectHRICharacterPrintPosition(int n) async {
    final version = await methodChannel
        .invokeMethod('selectHRICharacterPrintPosition', {'n': n});
    return version;
  }

  @override
  Future<List<int>> selectInternationalCharacterSets(int n) async {
    final version = await methodChannel
        .invokeMethod('selectInternationalCharacterSets', {'n': n});
    return version;
  }

  @override
  Future<List<int>> printBarcode(int m, int n, String content) async {
    final version = await methodChannel
        .invokeMethod('printBarcode', {'m': m, 'n': n, 'content': content});
    return version;
  }

  @override
  Future<List<int>> setAbsolutePrintPosition(int m, int n) async {
    final version = await methodChannel
        .invokeMethod('setAbsolutePrintPosition', {'m': m, 'n': n});
    return version;
  }

  @override
  Future<List<int>> text(String text, String codePage) async {
    final version = await methodChannel
        .invokeMethod('text', {'text': text, 'codePage': codePage ?? "cp874"});
    return version;
  }

  @override
  Future<List<int>> printQRcode(int n, int errLevel, String content) async {
    final version = await methodChannel.invokeMethod(
        'printQRcode', {'n': n, 'errLevel': errLevel, 'content': content});
    return version;
  }

  @override
  Future<List<int>> cut() async {
    final version = await methodChannel.invokeMethod('cut');
    return version;
  }

  @override
  Future<List<int>> initializePrinter() async {
    final version = await methodChannel.invokeMethod('initializePrinter');
    return version;
  }

  @override
  Future<List<int>> cancelChineseCharModel() async {
    final version = await methodChannel.invokeMethod('cancelChineseCharModel');
    return version;
  }

  @override
  Future<List<int>> printAndFeedLine() async {
    final version = await methodChannel.invokeMethod('printAndFeedLine');
    return version;
  }

  // ============= ESC POS command ==================//
}
