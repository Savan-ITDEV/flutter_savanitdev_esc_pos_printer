# flutter_savanitdev_printer

## Laos

สำหรับภาษาลาวสามารถใช้งานได้แต่ต้องเป็นปริ้นเตอร์ที่รองรับภาษาลาวเท่านั้นก็ขอบใจ ^^

## Thai

สวัสดีครับผมที่เป็น library ที่ผมสร้างขึ้นมาเพื่อตอบสนองต่อผู้ใช้งานที่ต้องการปริ้นภาษาไทยและภาษาเวียดนามถ้าหากว่าใช้งานพบเจอปัญหาสามารถแจ้งผมมาได้เลยนะครับผมขอขอบคุณครับ ^^

## VietNam

Xin chào, đây là thư viện tôi tạo ra để đáp ứng cho người dùng muốn in tiếng Thái và tiếng Việt. Nếu bạn gặp bất kỳ vấn đề gì khi sử dụng, bạn có thể thông báo cho tôi. Tôi xin cảm ơn bạn ^^.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

[Link React Native](https://www.npmjs.com/package/react-native-savanitdev-thermal-printer)

## Alert

- currently I config just support for Thai languages and Vietnam
  📍 if you need print more languages please contact me ^^

## Support

| Expo | ✅  
| React Native CLI | ✅
| Flutter CLI | ✅

| Implement            | Android | IOS |
| -------------------- | ------- | --- |
| imageBase64          | ✅      | ✅  |
| print check printer  | ✅      | ✅  |
| cancel Chinese       | ✅      | ✅  |
| set code page        | ✅      | ✅  |
| print test code page | ✅      | ✅  |

auto change languages Thai and Vietnam ✅: Full option [contact me](https://www.facebook.com/SavanitDev)
print info printer ✅: Full option [contact me](https://www.facebook.com/SavanitDev)
set codePage ✅: Full option [contact me](https://www.facebook.com/SavanitDev)
need more languages ✅ [contact me](https://www.facebook.com/SavanitDev)

## Support

| Printer   | Android | IOS |
| --------- | ------- | --- |
| BLUETOOTH | ✅      | ✅  |
| Net       | ✅      | ✅  |
| USB       | ✅      | ❌  |

<br />
<div style="display: flex; flex-direction: row; align-self: center; align-items: center">
<img src="https://i.ibb.co/GtHcCDb/Screenshot-1716456174.png" alt="screenshot" width="300" height="550"/>
<img src="https://i.ibb.co/2sK4qCw/Screenshot-1716456182.png" alt="bill" width="300" height="550"/>
<img src="https://i.ibb.co/Rg6VfkW/photo-6145642264483971071-y.jpg" alt="bill" width="370" height="550"/>
<img src="https://i.ibb.co/2j57z5z/photo-6145642264483971060-y.jpg" alt="screenshot" width="370" height="620"/>
</div>

[Link download APK test app](https://drive.google.com/file/d/1sl5MhzqL78LQynNDHnRE5d3Rw3SRF2aP/view?usp=sharing)

How to install package Android

1. Copy this code to AndroidManfest.xml
   <uses-permission android:name="android.hardware.usb.UsbAccessory"/>
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
   <uses-permission android:name="android.permission.BLUETOOTH"/>
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
   <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE"/>
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
   <uses-permission android:name="android.permission.INTERNET"/>
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
   <uses-permission android:name="android.permission.RECORD_AUDIO"/>
   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
   <uses-permission android:name="android.permission.VIBRATE"/>
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
   <uses-feature android:name="android.hardware.usb.host" android:required="true"/>

 <service android:name="net.posprinter.service.PosprinterService"/>

 <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"/>

 <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" android:resource="@xml/device_filter"/>

- Create folder xml/device_filter.xml

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 0x0403 / 0x60??: FTDI -->
    <usb-device vendor-id="1027" product-id="24577" /> <!-- 0x6001: FT232R -->
    <usb-device vendor-id="1027" product-id="24592" /> <!-- 0x6010: FT2232H -->
    <usb-device vendor-id="1027" product-id="24593" /> <!-- 0x6011: FT4232H -->
    <usb-device vendor-id="1027" product-id="24596" /> <!-- 0x6014: FT232H -->
    <usb-device vendor-id="1027" product-id="24597" /> <!-- 0x6015: FT231X -->

    <!-- 0x10C4 / 0xEA??: Silabs CP210x -->
    <usb-device vendor-id="4292" product-id="60000" /> <!-- 0xea60: CP2102 -->
    <usb-device vendor-id="4292" product-id="60016" /> <!-- 0xea70: CP2105 -->
    <usb-device vendor-id="4292" product-id="60017" /> <!-- 0xea71: CP2108 -->
    <usb-device vendor-id="4292" product-id="60032" /> <!-- 0xea80: CP2110 -->

    <!-- 0x067B / 0x2303: Prolific PL2303 -->
    <usb-device vendor-id="1659" product-id="8963" />

    <!-- 0x1a86 / 0x?523: Qinheng CH34x -->
    <usb-device vendor-id="6790" product-id="21795" /> <!-- 0x5523: CH341A -->
    <usb-device vendor-id="6790" product-id="29987" /> <!-- 0x7523: CH340 -->

    <!-- CDC driver -->
    <usb-device vendor-id="9025" />                   <!-- 0x2341 / ......: Arduino -->
    <usb-device vendor-id="5824" product-id="1155" /> <!-- 0x16C0 / 0x0483: Teensyduino  -->
    <usb-device vendor-id="1003" product-id="8260" /> <!-- 0x03EB / 0x2044: Atmel Lufa -->
    <usb-device vendor-id="7855" product-id="4"    /> <!-- 0x1eaf / 0x0004: Leaflabs Maple -->
    <usb-device vendor-id="3368" product-id="516"  /> <!-- 0x0d28 / 0x0204: ARM mbed -->
    <!--    zywell-->
    <usb-device vendor-id="1155" product-id="22339"  />

</resources>

Setup IOS
Add below code into info.plist

- <key>NSBluetoothAlwaysUsageDescription</key>
- <string>we needed to discover nearby devices, such as a thermal printer and print receipts.</string>
- <key>NSBluetoothPeripheralUsageDescription</key>
- <string>We needed to discover nearby devices, connect to a thermal printer and print receipts.</string>

- install package for test : 1.[esc_pos_utils_plus](https://pub.dev/packages/esc_pos_utils_plus) 2.[flutter_charset_savanitdev](https://pub.dev/packages/flutter_charset_savanitdev/example)

## Generate a Ticket

### Simple ticket with styles:

```dart
  final profile = await CapabilityProfile.load();
    final generator = Generator(PaperSize.mm80, profile);
    List<int> bytes = [];

    final ByteData data = await rootBundle.load('assets/images/logo.png');
    final Uint8List imgBytes = data.buffer.asUint8List();
    final img.Image image = img.decodeImage(imgBytes)!;

    bytes +=
        generator.imageRaster(img.copyResize(image, width: 130, height: 130));
    bytes += generator.text(" ");
    bytes += generator.text("SavanitDev Shop",
        styles: PosStyles(
            align: PosAlign.center, bold: true, height: PosTextSize.size1));
    bytes += generator.text(" ");
    bytes += generator.textEncoded(
        useEncode(
            _selectedLang == "TH" ? "หมายเลขโต๊ะ : 12345" : "số bàn : 12345"),
        styles: PosStyles(align: PosAlign.center));
    bytes += generator.text(" ");
    bytes += generator.textEncoded(
        useEncode(
            _selectedLang == "TH" ? "วันที่ 25/05/2024" : "ngày 25/05/2024"),
        styles: PosStyles(align: PosAlign.center));
    bytes += generator.text(" ");

    bytes += generator.row([
      PosColumn(
          textEncoded: useEncode(_selectedLang == "TH" ? "สินค้า" : "sản phẩm"),
          width: 7,
          styles: PosStyles(align: PosAlign.left)),
      PosColumn(
          textEncoded: useEncode(_selectedLang == "TH" ? "จำนวน" : "Số lượng"),
          width: 3,
          styles: PosStyles(align: PosAlign.left)),
      PosColumn(
          textEncoded: useEncode(_selectedLang == "TH" ? "ราคา" : "giá"),
          width: 2,
          styles: PosStyles(align: PosAlign.right)),
    ]);
    final arr = [
      {
        "name": "ปอเปี๊ยะทอด",
        "qty": "2",
        "price": "1000.00",
      },
      {
        "name": "อาหารไทย หมี่กรอบ",
        "qty": "2",
        "price": "45.00",
      },
      {
        "name": "กุ้งลายโสร่ง,ประทัดลม,ทอดมันกุ้ง",
        "qty": "2",
        "price": "500.00",
      },
      {
        "name": "หมู, เนื้อผึ่งแดดทอด",
        "qty": "2",
        "price": "10500.00",
      },
    ];
    for (var i = 0; i < arr.length; i++) {
      final rep = arr[i];
      bytes += generator.row([
        PosColumn(
            textEncoded: useEncode(_selectedLang == "TH"
                ? rep["name"].toString()
                : "món ăn việt nam"),
            width: 7,
            styles: PosStyles(align: PosAlign.left)),
        PosColumn(
            text: rep["qty"].toString(),
            width: 2,
            styles: PosStyles(align: PosAlign.left)),
        PosColumn(
            textEncoded: useEncode(_selectedLang == "TH"
                ? "${rep["price"].toString()} บาท"
                : "100.000 đồng"),
            width: 3,
            styles: PosStyles(align: PosAlign.right)),
      ]);
    }
    bytes += generator.row([
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "ทั้งหมด" : "tổng cộng"),
          width: 5,
          styles: PosStyles(align: PosAlign.left)),
      PosColumn(
        text: "",
        width: 1,
        styles: PosStyles(align: PosAlign.center),
      ),
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "500 บาท" : "100.000 đồng"),
          width: 6,
          styles: PosStyles(align: PosAlign.right)),
    ]);
    bytes += generator.row([
      PosColumn(
          textEncoded: useEncode(_selectedLang == "TH" ? "ลดราคา" : "giảm giá"),
          width: 5,
          styles: PosStyles(align: PosAlign.left)),
      PosColumn(
        text: "",
        width: 1,
        styles: PosStyles(align: PosAlign.center),
      ),
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "500 บาท" : "100.000 đồng"),
          width: 6,
          styles: PosStyles(align: PosAlign.right)),
    ]);
    bytes += generator.row([
      PosColumn(
          textEncoded: useEncode(
              _selectedLang == "TH" ? "ค่าบริการ 10%" : "Phí dịch vụ 10%"),
          width: 5,
          styles: PosStyles(align: PosAlign.left)),
      PosColumn(
        text: "",
        width: 1,
        styles: PosStyles(align: PosAlign.center),
      ),
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "50 บาท" : "10.000 đồng"),
          width: 6,
          styles: PosStyles(align: PosAlign.right)),
    ]);
    bytes += generator.row([
      PosColumn(
        text: "VAT 7%",
        width: 5,
        styles: PosStyles(align: PosAlign.left),
      ),
      PosColumn(
        text: "",
        width: 1,
        styles: PosStyles(align: PosAlign.center),
      ),
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "50 บาท" : "10.000 đồng"),
          width: 6,
          styles: PosStyles(align: PosAlign.right)),
    ]);
    bytes += generator.hr(ch: "-");
    bytes += generator.row([
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "ทั้งหมด" : "tổng cộng"),
          width: 5,
          styles: PosStyles(align: PosAlign.left, bold: true)),
      PosColumn(
        text: "",
        width: 1,
        styles: PosStyles(align: PosAlign.center),
      ),
      PosColumn(
          textEncoded:
              useEncode(_selectedLang == "TH" ? "50 บาท" : "10.000 đồng"),
          width: 6,
          styles: PosStyles(align: PosAlign.right, bold: true)),
    ]);
    bytes += generator.hr(ch: "-");
    bytes += generator.text(" ");
    bytes += generator.text("Scan QR code for visit my Facebook",
        styles: PosStyles(align: PosAlign.center));
    bytes += generator.text(" ");
    bytes += generator.qrcode("https://www.facebook.com/SavanitDev",
        size: QRSize.Size8);

    bytes += generator.text(" ");
    bytes += generator.hr(ch: ".");
    bytes += generator.text(" ");
    bytes +=
        generator.text("Thank you", styles: PosStyles(align: PosAlign.center));

    bytes += generator.textEncoded(
        useEncode(_selectedLang == "TH"
            ? "สร้าง library นี้โดย"
            : "tạo thư viện này bởi"),
        styles: PosStyles(align: PosAlign.center));
    bytes += generator.textEncoded(
        useEncode("https://www.facebook.com/SavanitDev"),
        styles: PosStyles(align: PosAlign.center));

    bytes += generator.feed(2);
    bytes += generator.cut();
    String base64String = base64Encode(bytes);

    if (_selectedType == "NET") {
      await _savanitdevPlugin.printRawData(addressPrinter, base64String, false);
    } else if (_selectedType == "BLE") {
      await _savanitdevPlugin.rawDataBLE(base64String);
    } else {
      if (Platform.isAndroid) {
        await _savanitdevPlugin.printRawData(
            addressPrinter, base64String, false);
      }
    }

```

## Support Full Options printer or give me coffee 🔰🔰🔰

<div style="display: flex; flex-direction: row; align-self: center; align-items: center">

BIDV ViteNam
<img src="https://i.ibb.co/HHJrJKL/photo-6231225951782550476-y.jpg" alt="bill" width="300" height="300"/>

OnePay BCEL Laos
<img src="https://i.ibb.co/ykdLgwm/photo-6231225951782550469-y.jpg" alt="bill" width="300" height="300"/>

Binance Pay
<img src="https://i.ibb.co/BLvK9QV/photo-6231225951782550475-y.jpg" alt="bill" width="300" height="300"/>

USDT (TRC20) : THkJtfAKZVCvTxifDGa4NLZQrUJYbUZ2Rs
<img src="https://i.ibb.co/brkwkCC/photo-6231225951782550474-x.jpg" alt="bill" width="300" height="300"/>

</div>

## Support Me by give Star ⭐️⭐️⭐️⭐️⭐️⭐️⭐️⭐️⭐️⭐️

<img alt="Star the SavanitDev repo on GitHub to support the project" src="https://user-images.githubusercontent.com/9664363/185428788-d762fd5d-97b3-4f59-8db7-f72405be9677.gif" width="50%">

## FAQ Support 🔰🔰🔰

you can contact me directly [Telegram](@dev_la), feedback your problem

<img src="https://i.ibb.co/vc8HjFW/dev-la.jpg">

or ✉️

Telegram : @dev_la
Fanpage : https://www.facebook.com/SavanitDev

Thank you guys
