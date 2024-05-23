import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_charset_savanitdev/charset.dart';
import 'package:get/get.dart';
import 'package:image/image.dart' as img;
import 'package:flutter_savanitdev_printer/flutter_savanitdev_printer.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  File? _image;
  String? _base64Image;
  String _selectedType = 'NET'; // net,2 : ble,3:usb
  String _selectedLang = 'TH';
  String inputText = '';
  final _savanitdevPlugin = FlutterSavanitdevPrinter();
  String addressPrinter = '192.168.1.197';
  String codePageLang = '70'; // Thai : cp874 70,21 viet : viscii,tcvn 69,27
  bool status = false;
  List<dynamic> listPrinter = [];
  TextEditingController _controller =
      TextEditingController(text: '192.168.1.197');
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Uint8List useEncode(String text) {
    return windows874.encode(text);
  }

  autoChangeLang(String text) {}

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    try {
      if (Platform.isIOS) {
        await _savanitdevPlugin.initBLE();
      } else {
        await _savanitdevPlugin.onCreate();
      }
    } on PlatformException {}
  }

  void _onConnect() {
    try {
      if (_controller.text.isEmpty || addressPrinter.isEmpty) {
        Get.snackbar(
          "Alert",
          "Please check printer!",
          colorText: Colors.white,
          backgroundColor: Colors.lightBlue,
          icon: const Icon(Icons.add_alert),
        );
        return;
      }
      if (_selectedType == "NET") {
        _savanitdevPlugin.connectNet(addressPrinter).then((value) async {
          autoChangeLang(_selectedLang);
          setState(() {
            status = true;
          });
        });
      } else if (_selectedType == "BLE") {
        _savanitdevPlugin.connectBLE(addressPrinter).then((value) {
          Future.delayed(Duration(microseconds: 500), () {
            autoChangeLang(_selectedLang);
            setState(() {
              status = true;
            });
          });
        });
      } else {
        if (Platform.isAndroid) {
          _savanitdevPlugin.connectUSB(addressPrinter).then((value) async {
            autoChangeLang(_selectedLang);
            setState(() {
              status = true;
            });
          });
        }
      }
    } catch (e) {
      print(e);
    }
  }

  _onDisconnect(String value) async {
    try {
      if (value == "NET" && status) {
        await _savanitdevPlugin.disconnectNet(_controller.text.toString());
        final mac = value == "NET" ? addressPrinter = _controller.text : "";
        setState(() {
          listPrinter = [];
          addressPrinter = mac;
          this._selectedType = value.toString();
          status = false;
        });
      } else if (value == "BLE" && status) {
        _savanitdevPlugin.disconnectBLE().then((res) {
          final mac = value == "NET" ? addressPrinter = _controller.text : "";
          setState(() {
            listPrinter = [];
            addressPrinter = mac;
            this._selectedType = value.toString();
            status = false;
          });
        });
      } else {
        if (Platform.isAndroid && status) {
          _savanitdevPlugin.disconnectBLE().then((res) async {
            final mac = value == "NET" ? addressPrinter = _controller.text : "";
            setState(() {
              listPrinter = [];
              addressPrinter = mac;
              this._selectedType = value.toString();
              status = false;
            });
          });
        } else {
          setState(() {
            listPrinter = [];
            this._selectedType = value.toString();
          });
        }
      }
    } catch (e) {
      print("error conenct ${e}");
      Get.snackbar(
        "Alert",
        "Please check printer",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
    }
  }

  void _onScanPrinter() {
    if (_selectedType == "BLE") {
      _savanitdevPlugin.findAvailableDevice().then((value) {
        List<dynamic> arr = [];
        value?.forEach((data) {
// Remove the curly braces and split the string by commas
          List<String> parts = data
              .toString()
              .substring(1, data.toString().length - 1)
              .split(', ');
          Map<String, dynamic> resultMap = {};
          parts.forEach((part) {
            List<String> keyValue = part.split(': ');
            resultMap[keyValue[0]] = keyValue[1];
          });

          arr.add(resultMap);
        });

        setState(() {
          listPrinter = arr;
        });
      });
    } else {
      if (Platform.isAndroid) {
        _savanitdevPlugin.getUSB().then((value) {
          final path = value.toString().replaceAll("[", "").replaceAll("]", "");
          List<dynamic> arr = [];
          arr.add(path.toString());
          setState(() {
            listPrinter = arr;
            addressPrinter = path.toString();
          });
        });
      }
    }
  }

  void _onButtonPrintRaw() async {
    if (addressPrinter.isEmpty || status == false) {
      Get.snackbar(
        "Alert",
        "Please connect printer first!",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }

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
  }

  void _onButtonPrintText() async {
    if (addressPrinter.isEmpty || status == false) {
      Get.snackbar(
        "Alert",
        "Please connect printer first!",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }
    if (inputText.isEmpty) {
      Get.snackbar(
        "Alert",
        "Please input your text",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }

    final profile = await CapabilityProfile.load();
    final generator = Generator(PaperSize.mm80, profile);
    List<int> bytes = [];

    try {
      bytes += generator.textEncoded(useEncode(inputText));
    } catch (e) {
      Get.snackbar(
        "Alert use not correct language",
        "this library just support Thai,vietnam and english, if you want you use other languages please contact me : https://www.facebook.com/SavanitDev",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }
    bytes +=
        generator.textEncoded(useEncode("https://www.facebook.com/SavanitDev"));

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
  }

  void _onButtonPremissionUSB() {
    if (addressPrinter.isEmpty || status == false) {
      Get.snackbar(
        "Alert",
        "Please connect printer first!",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }
    _savanitdevPlugin.tryGetUsbPermission().then((value) {
      // print("tryGetUsbPermission -> ${value.toString()}");
    });
  }

  void _onButtonBlePermission() async {
    if (addressPrinter.isEmpty || status == false) {
      Get.snackbar(
        "Alert",
        "Please connect printer first!",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }
    await _savanitdevPlugin.reqBlePermission();
  }

  void _onPrintImg() {
    if (addressPrinter.isEmpty || status == false) {
      Get.snackbar(
        "Alert",
        "Please connect printer first!",
        colorText: Colors.white,
        backgroundColor: Colors.lightBlue,
        icon: const Icon(Icons.add_alert),
      );
      return;
    }

    _savanitdevPlugin.image64BaseBLE(_base64Image!, 576, 0).then((value) {
      print("image64BaseBLE -> ${value.toString()}");
    });
  }

  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedImage = await picker.pickImage(source: ImageSource.gallery);
    setState(() {
      if (pickedImage != null) {
        _image = File(pickedImage.path);
        _convertImageToBase64();
      } else {
        print('No image selected.');
      }
    });
  }

  Future<void> _convertImageToBase64() async {
    if (_image != null) {
      List<int> imageBytes = await _image!.readAsBytes();
      setState(() {
        _base64Image = base64Encode(imageBytes);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Savanitdev Printer'),
        ),
        body: Center(
          child: Builder(builder: (context) {
            return ListView(
              children: [
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: ElevatedButton(
                    onPressed: () async {
                      final Uri url =
                          Uri.parse('https://www.facebook.com/SavanitDev');
                      if (!await launchUrl(url)) {
                        throw Exception('Could not launch');
                      }
                    },
                    child: const Text('contact support'),
                  ),
                ),
                Center(
                    child: Text(
                  "Status : ${status && addressPrinter.isNotEmpty ? "Connected" : "No connect"}",
                  style: TextStyle(color: status ? Colors.green : Colors.red),
                )),
                Wrap(
                  alignment: WrapAlignment.center,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: DropdownButton<String>(
                        value: _selectedType.isEmpty ? "NET" : _selectedType,
                        items: <String>[
                          'NET',
                          'BLE',
                          'USB',
                        ].map((String val) {
                          return DropdownMenuItem<String>(
                            value: val,
                            child: Text(val),
                          );
                        }).toList(),
                        onChanged: (String? value) {
                          _onDisconnect(value.toString());
                        },
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: DropdownButton<String>(
                        value: _selectedLang,
                        items: <String>[
                          'TH',
                          'VN',
                        ].map((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(value),
                          );
                        }).toList(),
                        onChanged: (String? value) {
                          autoChangeLang(value.toString());
                          setState(() {
                            _selectedLang = value.toString();
                          });
                        },
                      ),
                    ),
                    _selectedType == "NET"
                        ? Container(
                            width: 300,
                            height: 40,
                            alignment: Alignment.centerLeft,
                            child: TextField(
                              controller: _controller,
                              onChanged: (text) {
                                setState(() {
                                  addressPrinter = text;
                                });
                              },
                              decoration: InputDecoration(
                                hintText: 'Enter your text here',
                                labelText: 'address ${_selectedType}',
                                border: const OutlineInputBorder(),
                              ),
                            ),
                          )
                        : Container(
                            width: 300,
                            child: Column(
                              children: [
                                const Text("List printer"),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: List.generate(listPrinter.length,
                                      (index) {
                                    final data = listPrinter[index];

                                    return InkWell(
                                        onTap: () {
                                          setState(() {
                                            addressPrinter =
                                                data["deviceAddress"]
                                                    .toString();
                                          });
                                        },
                                        child: Padding(
                                          padding: const EdgeInsets.all(8.0),
                                          child: Text(
                                            textAlign: TextAlign.left,
                                            _selectedType == "BLE"
                                                ? "${addressPrinter == data["deviceAddress"].toString() ? "✅" : ""} ${data["name"].toString()} : ${data["deviceAddress"].toString()}"
                                                : "${data.toString().isNotEmpty && addressPrinter == data.toString() ? "✅" : ""} ${data.toString()}",
                                          ),
                                        ));
                                  }),
                                ),
                              ],
                            )),
                    _selectedType != "NET"
                        ? Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: ElevatedButton(
                              onPressed: _onScanPrinter,
                              child: const Text('scan'),
                            ),
                          )
                        : Container(),
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: ElevatedButton(
                        onPressed: _onConnect,
                        child: const Text('connect'),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: ElevatedButton(
                        onPressed: () async {
                          await _onDisconnect(_selectedType);
                        },
                        child: const Text('disconnect'),
                      ),
                    ),
                  ],
                ),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: SizedBox(
                    width: 240, // <-- TextField width
                    height: 120, // <-- TextField height
                    child: TextField(
                      maxLines: null,
                      expands: true,
                      keyboardType: TextInputType.multiline,
                      decoration: InputDecoration(
                          filled: true, hintText: 'Enter text here'),
                      onChanged: (String? value) {
                        setState(() {
                          inputText = value.toString();
                        });
                      },
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: ElevatedButton(
                    onPressed: _onButtonPrintText,
                    child: const Text('print text'),
                  ),
                ),
                Center(child: SizedBox(height: 20)),
                _image == null
                    ? Center(child: Text('No image selected.'))
                    : Image.file(_image!, width: 400, height: 400),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: ElevatedButton(
                    onPressed: _pickImage,
                    child: const Text('choose image'),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: ElevatedButton(
                    onPressed: _onPrintImg,
                    child: const Text('image print'),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: ElevatedButton(
                    onPressed: _onButtonPrintRaw,
                    child: const Text('print template Rawdata'),
                  ),
                ),
                Platform.isAndroid
                    ? Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: ElevatedButton(
                          onPressed: _onButtonPremissionUSB,
                          child: const Text('request Usb Permission'),
                        ),
                      )
                    : const Text(""),
                Platform.isAndroid
                    ? Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: ElevatedButton(
                          onPressed: _onButtonBlePermission,
                          child: const Text('request Bluetooth Permission'),
                        ),
                      )
                    : const Text(""),
              ],
            );
          }),
        ),
      ),
    );
  }
}
