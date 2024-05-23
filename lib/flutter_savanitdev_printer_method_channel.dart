import 'dart:io';
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
  Future<String?> onCreate() async {
    final version = await methodChannel.invokeMethod<String>('onCreate');
    return version;
  }

  @override
  Future<String?> initBLE() async {
    final version = await methodChannel.invokeMethod<String>('initBLE');
    return version;
  }

  @override
  Future<String?> startScanBLE() async {
    final version = await methodChannel.invokeMethod<String>('startScanBLE');
    return version;
  }

  @override
  Future<List<dynamic>?> getListDevice() async {
    final version = await methodChannel.invokeMethod('getListDevice');
    return version;
  }

  @override
  Future<String?> checkStatusBLE() async {
    final version = await methodChannel.invokeMethod<String>('checkStatusBLE');
    return version;
  }

  @override
  Future<String?> connectNet(String ip) async {
    final version = await methodChannel.invokeMethod<String>('connectNet', ip);
    return version;
  }

  @override
  Future<String?> disconnectNet(String ip) async {
    if (Platform.isIOS) {
      final version =
          await methodChannel.invokeMethod<String>('disconnectNet', ip);
      return version;
    } else {
      final version = await methodChannel.invokeMethod<String>('disConnect');
      return version;
    }
  }

  @override
  Future<String?> setLang(String codePage) async {
    final version =
        await methodChannel.invokeMethod<String>('setLang', codePage);
    return version;
  }

  @override
  Future<String?> printLangPrinter() async {
    final version =
        await methodChannel.invokeMethod<String>('printLangPrinter');
    return version;
  }

  @override
  Future<String?> getLangModel() async {
    final version = await methodChannel.invokeMethod<String>('getLangModel');
    return version;
  }

  @override
  Future<String?> rawDataBLE(String encode) async {
    final version =
        await methodChannel.invokeMethod<String>('rawDataBLE', encode);
    return version;
  }

  @override
  Future<String?> image64BaseBLE(
      String base64String, int width, int isBLE) async {
    final version = await methodChannel
        .invokeMethod<String>('image64BaseBLE', <String, dynamic>{
      'width': width,
      'isBLE': isBLE,
      'base64String': base64String,
    });
    return version;
  }

  @override
  Future<String?> printBLEImgAndSet(
      String base64String, int width, int isCut) async {
    final version = await methodChannel
        .invokeMethod<String>('printBLEImgAndSet', <String, dynamic>{
      'width': width,
      'isCut': isCut,
      'base64String': base64String,
    });
    return version;
  }

  @override
  Future<String?> printImgNet(
      String ip, String base64String, int width, bool isDisconnect) async {
    final version = await methodChannel
        .invokeMethod<String>('printImgNet', <String, dynamic>{
      'ip': ip,
      'base64String': base64String,
      'width': width,
      'isDisconnect': isDisconnect,
    });
    return version;
  }

  @override
  Future<String?> printRawData(
      String ip, String encode, bool isDisconnect) async {
    final version = await methodChannel
        .invokeMethod<String>('printRawData', <String, dynamic>{
      'ip': ip,
      'encode': encode,
      'isDisconnect': isDisconnect,
    });
    return version;
  }

  @override
  Future<String?> printBLEinPrinter(int isCut) async {
    final version =
        await methodChannel.invokeMethod<String>('printBLEinPrinter', isCut);
    return version;
  }

  @override
  Future<String?> connectBLE(String macAddress) async {
    final version =
        await methodChannel.invokeMethod<String>('connectBLE', macAddress);
    return version;
  }

  @override
  Future<String?> disconnectBLE() async {
    if (Platform.isIOS) {
      final version = await methodChannel.invokeMethod<String>('disconnectBLE');
      return version;
    } else {
      final version = await methodChannel.invokeMethod<String>('disConnect');
      return version;
    }
  }

  @override
  Future<String?> connectUSB(String usbAddress) async {
    final version =
        await methodChannel.invokeMethod<String>('connectUSB', usbAddress);
    return version;
  }

  @override
  findAvailableDevice() async {
    final version =
        await methodChannel.invokeMethod<String>('findAvailableDevice');
    List<String> items =
        version.toString().replaceAll('[', '').replaceAll(']', '').split(',');

    // Trim extra spaces from each element
    List<String> trimmedItems = items.map((item) => item.trim()).toList();

    // Create a list of maps with name and address
    List<Map<String, String>> nameAddressList = [];
    for (int i = 0; i < trimmedItems.length; i += 2) {
      if (i + 1 < trimmedItems.length) {
        Map<String, String> map = {
          'name': trimmedItems[i],
          'deviceAddress': trimmedItems[i + 1]
        };
        nameAddressList.add(map);
      }
    }

    return nameAddressList;
  }

  @override
  reqBlePermission() async {
    await methodChannel.invokeMethod('reqBlePermission');
    // return version;
  }

  @override
  tryGetUsbPermission() async {
    final version =
        await methodChannel.invokeMethod<String>('tryGetUsbPermission');
    return version;
  }

  @override
  getUSB() async {
    final version = await methodChannel.invokeMethod<String>('getUSB');
    return version;
  }
}
