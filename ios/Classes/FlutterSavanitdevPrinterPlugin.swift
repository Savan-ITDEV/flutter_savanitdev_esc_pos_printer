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

public class FlutterSavanitdevPrinterPlugin: NSObject, POSWIFIManagerDelegate, WIFIConnecterDelegate, CBCentralManagerDelegate, FlutterPlugin, POSBLEManagerDelegate {


    var rssiList: [NSNumber] = []
    var dataArr: [CBPeripheral] = []
    var startTime: Date?
    var centralManager: CBCentralManager!
    var addressCurrent = ""
    var btManager: POSBLEManager!
    var statusBLE: Bool = false
    var isDisconnectPrinter: Bool = false
    var isConnectPrinter: Bool = false
    var setPrinter = Data()
    let printerManager = PrinterManager()
    var retryCount = 0
    private var resultMethod: FlutterResult?
    private var hasResultBeenCalled = false // To prevent multiple calls
    
    @objc
    func initPrinter() {
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        self.btManager = POSBLEManager.sharedInstance()
        self.btManager.delegate = self
//        self.btManager.disconnectRootPeripheral();
        self.isConnectPrinter = false
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_savanitdev_printer", binaryMessenger: registrar.messenger())
        let instance = FlutterSavanitdevPrinterPlugin()
        instance.initPrinter()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        DispatchQueue.global(qos: .userInitiated).async {
            self.resultMethod = result
            self.hasResultBeenCalled = false
            
            guard let args = call.arguments as? [String: Any] else {
                self.callResult(false)
                return
            }
            
            switch call.method {
            case "connect":
                guard let address = args["address"] as? String,
                      let type = args["type"] as? String else {
                    self.callResult(false)
                    return
                }
                self.connect(address, portType: type)
                
            case "disconnect":
                guard let address = args["address"] as? String else {
                    self.callResult(false)
                    return
                }
                self.disconnect(address)
                
            case "printCommand":
                guard let address = args["address"] as? String
                else {
                    self.callResult(false)
                    return
                }
                let encode = args["encode"] as! String
                let img = args["img"] as! String
                let iniCommand = args["iniCommand"] as! String
                let cutterCommands = args["cutterCommands"] as! String
                let isCut = args["isCut"] as? Bool
                let isDisconnect = args["isDisconnect"] as? Bool
                let isDevicePOS = args["isDevicePOS"] as? Bool
                
                self.printCommand(address, iniCommand: iniCommand, cutterCommands: cutterCommands, encode: encode, img: img, isCut: isCut!, isDisconnect: isDisconnect!, isDevicePOS: isDevicePOS!)
                
            case "discovery":
                guard let type = args["type"] as? String else {
                    result([])
                    return
                }
                if(type == "bluetooth"){
                    self.discovery(result)
                }else{
                    result([])
                    return
                }
            default:
                self.callResult(false)
            }
        }
    }
    
    // Helper to ensure resultMethod is called only once
    private func callResult(_ value: Bool) {
        guard !hasResultBeenCalled, let result = resultMethod else { return }
        hasResultBeenCalled = true
        result(value)
        resultMethod = nil
    }
    
    // MARK: - WIFIConnecterDelegate Methods
    public func wifiPOSConnected(toHost ip: String, port: UInt16, mac: String) {
        print("WiFi printer connected - IP: \(ip), Port: \(port), MAC: \(mac)")
        self.callResult(true)
    }
    
    public func wifiPOSDisconnectWithError(_ error: Error?, mac: String, ip: String) {
        print("WiFi printer disconnected - IP: \(ip), MAC: \(mac)")
        if let printer = printerManager.getPrinter(id: ip) {
            printer.disconnect()
            isDisconnectPrinter = false
            printerManager.removePrinter(id: ip)
        }
        self.callResult(false)
    }
    
    public func wifiPOSWriteValue(withTag tag: Int, mac: String, ip: String) {
        
        guard self.printerManager.getPrinter(id: ip) != nil else {
            self.callResult(false)
            return
        }
        self.statusXprinter(address: ip)
        
    }
    
    public func wifiPOSReceiveValue(for data: Data, mac: String, ip: String) {
        // No result needed here
        print("WiFi printer data received - IP: \(ip), MAC: \(mac), Data size: \(data.count) bytes")
    }
    
    @objc
    func connect(_ address: String, portType: String) {
        if portType == "bluetooth" {
            guard statusBLE else {
                callResult(false)
                return
            }
            connectBLE(address)
        } else {
            connectNet(address)
        }
    }
    
    @objc
    func connectNet(_ address: String) {
        guard !address.isEmpty else {
            callResult(false)
            return
        }
        if printerManager.getPrinter(id: address) != nil {
            callResult(true)
        } else {
            let printer = WIFIConnecter()
            printer.delegate = self
            printerManager.addPrinter(id: address, printer: printer)
            // Success will be handled by delegate
        }
    }
    
    @objc
    func connectBLE(_ identifiers: String) {
        guard statusBLE else {
            callResult(false)
            return
        }
//        self.btManager.startScan()
        if let peripheral = dataArr.first(where: { $0.identifier.uuidString == identifiers }) {
            if (addressCurrent == peripheral.identifier.uuidString && self.isConnectPrinter == true) {
                callResult(true)
            } else {
                self.btManager.connectDevice(peripheral)
            }
        } else {
            callResult(false)
        }
    }
    
    @objc
    func disconnect(_ address: String) {
        guard !address.isEmpty else {
            callResult(false)
            return
        }
        if isValidIPAddress(address) {
            disconnectNet(address)
        } else {
            disconnectBT()
        }
    }
    
    @objc
    func disconnectBT() {
        guard statusBLE else {
            callResult(false)
            return
        }
        if btManager.isConnecting {
            btManager.disconnectRootPeripheral()
        } else {
            callResult(true)
        }
    }
    
    @objc
    func disconnectNet(_ address: String) {
        guard let printer = printerManager.getPrinter(id: address) else {
            callResult(false)
            return
        }
        printer.disconnect()
        callResult(true)
    }
    
    @objc
    func printCommand(_ address: String, iniCommand: String, cutterCommands: String,
                      encode: String, img: String, isCut: Bool, isDisconnect: Bool, isDevicePOS: Bool) {
        if(address.isEmpty){
            callResult(false)
            return
        }
        var concatenatedData = Data([0x1d, 0x72, 0x01])
        if isValidIPAddress(address) {
            concatenatedData.append(POSCommand.initializePrinter())
            concatenatedData.append(POSCommand.selectAlignment(1))
        }else{
            concatenatedData.append(PosCommand2.initializePrinter())
            concatenatedData.append(PosCommand2.selectAlignment(1))
        }
        if(!iniCommand.isEmpty){
            concatenatedData.append(Data(base64Encoded: iniCommand)!)
        }
        if(!img.isEmpty){
            guard let imageData = Data(base64Encoded: img) else { return callResult(false) }
            guard let image = UIImage(data: imageData) else { return callResult(false)}
            let imgEncode = self.monoImg(image: image, threshold: 0.1)
            if isValidIPAddress(address) {
                concatenatedData.append(POSCommand.printRasteBmp(withM: RasterNolmorWH, andImage: imgEncode, andType: Dithering))
            }else{
                concatenatedData.append(PosCommand2.printRasteBmp(withM: RasterNolmorWH, andImage: imgEncode, andType: Dithering))
            }
        }
        if(!encode.isEmpty){
            concatenatedData.append(Data(base64Encoded: encode)!)
        }
        if(isCut && !cutterCommands.isEmpty){
            concatenatedData.append(Data(base64Encoded: cutterCommands)!)
        }
        if(isCut && cutterCommands.isEmpty){
            if isValidIPAddress(address) {
                concatenatedData.append(POSCommand.selectCutPageModelAndCutpage(withM: 1, andN: 1))
            }else{
                concatenatedData.append(PosCommand2.selectCutPageModelAndCutpage(withM: 1, andN: 1))
            }
        }
        isDisconnectPrinter = isDisconnect;
        concatenatedData.append(Data("    ".utf8))
        concatenatedData.append(Data("    ".utf8))
        if isValidIPAddress(address) {
            self.printByteNet(address, data: concatenatedData)
        } else {
            DispatchQueue.global(qos: .background).async {
                self.printByteBLE(concatenatedData)
            }
        }
    }
    
    @objc
    func printByteNet(_ address: String, data: Data) {
        guard let printer = printerManager.getPrinter(id: address) else {
            callResult(false)
            return
        }
        printer.writeCommand(with: data)
        // Success will be handled by delegate
    }
    
    @objc
    func printByteBLE(_ data: Data) {
        guard statusBLE else {
            callResult(false)
            return
        }
        sleep(2)
        // Write data to the characteristic
        btManager.writeCommand(with: data)
        self.statusBTXprinter();
        
    }
    
    @objc
    func statusXprinter(address: String) {
        guard let printer = printerManager.getPrinter(id: address) else {
            callResult(false)
            return
        }
        sleep(1)
        printer.printerStatus { [weak self] (responseData: Data?) in
            DispatchQueue.main.async {
                guard let data = responseData, let byte = [UInt8](data).first, byte == 0 else {
                    self?.callResult(false)
                    printer.disconnect()
                    return
                }
                self?.callResult(true)
                if(self?.isDisconnectPrinter == true){
                    printer.disconnect()
                }
                
                self?.isDisconnectPrinter = false
            }
        }
    }
    
    @objc
    func statusBTXprinter() {
        btManager.printerStatus { [weak self] (responseData: Data?) in
        
                       guard let data = responseData, let byte = [UInt8](data).first, byte == 0 || byte == 18 else {
                           self?.callResult(false)
                           return
                       }
                       print("status ===> \(byte.description)")
                     
//                   if(self?.isDisconnectPrinter == true){
//                    self?.btManager.disconnectRootPeripheral()
//                     }
//                    self?.isDisconnectPrinter = false
                   
               }
    }
    
    @objc
    func discovery(_ result: @escaping FlutterResult) {
        if(self.statusBLE == true){
            if dataArr.count > 0 {
                var arr: [[String: String]] = []
                for peripheral in dataArr {
                    let peripheralDict: [String: String] = [
                        "address": peripheral.identifier.uuidString,
                        "name": peripheral.name ?? "Unknown"
                    ]
                    arr.append(peripheralDict)
                }
                print("found device: \(arr)")
                result(arr)
            } else {
                print("not found device")
                result([])
            }
        }else{
            result([])
        }
    }
    
    
    // MARK: - Bluetooth Delegate Methods
        public func centralManagerDidUpdateState(_ central: CBCentralManager) {
            print("centralManagerDidUpdateState ")
            statusBLE = central.state == .poweredOn
        }
    //
    public func poSbleUpdatePeripheralList(_ peripherals: [Any]!, rssiList: [Any]!) {
            print("poSbleUpdatePeripheralList ")
            if let peripherals = peripherals as? [CBPeripheral], let rssiList = rssiList as? [NSNumber] {
                self.dataArr = peripherals
                self.rssiList = rssiList
            }
        }
        
        public func poSbleConnect(_ peripheral: CBPeripheral!) {
                print("poSbleConnect")
                self.addressCurrent = peripheral.identifier.uuidString
                self.callResult(true)
                self.isConnectPrinter = true
        
        }
        
        public func poSbleFail(toConnect peripheral: CBPeripheral!, error: (any Error)!) {
            print("poSbleFail \(String(describing: error))")
            DispatchQueue.main.async { [weak self] in
                self?.callResult(false)
                self?.isConnectPrinter = false
            }
        }
        
        public func poSbleDisconnectPeripheral(_ peripheral: CBPeripheral!, error: (any Error)!) {
            print("poSbleDisconnectPeripheral  \(String(describing: error))")
            DispatchQueue.main.async { [weak self] in
                self?.btManager.disconnectRootPeripheral()
                self?.isConnectPrinter = false
                self?.callResult(true)
            }
        }
        
        public func poSbleWriteValue(for character: CBCharacteristic!, error: (any Error)!) {
            print("poSbleWriteValue  \(String(describing: error))")
                guard error == nil, character != nil else {
                    self.callResult(false)
                    return
                }
//            self.callResult(true)
            
        }
        
        public func poSbleReceiveValue(for characteristic: CBCharacteristic!, error: (any Error)!) {
            // No action needed
            print("poSbleReceiveValue  \(String(describing: error))")
            print("poSbleReceiveValue  \(String(describing: characteristic.description))")
                guard error == nil, characteristic != nil else {
                    self.callResult(false)
                    return
                }
        
            self.callResult(true)
        }
        
        public func poSbleCentralManagerDidUpdateState(_ state: Int) {
            print("poSbleCentralManagerDidUpdateState \(state.description)")
            self.isConnectPrinter = false
            self.dataArr.removeAll()
            self.dataArr = []
//            self.btManager.disconnectRootPeripheral()
            self.btManager.startScan()
            
        }
    
    // MARK: - Utility Functions
    func monoImg(image: UIImage, threshold: CGFloat = 0.1) -> UIImage? {
        guard let ciImage = CIImage(image: image) else { return nil }
        let grayscaleFilter = CIFilter(name: "CIColorControls")
        grayscaleFilter?.setValue(ciImage, forKey: kCIInputImageKey)
        grayscaleFilter?.setValue(0.0, forKey: kCIInputSaturationKey)
        grayscaleFilter?.setValue(1.0, forKey: kCIInputContrastKey)
        guard let grayscaleImage = grayscaleFilter?.outputImage else { return nil }
        
        let thresholdFilter = CIFilter(name: "CIColorMatrix")
        thresholdFilter?.setValue(grayscaleImage, forKey: kCIInputImageKey)
        let thresholdVector = CIVector(x: threshold, y: threshold, z: threshold, w: 0)
        thresholdFilter?.setValue(thresholdVector, forKey: "inputRVector")
        thresholdFilter?.setValue(thresholdVector, forKey: "inputGVector")
        thresholdFilter?.setValue(thresholdVector, forKey: "inputBVector")
        thresholdFilter?.setValue(CIVector(x: 0, y: 0, z: 0, w: 1), forKey: "inputAVector")
        guard let outputCIImage = thresholdFilter?.outputImage else { return nil }
        
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
        guard components.count == 4 else { return false }
        return components.allSatisfy { Int($0) ?? -1 >= 0 && Int($0) ?? -1 <= 255 }
    }
}
