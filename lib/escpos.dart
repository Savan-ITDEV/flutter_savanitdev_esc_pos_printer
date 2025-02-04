import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_charset_savanitdev/charset.dart';

class ESCPOS {
  // ============= ESC POS command ==================//
  List<int> _data = [];
  List<int> toBytes() {
    return _data;
  }

  String toEncode() {
    return base64Encode(_data);
  }

  static Uint8List byteMerger(Uint8List byte1, Uint8List byte2) {
    Uint8List byte3 = Uint8List(byte1.length + byte2.length);
    byte3.setAll(0, byte1);
    byte3.setAll(byte1.length, byte2);
    return byte3;
  }

  ESCPOS printQRcode(int size, int errLevel, String code) {
    Uint8List b = strToBytes(code);
    int a = b.length;

    int nL = a <= 255 ? a : a % 256;
    int nH = a <= 255 ? 0 : a ~/ 256;

    // Base data array
    Uint8List data = Uint8List.fromList([
      29,
      40,
      107,
      48,
      103,
      size,
      29,
      40,
      107,
      48,
      105,
      errLevel,
      29,
      40,
      107,
      48,
      128,
      nL,
      nH
    ]);

    // Merge the QR code data
    data = byteMerger(data, b);

    // Add final byte sequence
    Uint8List c = Uint8List.fromList([29, 40, 107, 48, 129]);
    data = byteMerger(data, c);

    _data += Uint8List.fromList(data);
    return this;
  }

  Uint8List printBarcode(int m, int n, String content) {
    // Initial command with barcode type `m` and additional parameter `n`
    Uint8List data = Uint8List.fromList([29, 107, m, n]);

    // Convert content to bytes
    Uint8List text = strToBytes(content);

    // Merge the data and text arrays
    data = byteMerger(data, text);

    return data;
  }

  static Uint8List strToBytes(String str) {
    try {
      // First convert string to UTF-8 bytes
      List<int> utf8Bytes = utf8.encode(str);
      return Uint8List.fromList(utf8Bytes);
    } catch (e) {
      print('Encoding error: $e');
      rethrow;
    }
  }

  /// Selects the alignment of the text
  ///
  /// The alignment can be either "left", "center", or "right"
  ///
  /// Returns the corresponding ESC POS command
  ESCPOS align(String align) {
    int num;
    switch (align) {
      case "center":
        num = 1;
        break;
      case "right":
        num = 2;
        break;
      case "left":
      default:
        num = 0;
        break;
    }
    _data += Uint8List.fromList([27, 97, num]);
    return this;
  }

  /// Sets the character size
  ///
  /// The parameter `size` ranges from 0 to 255, where bits 0 to 3 set the character height
  /// and bits 4 to 7 set the character width link : https://files.for-t.ru/Printers_receipts
  ESCPOS fontSize(ESCFontSize size) {
    _data += Uint8List.fromList([29, 33, size.value]);
    return this;
  }

  /// Sets the setBold
  ///
  /// n - 0-255, the lowest bit is 1, select bold; the lowest bit is 0, cancel bold
  /// and bits 1 to 0 set the setBold width link : https://files.for-t.ru/Printers_receipts
  ESCPOS setBold(int n) {
    _data += Uint8List.fromList([27, 69, n]);
    return this;
  }

  ESCPOS setCodePage(int codePage) {
    _data += Uint8List.fromList([27, 116, codePage]);
    return this;
  }

  ESCPOS setBarcodeWidth(int n) {
    _data += Uint8List.fromList([29, 119, n]);
    return this;
  }

  ESCPOS setBarcodeHeight(int n) {
    _data += Uint8List.fromList([29, 104, n]);
    return this;
  }

  ESCPOS selectHRICharacterPosition(int n) {
    _data += Uint8List.fromList([29, 72, n]);
    return this;
  }

  ESCPOS selectInternationalCharacterSets(int codePage) {
    _data += Uint8List.fromList([27, 82, codePage]);
    return this;
  }

  ESCPOS setPosition(int left, int right) {
    _data += [27, 36, left, right];
    return this;
  }

  ESCPOS setPageSize(bool is80mm) {
    _data += is80mm ? [29, 87, 0x80, 0x01] : [29, 87, 0x40, 0x02];
    return this;
  }

  // ESCPOS setPage76() {
  //   _data += [29, 87, 0x60, 0x02];
  //   return this;
  // }

  /// text for thai and vietnamese
  ///
  /// The text can be either "th", "vn"
  ///
  /// Returns the corresponding ESC POS command
  ESCPOS text(String text, String lang) {
    if (lang == "th") {
      _data += windows874.encode(text);
    } else {
      _data += viscii.encode(text);
    }
    return this;
  }

  ESCPOS cutSpace() {
    _data += Uint8List.fromList([29, 86, 0x42, 0x66]);
    return this;
  }

  ESCPOS cut() {
    _data += Uint8List.fromList([10, 10, 10, 10, 10, 29, 86, 48]);
    return this;
  }

  ESCPOS drawer() {
    _data += [27, 64, 27, 112, 0, 50, 250];
    return this;
  }

  ESCPOS beepDrawer() {
    _data += [27, 64, 27, 112, 0, 80, 80, 27, 66, 2, 3];
    return this;
  }

  ESCPOS beepDrawerCut() {
    _data += [27, 64, 29, 86, 66, 0, 27, 112, 0, 80, 80, 27, 66, 2, 3];
    return this;
  }

  ESCPOS beepKOT() {
    _data += [27, 64, 27, 66, 3, 3];
    return this;
  }

  ESCPOS beepInvoice() {
    _data += [27, 64, 27, 66, 2, 2, 2];
    return this;
  }

  ESCPOS getLangPrinter() {
    _data += [
      140,
      141,
      142,
      143,
      144,
      145,
      146,
      147,
      148,
      149,
      150,
      151,
      152,
      153,
      154,
      155,
      156,
      157,
      158,
      159,
      160,
      161,
      162,
      163,
      164,
      165,
      166,
      167,
      168,
      169,
      170,
      171,
      172,
      173,
      174,
      175,
      176,
      177,
      178,
      179,
      180,
      181,
      182,
      183,
      184,
      185,
      186,
      187,
      188,
      189,
      190,
      191,
      192,
      193,
      194,
      195,
      196,
      197,
      198,
      199,
      200,
      201,
      202,
      203,
      204,
      205,
      206,
      207,
      208,
      209,
      210,
      211,
      212,
      213,
      214,
      215,
      216,
      217,
      218,
      219,
      220,
      221,
      222,
      223,
      224,
      225,
      226,
      227,
      228,
      229,
      230,
      231,
      232,
      233,
      234,
      235,
      236,
      237,
      238,
      239,
      240,
      241,
      242,
      243,
      244,
      245,
      246,
      247,
      248,
      249,
      250,
      251,
      252,
      253,
      254,
      255,
      0,
      0,
      0,
      0,
      0,
      10,
      27,
      64,
      29,
      86,
      65,
      72,
      28,
      38,
      0
    ];
    return this;
  }

  ESCPOS getInfoPrinter() {
    _data += [31, 27, 31, 103];
    return this;
  }

  ESCPOS setThaiCharNET() {
    _data += [31, 27, 31, 255, 21, 10, 0];
    return this;
  }

  ESCPOS setThaiCharBLE() {
    _data += [31, 27, 31, 255, 70, 10, 0];
    return this;
  }

  ESCPOS restartPrinter() {
    _data += [27, 115, 66, 69, 146, 154, 1, 0, 95, 10];
    return this;
  }

  ESCPOS initialText() {
    _data += Uint8List.fromList([27, 64]);
    return this;
  }

  ESCPOS defineUserDefinedCharacters(int c1, int c2, Uint8List b) {
    final command = Uint8List.fromList([27, 38, 3, c1, c2]);
    _data += Uint8List.fromList([...command, ...b]);
    return this;
  }

  ESCPOS selectOrCancelCustomChar(int n) {
    // Create a Uint8List containing the byte data
    _data += Uint8List.fromList([27, 37, n]);
    return this;
  }

  ESCPOS cancelUserDefinedCharacters(int n) {
    // Create a Uint8List containing the byte data
    _data += Uint8List.fromList([27, 63, n]);
    return this;
  }

  /// Change font to custom font like '$'
  ESCPOS changeFontToCustom(String fontCode) {
    // Assuming custom font codes are defined by the printer manual
    _data += Uint8List.fromList([27, 38, fontCode.codeUnitAt(0)]);
    return this;
  }

  ESCPOS cancelChineseChar() {
    _data += Uint8List.fromList([28, 46]);
    return this;
  }

  ESCPOS feedLine() {
    _data += Uint8List.fromList([10]);
    return this;
  }

  ESCPOS rawByte(List<int> bytes) {
    _data += Uint8List.fromList(bytes);
    return this;
  }

  /// A line of dashes or any character
  ///
  /// The function creates a string of the specified character repeated to the
  /// desired length, converts it to bytes, and appends a newline character. example :lineDot({int length = 13, String type = "-"})
  ///
  /// Returns the resulting bytes
  ESCPOS lineDot({int length = 13, String type = "-"}) {
    // Create a string of the specified character repeated to the desired length
    String line = type * length;

    // Convert the string to bytes
    Uint8List lineBytes = strToBytes(line);

    // Create bytes for a newline character
    Uint8List newline = Uint8List.fromList([13, 10]);

    // Merge the line bytes with newline bytes
    _data += byteMerger(lineBytes, newline);
    return this;
  }
}

/// Enum representing font sizes for thermal printers
enum ESCFontSize {
  standard(0x00, '1x Height, 1x Width'),
  height2x(0x01, '2x Height, 1x Width'),
  height3x(0x02, '3x Height, 1x Width'),
  height4x(0x03, '4x Height, 1x Width'),
  width2x(0x10, '1x Height, 2x Width'),
  width3x(0x20, '1x Height, 3x Width'),
  width4x(0x30, '1x Height, 4x Width'),
  height2xWidth2x(0x11, '2x Height, 2x Width'),
  height2xWidth3x(0x12, '2x Height, 3x Width'),
  height2xWidth4x(0x13, '2x Height, 4x Width'),
  height3xWidth2x(0x21, '3x Height, 2x Width'),
  height3xWidth3x(0x22, '3x Height, 3x Width'),
  height3xWidth4x(0x23, '3x Height, 4x Width'),
  height4xWidth2x(0x31, '4x Height, 2x Width'),
  height4xWidth3x(0x32, '4x Height, 3x Width'),
  height4xWidth4x(0x33, '4x Height, 4x Width');

  final int value;
  final String description;

  const ESCFontSize(this.value, this.description);

  @override
  String toString() =>
      '$description (0x${value.toRadixString(16).toUpperCase()})';
}
