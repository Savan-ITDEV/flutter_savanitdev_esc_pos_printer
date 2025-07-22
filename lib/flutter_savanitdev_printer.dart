import 'flutter_savanitdev_printer_platform_interface.dart';

class FlutterSavanitdevPrinter {
  Future<String?> getPlatformVersion() {
    return FlutterSavanitdevPrinterPlatform.instance.getPlatformVersion();
  }

  Future<List> discovery(String type, int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance.discovery(type, timeout);
  }

  Future<bool> connect(
      String address, String type, bool isCloseConnection, int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance
        .connect(address, type, isCloseConnection, timeout);
  }

  Future<bool> disconnect(String address, int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance
        .disconnect(address, timeout);
  }

  Future<bool> printCommand(
      {String address = "",
      String iniCommand = "",
      String cutterCommands = "",
      String img = "",
      String encode = "",
      bool isCut = false,
      bool isDisconnect = false,
      bool isDevicePOS = false,
      bool isDelay = true,
      int timeout = 30,
      int width = 576}) {
    return FlutterSavanitdevPrinterPlatform.instance.printCommand(
      address: address,
      iniCommand: iniCommand,
      cutterCommands: cutterCommands,
      img: img,
      encode: encode,
      isCut: isCut,
      isDisconnect: isDisconnect,
      isDevicePOS: isDevicePOS,
      isDelay: isDelay,
      timeout: timeout,
      width: width,
    );
  }

  Future<String?> connectMultiXPrinter(String address, String type) {
    return FlutterSavanitdevPrinterPlatform.instance
        .connectMultiXPrinter(address, type);
  }

  Future<String?> disconnectXPrinter(String address) {
    return FlutterSavanitdevPrinterPlatform.instance
        .disconnectXPrinter(address);
  }

  Future<String?> removeConnection(String address) {
    return FlutterSavanitdevPrinterPlatform.instance.removeConnection(address);
  }

  Future<String?> printRawDataESC(
      String address, String encode, bool isDevicePOS) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printRawDataESC(address, encode, isDevicePOS);
  }

  Future<String?> printImgESCX(String address, String encode, int countCut,
      int width, bool isDevicePOS) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printImgESCX(address, encode, countCut, width, isDevicePOS);
  }

  Future<String?> cutESCX(String address) {
    return FlutterSavanitdevPrinterPlatform.instance.cutESCX(address);
  }

  Future<String?> pingDevice(String address, int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance
        .pingDevice(address, timeout);
  }

  Future<String?> startQuickDiscovery(int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance
        .startQuickDiscovery(timeout);
  }

  Future<List> USBDiscovery() {
    return FlutterSavanitdevPrinterPlatform.instance.USBDiscovery();
  }

  Future<String?> printImgZPL(
      String address, String encode, int printCount, int width, int x, int y) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printImgZPL(address, encode, printCount, width, x, y);
  }

  Future<String?> printImgCPCL(
      String address, String encode, int width, int x, int y) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printImgCPCL(address, encode, width, x, y);
  }

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
  ) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printImgTSPL(address, encode, width, widthBmp, height, m, n, x, y);
  }

  Future<String?> setPrintSpeed(String address, int speed) {
    return FlutterSavanitdevPrinterPlatform.instance.setPrintSpeed(
      address,
      speed,
    );
  }

  Future<String?> setPrintOrientation(String address, String orientation) {
    return FlutterSavanitdevPrinterPlatform.instance.setPrintOrientation(
      address,
      orientation,
    );
  }

  Future<String?> printRawDataCPCL(String address, String encode) {
    return FlutterSavanitdevPrinterPlatform.instance.printRawDataCPCL(
      address,
      encode,
    );
  }

  Future<String?> printRawDataTSPL(String address, String encode) {
    return FlutterSavanitdevPrinterPlatform.instance.printRawDataTSPL(
      address,
      encode,
    );
  }

  Future<String?> setPrintDensity(String address, String density) {
    return FlutterSavanitdevPrinterPlatform.instance.setPrintDensity(
      address,
      density,
    );
  }

  Future<String?> printerStatusZPL(String address, int timeout) {
    return FlutterSavanitdevPrinterPlatform.instance.printerStatusZPL(
      address,
      timeout,
    );
  }

  Future<String?> getUSBAddress(int productId, int vendorId) {
    return FlutterSavanitdevPrinterPlatform.instance
        .getUSBAddress(productId, vendorId);
  }

  // ============= ZyWell Printer ==================//
  Future<bool> connectZyWell(String address, String type) {
    return FlutterSavanitdevPrinterPlatform.instance
        .connectZyWell(address, type);
  }

  Future<bool> disconnectZyWell(String address) {
    return FlutterSavanitdevPrinterPlatform.instance.disconnectZyWell(address);
  }

  Future<String?> getPrinterStatusZyWell(String address) {
    return FlutterSavanitdevPrinterPlatform.instance
        .getPrinterStatusZyWell(address);
  }

  Future<String?> printRawZyWell(String address, String encode) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printRawZyWell(address, encode);
  }

  Future<bool> printImgZyWell(
     {
      String iniCommand = "",
      String cutterCommands = "",
      String img = "",
      String encode = "",
      bool isCut = false,
      bool isDisconnect = false,
      bool isDevicePOS = false,
      int timeout = 30,
      int width = 576}
    ) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printImgZyWell(
      iniCommand: iniCommand,
      cutterCommands: cutterCommands,
      img: img,
      encode: encode,
      isCut: isCut,
      isDisconnect: isDisconnect,
      isDevicePOS: isDevicePOS,
      timeout: timeout,
      width: width);
  }

  // ============= ESC POS command ==================//

  Future<List<int>> selectAlignment(String align) {
    return FlutterSavanitdevPrinterPlatform.instance.selectAlignment(align);
  }

  Future<List<int>> selectCharacterSize(int n) {
    return FlutterSavanitdevPrinterPlatform.instance.selectCharacterSize(n);
  }

  Future<List<int>> selectOrCancelBoldModel(int n) {
    return FlutterSavanitdevPrinterPlatform.instance.selectOrCancelBoldModel(n);
  }

  Future<List<int>> selectCharacterCodePage(int n) {
    return FlutterSavanitdevPrinterPlatform.instance.selectCharacterCodePage(n);
  }

  Future<List<int>> setBarcodeWidth(int n) {
    return FlutterSavanitdevPrinterPlatform.instance.setBarcodeWidth(n);
  }

  Future<List<int>> setBarcodeHeight(int n) {
    return FlutterSavanitdevPrinterPlatform.instance.setBarcodeHeight(n);
  }

  Future<List<int>> selectHRICharacterPrintPosition(int n) {
    return FlutterSavanitdevPrinterPlatform.instance
        .selectHRICharacterPrintPosition(n);
  }

  Future<List<int>> selectInternationalCharacterSets(int n) {
    return FlutterSavanitdevPrinterPlatform.instance
        .selectInternationalCharacterSets(n);
  }

  Future<List<int>> printBarcode(int m, int n, String content) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printBarcode(m, n, content);
  }

  Future<List<int>> setAbsolutePrintPosition(int m, int n) {
    return FlutterSavanitdevPrinterPlatform.instance
        .setAbsolutePrintPosition(m, n);
  }

  Future<List<int>> text(String text, String codePage) {
    return FlutterSavanitdevPrinterPlatform.instance.text(text, codePage);
  }

  //Module size. Range[1, 16], Default
  Future<List<int>> printQRcode(int n, int errLevel, String code) {
    return FlutterSavanitdevPrinterPlatform.instance
        .printQRcode(n, errLevel, code);
  }

  Future<List<int>> cut() {
    return FlutterSavanitdevPrinterPlatform.instance.cut();
  }

  Future<List<int>> initializePrinter() {
    return FlutterSavanitdevPrinterPlatform.instance.initializePrinter();
  }

  Future<List<int>> cancelChineseCharModel() {
    return FlutterSavanitdevPrinterPlatform.instance.cancelChineseCharModel();
  }

  Future<List<int>> printAndFeedLine() {
    return FlutterSavanitdevPrinterPlatform.instance.printAndFeedLine();
  }
}
