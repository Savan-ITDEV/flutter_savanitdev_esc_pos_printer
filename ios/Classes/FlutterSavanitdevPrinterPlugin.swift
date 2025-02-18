import Flutter
import Darwin
import UIKit
import Foundation


class PrinterManager {
    var connectedPrinterMap: [String: WIFIConnecter] = [:]
   
    func addPrinter(id: String, printer: WIFIConnecter) {
        connectedPrinterMap[id] = printer
        printer.connect(withHost: id, port: 9100)
    }
   
    func removePrinter(id: String) {
        connectedPrinterMap.removeValue(forKey: id)
    }
   
    func getPrinter(id: String) -> WIFIConnecter? {
        return connectedPrinterMap[id]
    }
   
    func listPrinters() -> [String: WIFIConnecter] {
        return connectedPrinterMap
    }
}

public class FlutterSavanitdevPrinterPlugin:  NSObject, POSWIFIManagerDelegate,WIFIConnecterDelegate,CBCentralManagerDelegate,POSBLEManagerDelegate,FlutterPlugin {
   
    var rssiList: [NSNumber] = []
    var dataArr: [CBPeripheral] = []
    var startTime: Date?
    var centralManager: CBCentralManager!
    var addressCurrent = "";
    var btManager: POSBLEManager!
    var statusBLE :Bool=false
    var setPrinter = Data();
    let printerManager = PrinterManager()
    var resultMethod: FlutterResult?
   
    @objc
    func initPrinter() {
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        self.btManager = POSBLEManager.sharedInstance()
        self.btManager.delegate = self
    }
   
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_savanitdev_printer", binaryMessenger: registrar.messenger())
        let instance = FlutterSavanitdevPrinterPlugin()
        instance.initPrinter()
        registrar.addMethodCallDelegate(instance, channel: channel)
      
    }
   
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let args = call.arguments as? [String: Any] else {
            result(FlutterError(code: "ERROR_CODE", message: "Invalid arguments", details: "Arguments must be a dictionary"))
            return
        }

        switch call.method {
        case "connectMultiXPrinter":
            guard let address = args["address"] as? String,
                  let type = args["type"] as? String else {
                result(FlutterError(code: "ERROR_CODE", message: "Invalid parameters", details: "Missing address or portType"))
                return
            }
            connectMultiXPrinter(address, portType: type, result: result)
           
        case "disconnectXPrinter":
            guard let address = args["address"] as? String else {
                result(FlutterError(code: "ERROR_CODE", message: "Invalid parameters", details: "Missing address"))
                return
            }
            disconnectXPrinter(address, result: result)
           
        case "printRawDataESC":
            guard let address = args["address"] as? String,
                  let encode = args["encode"] as? String else {
                result(FlutterError(code: "ERROR_CODE", message: "Invalid parameters", details: "Missing address or data"))
                return
            }
            printRawDataESC(address, base64String: encode, result: result)
           
        case "printImgESCX":
            guard let address = args["address"] as? String,
                  let encode = args["encode"] as? String else {
                result(FlutterError(code: "ERROR_CODE", message: "Invalid parameters", details: "Missing address or imageData"))
                return
            }
            printImgESCX(address, base64String: encode, width: args["width"] as? Int ?? 0, result: result)
           
        case "getListDevice":
            getListDevice(result)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
   
   
    // MARK: - WIFIConnecterDelegate Methods
    public func wifiPOSConnected(toHost ip: String, port: UInt16, mac: String) {
        print("WiFi printer connected - IP: \(ip), Port: \(port), MAC: \(mac)")
        resultMethod?("CONNECTED")
        resultMethod = nil
    }
   
    public func wifiPOSDisconnectWithError(_ error: Error?, mac: String, ip: String) {
        print("WiFi printer disconnected - IP: \(ip), MAC: \(mac)")
       
        if let printer = printerManager.getPrinter(id: ip) {
            printer.disconnect()
            printerManager.removePrinter(id: ip)
           
            if let error = error {
                resultMethod?(FlutterError(code: "ERROR_CODE",
                                       message: "DISCONNECT_ERROR",
                                       details: "Disconnected with error: \(error.localizedDescription)"))
            } else {
                resultMethod?("Printer disconnected normally")
            }
        } else {
            resultMethod?(FlutterError(code: "ERROR_CODE",
                                   message: "DISCONNECT_FAIL",
                                   details: "Failed to disconnect: printer not found at \(ip)"))
        }
        resultMethod = nil
    }
   
    public func wifiPOSWriteValue(withTag tag: Int, mac: String, ip: String) {
        guard let printer = printerManager.getPrinter(id: ip) else {
            resultMethod!(FlutterError(code: "ERROR_CODE",
                              message: "PRINTER_NOT_FOUND",
                              details: "No printer found at address: \(ip)"))
            return
        }
        printer.disconnect()
        printerManager.removePrinter(id: ip)
        sleep(1)
        if tag > 0 {
               print("Print job \(tag) completed successfully.")
            resultMethod?("STS_NORMAL")
           } else {
               print("Print job failed.")
               resultMethod!(FlutterError(code: "ERROR_CODE",
                                 message: "PRINT_FAIL",
                                 details: "Print job failed. \(ip)"))
           }
    }
   
    public func wifiPOSReceiveValue(for data: Data, mac: String, ip: String) {
        print("WiFi printer data received - IP: \(ip), MAC: \(mac), Data size: \(data.count) bytes")
    }
   
   
    @objc
    func connectMultiXPrinter(_ address:String, portType:String, result: @escaping FlutterResult) -> Void {
        if portType == "bluetooth" {
            guard statusBLE else {
                result(FlutterError(code: "ERROR_CODE",
                                  message: "BLUETOOTH_DISABLED",
                                  details: "Bluetooth is not enabled on this device"))
                return
            }
            // Start scanning for Bluetooth devices
            self.btManager.startScan()
            self.resultMethod = result
            connectBLE(address,result: result)
        } else {
            connectNet(address, result: result)
        }
    }
   
    @objc
    func connectNet(_ address: String, result: @escaping FlutterResult) -> Void {
        do {
            if printerManager.getPrinter(id: address) != nil {
                print("Printer already connected")
                result("CONNECTED")
            } else {
                resultMethod = result
                guard !address.isEmpty else {
                    result(FlutterError(code: "ERROR_CODE",
                                      message: "INVALID_ADDRESS",
                                      details: "Printer address cannot be empty"))
                    return
                }
           
                // Add a printer with an ID
                let printer = WIFIConnecter()
                printer.delegate = self
                printerManager.addPrinter(id: address, printer: printer)
                print("Added new printer connection")
            }
        } catch let error {
            print("Connection error for printer at \(address): \(error.localizedDescription)")
            result(FlutterError(code: "ERROR_CODE",
                              message: "CONNECT_ERROR",
                              details: "Failed to connect: \(error.localizedDescription)"))
        }
    }
    @objc
    func connectBLE(_ identifiers :String, result: @escaping FlutterResult) {
        resultMethod = result
                if let peripheral = dataArr.first(where: { $0.identifier.uuidString == identifiers }){
                  print("connectBLE")
                    if(addressCurrent == peripheral.identifier.uuidString){
                        result("CONNECTED")
                    }else{
                        self.btManager.connectDevice(peripheral);
                    }
                }else{
                    self.btManager.startScan();
                    result(FlutterError(code: "ERROR_CODE",
                                      message: "INVALID_ADDRESS",
                                      details: "Printer address cannot be empty"))
                }
              
        }
   
   
   
    @objc
    func disconnectXPrinter(_ address: String, result: @escaping FlutterResult) {
        guard !address.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_ADDRESS",
                              details: "Printer address cannot be empty"))
            return
        }
        resultMethod = result
        if isValidIPAddress(address) {
            disconnectNet(address,result: result)
        }else{
            disconnectBT(result: result)
        }
     
    }
   
    @objc
    func disconnectBT(result: @escaping FlutterResult) {
           if(self.statusBLE == true){
               resultMethod = result
               if(self.btManager.printerIsConnect()){
                   self.btManager.disconnectRootPeripheral();
               }else{
                   result("DISCONNTED");
               }
           }else{
                      result(FlutterError(code: "ERROR_CODE",
                                        message: "BT_NOT_ENABLE",
                                          details: "please enable bluetooth"))
           }
       }
   
   
   
    @objc
    func disconnectNet(_ address: String, result: @escaping FlutterResult) {
        guard !address.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_ADDRESS",
                              details: "Printer address cannot be empty"))
            return
        }
        resultMethod = result
        guard let printer = printerManager.getPrinter(id: address) else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "PRINTER_NOT_FOUND",
                              details: "No printer found at address: \(address)"))
            return
        }
        printer.disconnect()
        print("Disconnected printer at: \(address)")
    }
    
    @objc
    func printImgESCX(_ address: String, base64String: String, width: Int, result: @escaping FlutterResult) {
        resultMethod = result
        guard !address.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_ADDRESS",
                              details: "Printer address cannot be empty"))
            return
        }
       
        guard !base64String.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_DATA",
                              details: "Image data cannot be empty"))
            return
        }
        
        guard let imageData = Data(base64Encoded: base64String) else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "ENCODE_ERROR",
                              details: "Failed to decode base64 image data"))
            return
        }
        guard let image = UIImage(data: imageData) else {
               result(FlutterError(code: "ERROR_CODE", message: "CONVERT_IMG_ERROR", details: "Failed to create image from data"))
               return
       }
        guard let img = self.monoImg(image: image, threshold: 0.1) else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "CONVERT_IMG_ERROR",
                              details: "Failed to convert image to monochrome"))
            return
        }
    
        let initializePrinter: Data = POSCommand.initializePrinter()
        let align: Data = POSCommand.selectAlignment(1)
        let imgData: Data = POSCommand.printRasteBmp(withM: RasterNolmorWH, andImage: img, andType: Dithering)
        let cut: Data = POSCommand.selectCutPageModelAndCutpage(withM: 1, andN: 1)
        let spaceH1 = Data("    ".utf8)
        let spaceH2 = Data("    ".utf8)
        let concatenatedData = initializePrinter  + align + imgData + cut + spaceH1 + spaceH2
       
        if isValidIPAddress(address) {
            printByteNet(address,data: concatenatedData,result: result)
        }else{
            printByteBLE(concatenatedData,result: result)
        }
    }
    @objc
    func printRawDataESC(_ address: String, base64String: String, result: @escaping FlutterResult) {
        guard !address.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_ADDRESS",
                              details: "Printer address cannot be empty"))
            return
        }
       
        guard !base64String.isEmpty else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_DATA",
                              details: "Print data cannot be empty"))
            return
        }
       
        guard let data = Data(base64Encoded: base64String) else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "INVALID_BASE64",
                              details: "Failed to decode base64 data"))
            return
        }
        resultMethod = result
        if isValidIPAddress(address) {
            printByteNet(address,data: data,result: result)
        }else{
            printByteBLE(data,result: result)
        }
    }
   
    @objc
    func printByteNet(_ address: String, data :Data,result: @escaping FlutterResult) {
        guard let printer = printerManager.getPrinter(id: address) else {
            result(FlutterError(code: "ERROR_CODE",
                                message: "PRINTER_NOT_FOUND",
                                details: "No printer found at address: \(address)"))
            return
        }
        printer.writeCommand(with: data)
    }
    @objc
    func printByteBLE(_ data :Data,result: @escaping FlutterResult) {
           if(self.statusBLE == true){
              self.btManager.writeCommand(with: data)
           }else{
               result(FlutterError(code: "ERROR_CODE",
                message: "BT_NOT_ENABLE",
                 details: "please enable bluetooth"))
           }
       }
   
    @objc
    func statusXprinter(address: String, result: @escaping FlutterResult) {
        guard let printer = printerManager.getPrinter(id: address) else {
            result(FlutterError(code: "ERROR_CODE",
                              message: "PRINTER_NOT_FOUND",
                              details: "No printer found at address: \(address)"))
            return
        }
        printer.disconnect()
//         printerManager.removePrinter(id: address)
        sleep(1)
        printer.printerStatus { (responseData: Data?) in
            let byte = [UInt8](responseData!)
            let status = byte[0]
           
            switch status {
            case 0x12:
                print("Ready")
                result("STS_NORMAL")
               
            case 0x16:
                print("Cover opened")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_COVEROPEN",
                                  details: "Printer cover is open. Please close the cover and try again"))
               
            case 0x32:
                print("Paper end")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_PAPEREMPTY",
                                  details: "Printer is out of paper. Please add paper and try again"))
               
            case 0x36:
                print("Cover opened & Paper end")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_COVEROPEN_STS_PAPEREMPTY",
                                  details: "Printer cover is open and out of paper. Please close cover and add paper"))
               
            default:
                print("Error: Unknown status code \(String(format: "0x%02X", status))")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "PRINT_FAIL",
                                  details: "Unknown printer status: \(String(format: "0x%02X", status))"))
            }
        }
    }
   
    @objc
    func statusBTXprinter(printer: POSBLEManager, result: @escaping FlutterResult) {
        self.btManager.printerStatus { (responseData: Data?) in
            guard let data = responseData else {
                result(FlutterError(code: "ERROR_CODE",
                                  message: "NO_RESPONSE",
                                  details: "Printer did not respond to status request"))
                return
            }
           
            guard data.count == 1 else {
                result(FlutterError(code: "ERROR_CODE",
                                  message: "INVALID_RESPONSE",
                                  details: "Unexpected response length: \(data.count) bytes"))
                return
            }
           
            let byte = [UInt8](data)
            let status = byte[0]
           
            switch status {
            case 0x12:
                print("Ready")
                result("STS_NORMAL")
               
            case 0x16:
                print("Cover opened")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_COVEROPEN",
                                  details: "Printer cover is open. Please close the cover and try again"))
               
            case 0x32:
                print("Paper end")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_PAPEREMPTY",
                                  details: "Printer is out of paper. Please add paper and try again"))
               
            case 0x36:
                print("Cover opened & Paper end")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "STS_COVEROPEN_STS_PAPEREMPTY",
                                  details: "Printer cover is open and out of paper. Please close cover and add paper"))
               
            default:
                print("Error: Unknown status code \(String(format: "0x%02X", status))")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "PRINT_FAIL",
                                  details: "Unknown printer status: \(String(format: "0x%02X", status))"))
            }
        }
    }
   
   
    // ------------------ bluetooth function ------------------ //
   
    @objc
    func getListDevice(_ result: @escaping FlutterResult) {
       if(self.statusBLE == true){
            if dataArr.count > 0 {
                var arr: [[String: Any]] = []
                for peripheral in dataArr {
                    let peripheralDict: [String: Any] = [
                        "address": peripheral.identifier.uuidString,
                        "name": peripheral.name ?? "Unknown"
                    ]
                
                    arr.append(peripheralDict)
                }
                print("found device: \(arr)")
                result(arr)
            } else {
                print("not found device")
                result(FlutterError(code: "ERROR_CODE",
                                  message: "NOT_FOUND",
                                  details: "not found printer"))
            }
            }else{

                result(FlutterError(code: "ERROR_CODE",
                                  message: "BT_NOT_ENABLE",
                                  details: "please enable bluetooth"))
            }
        }
   
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .unknown:
            print("Bluetooth status is UNKNOWN")
        case .resetting:
            print("Bluetooth status is RESETTING")
        case .unsupported:
            print("Bluetooth is NOT SUPPORTED on this device")
        case .unauthorized:
            print("Bluetooth is NOT AUTHORIZED for this app")
        case .poweredOff:
            statusBLE = false;
            print("Bluetooth is currently POWERED OFF")
            // Handle the case when Bluetooth is turned off
        case .poweredOn:
            statusBLE = true;
            print("Bluetooth is currently POWERED ON")
            // Handle the case when Bluetooth is turned on
        @unknown default:
            print("A previously unknown state occurred")
        }
    }
   
   
    public func poSbleUpdatePeripheralList(_ peripherals: [Any]!, rssiList: [Any]!) {
        if let peripherals = peripherals as? [CBPeripheral], let rssiList = rssiList as? [NSNumber] {
            self.dataArr = peripherals
            self.rssiList = rssiList
            print("update BLE devices : \(String(describing: peripherals))");
        }
    }
   
    public func poSbleConnect(_ peripheral: CBPeripheral!) {
        print("poSbleConnect ")
        addressCurrent = peripheral.identifier.uuidString;
        resultMethod?("CONNECTED");
        resultMethod = nil
    }
   
    public func poSbleFail(toConnect peripheral: CBPeripheral!, error: (any Error)!) {
        print("Bluetooth connection failed")
        addressCurrent = ""
        let errorMessage = error?.localizedDescription ?? "Unknown error"
        resultMethod?(FlutterError(code: "ERROR_CODE",
                                 message: "BLE_CONNECT_FAILED",
                                 details: "Failed to connect to device: \(errorMessage)"))
        resultMethod = nil
    }
   
    public func poSbleDisconnectPeripheral(_ peripheral: CBPeripheral!, error: (any Error)!) {
        print("Disconnecting Bluetooth peripheral: \(peripheral.identifier.uuidString)")
        self.btManager.startScan()
        addressCurrent = ""
        self.btManager.disconnectRootPeripheral()
        if let error = error {
            resultMethod?(FlutterError(code: "ERROR_CODE",
                                    message: "BLE_DISCONNECT_ERROR",
                                    details: "Error during disconnect: \(error.localizedDescription)"))
        } else {
            resultMethod?("DISCONNECT")
        }
        resultMethod = nil
    }
   
    public func poSbleWriteValue(for character: CBCharacteristic!, error: (any Error)!) {
        if let error = error {
            print("Bluetooth write error: \(error.localizedDescription)")
            resultMethod?(FlutterError(code: "ERROR_CODE",
                                    message: "PRINT_ERROR",
                                    details: "Failed to write to device: \(error.localizedDescription)"))
        } else if character != nil {
            print("Bluetooth write successful")
            resultMethod?("STS_NORMAL")
            self.btManager.disconnectRootPeripheral()
        } else {
            resultMethod?(FlutterError(code: "ERROR_CODE",
                                    message: "PRINT_ERROR",
                                    details: "Invalid characteristic"))
        }
        resultMethod = nil
    }
   
    public func poSbleReceiveValue(for characteristic: CBCharacteristic!, error: (any Error)!) {
        print("poSbleReceiveValue \(characteristic.description) ")
    }
   
    public func poSbleCentralManagerDidUpdateState(_ state: Int) {
        self.btManager.startScan();
        print("poSbleCentralManagerDidUpdateState")
    }
    // ------------------ bluetooth function ------------------ //
   
   
   
    // Convert color image
    func monoImg(image: UIImage, threshold: CGFloat = 0.1) -> UIImage? {
        // Convert UIImage to CIImage
        guard let ciImage = CIImage(image: image) else { return nil }
       
        // Convert to grayscale
        let grayscaleFilter = CIFilter(name: "CIColorControls")
        grayscaleFilter?.setValue(ciImage, forKey: kCIInputImageKey)
        grayscaleFilter?.setValue(0.0, forKey: kCIInputSaturationKey)  // Remove color
        grayscaleFilter?.setValue(1.0, forKey: kCIInputContrastKey)    // Maximize contrast
       
        guard let grayscaleImage = grayscaleFilter?.outputImage else { return nil }
       
        // Apply threshold to create black and white effect
        let thresholdFilter = CIFilter(name: "CIColorMatrix")
        thresholdFilter?.setValue(grayscaleImage, forKey: kCIInputImageKey)
       
        // Set the threshold to control black and white conversion
        let thresholdVector = CIVector(x: threshold, y: threshold, z: threshold, w: 0)
        thresholdFilter?.setValue(thresholdVector, forKey: "inputRVector")
        thresholdFilter?.setValue(thresholdVector, forKey: "inputGVector")
        thresholdFilter?.setValue(thresholdVector, forKey: "inputBVector")
        thresholdFilter?.setValue(CIVector(x: 0, y: 0, z: 0, w: 1), forKey: "inputAVector")
       
        guard let outputCIImage = thresholdFilter?.outputImage else { return nil }
       
        // Create a context and convert to UIImage
        let context = CIContext(options: nil)
        if let cgImage = context.createCGImage(outputCIImage, from: outputCIImage.extent) {
            return UIImage(cgImage: cgImage)
        }
       
        return nil
    }
   
    @objc
    func byteMerger(byte1: [UInt8], byte2: [UInt8]) -> [UInt8] {
        var byte3 = [UInt8]()
        byte3.append(contentsOf: byte1)
        byte3.append(contentsOf: byte2)
        return byte3
    }
    func isValidIPAddress(_ ipAddress: String) -> Bool {
          let components = ipAddress.split(separator: ".")
         
          // Check if it has exactly 4 components
          if components.count != 4 {
              return false
          }
         
          // Check if each component is a number between 0 and 255
          for component in components {
              if let number = Int(component), number >= 0 && number <= 255 {
                  continue
              } else {
                  return false
              }
          }
         
          return true
      }
}

