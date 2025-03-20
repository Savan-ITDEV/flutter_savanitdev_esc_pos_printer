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

  Future<List> discovery(String type, int timeout);
  Future<bool> connect(
      String address, String type, bool isCloseConnection, int timeout);
  Future<bool> disconnect(String address, int timeout);
  Future<bool> printCommand(
      {String address = "",
      String iniCommand = "",
      String cutterCommands = "",
      String img = "",
      String encode = "",
      bool isCut = false,
      bool isDisconnect = false,
      bool isDevicePOS = false,
      int timeout = 30,
      int width = 576});
  Future<String?> connectMultiXPrinter(String address, String type);

  Future<String?> disconnectXPrinter(String address);

  Future<String?> removeConnection(String address);

  Future<String?> printRawDataESC(
      String address, String encode, bool isDevicePOS);

  Future<String?> printImgESCX(
      String address, String encode, int countCut, int width, bool isDevicePOS);

  Future<String?> cutESCX(String address);

  Future<String?> pingDevice(String address, int timeout);

  Future<String?> startQuickDiscovery(int timeout);
  Future<List> USBDiscovery();

  Future<String?> printImgZPL(
      String address, String encode, int printCount, int width, int x, int y);

  Future<String?> printImgCPCL(
      String address, String encode, int width, int x, int y);

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
  );

  Future<String?> setPrintSpeed(String address, int speed);

  Future<String?> setPrintOrientation(String address, String orientation);

  Future<String?> printRawDataCPCL(String address, String encode);

  Future<String?> printRawDataTSPL(String address, String encode);

  Future<String?> setPrintDensity(String address, String density);

  Future<String?> printerStatusZPL(String address, int timeout);
  Future<String?> getUSBAddress(int productId, int vendorId);

// ============= ZyWell Printer ==================//

  Future<String?> connectZyWell(String address, String type);
  Future<String?> disconnectZyWell(String address);
  Future<String?> getPrinterStatusZyWell(String address);
  Future<String?> printRawZyWell(String address, String encode);
  Future<String?> printImgZyWell(
      String address, String encode, bool isCut, int width, int cutCount);

  // ============= ESC POS command ==================//
  Future<List<int>> selectAlignment(String align);
  Future<List<int>> selectCharacterSize(int n);
  Future<List<int>> selectOrCancelBoldModel(int n);
  Future<List<int>> selectCharacterCodePage(int n);
  Future<List<int>> setBarcodeWidth(int n);
  Future<List<int>> setBarcodeHeight(int n);
  Future<List<int>> selectHRICharacterPrintPosition(int n);
  Future<List<int>> selectInternationalCharacterSets(int n);
  Future<List<int>> printBarcode(int m, int n, String content);
  Future<List<int>> setAbsolutePrintPosition(int m, int n);
  Future<List<int>> text(String text, String codePage);
  Future<List<int>> printQRcode(int n, int errLevel, String code);
  Future<List<int>> cut();
  Future<List<int>> initializePrinter();
  Future<List<int>> cancelChineseCharModel();
  Future<List<int>> printAndFeedLine();
}
