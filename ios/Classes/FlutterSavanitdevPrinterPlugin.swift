import Flutter
import Darwin
import UIKit
import Foundation
public class FlutterSavanitdevPrinterPlugin:  NSObject,POSBLEManagerDelegate,FlutterPlugin {
    var test:PrinterManager!
    var isConnectedGlobal:Bool=false
    // Create an array of Bike objects
    var printList: [PrinterManager] = []
    var queueList:[DispatchQueue] = [];
    var isConnectedList = [false];
    var objectArray : [String] = [];
    var manager: POSBLEManager!
    var dataArr: [CBPeripheral] = []
    var rssiList: [NSNumber] = []
    var addreesCurrent = "";
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_savanitdev_printer", binaryMessenger: registrar.messenger())
    let instance = FlutterSavanitdevPrinterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

   public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method {

      case "connectNet":
          let ip = call.arguments as! String;
          connectNet(ip,result: result);
      case "disconnectNet":
          let ip = call.arguments as! String;
          disconnectNet(ip,result: result);
      case "printRawData":
          if let args = call.arguments as? Dictionary<String, Any>,
              let encode = args["encode"] as? String,
              let isDisconnect = args["isDisconnect"] as? Bool,
              let ip  = args["ip"] as? String {
              printRawData(ip, base64String: encode, isDisconnect: isDisconnect,result: result);
            } else {
              result(FlutterError.init(code: "bad args", message: nil, details: nil))
            }

      case "printImgNet":
          if let args = call.arguments as? Dictionary<String, Any>,
              let base64String = args["base64String"] as? String,
              let width = args["width"] as? Int,
              let isDisconnect = args["isDisconnect"] as? Bool,
              let ip  = args["ip"] as? String {
              printImgNet(ip, base64String: base64String,width: width, isDisconnect: isDisconnect,result: result);
            } else {
              result(FlutterError.init(code: "bad args", message: nil, details: nil))
            }


      case "clearLoops":
          clearLoops();

      case "initBLE":
          initBLE();

      case "startScanBLE":
          startScanBLE();

      case "checkStatusBLE":
          startScanBLE();

      case "printLangPrinter":
          printLangPrinter();

      case "getLangModel":
          getLangModel();

      case "cancelChinese":
          cancelChinese();

      case "getListDevice":
          startScanBLE();
          getListDevice(result: result);

      case "setLang":
        let codepage = call.arguments as! String;
          setLang(codepage);


      case "connectBLE":
          let identifiers = call.arguments as! String;
//          print("identifiers :  \(identifiers)")
      connectBLE(identifiers,result: result);
      case "disconnectBLE":
          let identifiers = call.arguments as! String;
          disconnectBLE(result: result);
      case "rawDataBLE":
          let encode = call.arguments as! String;
           rawDataBLE(encode,result: result);

      case "image64BaseBLE":
          if let args = call.arguments as? Dictionary<String, Any>,
              let base64String = args["base64String"] as? String,
              let width = args["width"] as? Int
              {
              print(width);
              image64BaseBLE(base64String,width: width,result: result);
            } else {
              result(FlutterError.init(code: "bad args", message: nil, details: nil))
            }



      default:
        result(FlutterMethodNotImplemented)
      }
    }

      // ------------------ bluetooth function ------------------ //

         @objc
         func initBLE() {
              self.manager = POSBLEManager.sharedInstance()
              self.manager.delegate = self
             print("init BLE")
           }
         @objc
         func startScanBLE() {
              self.manager.poSdisconnectRootPeripheral()
              self.manager.poSstartScan()
             print("startScanBLE")
           }

         @objc
         func checkStatusBLE(_ id :String,result: @escaping FlutterResult) {
           do {
               try
               print("checkStatusBLE")
               let status = self.manager.connectOK ? "connected" :"no connect"
               result(status)
            } catch {
               print("Failed disconnect: \(error)")

                result(FlutterError.init(code: "ERROR_CODE", message: "Failed disconnect", details: error))
           }
         }


        @objc
        func disconnectBLE(result: @escaping FlutterResult) {
          do {
            try self.manager.poSdisconnectRootPeripheral()
             print("disconnect")
              self.addreesCurrent = "";
              result("disconnect")
           } catch {
              print("Failed disconnect: \(error)")
               result(FlutterError.init(code: "ERROR_CODE", message: "Failed disconnect", details: error))
          }
        }

         @objc
         func connectBLE(_ identifiers :String,result: @escaping FlutterResult) {
               if(dataArr.count > 0){
             do {
             if let matchedPeripheral =  dataArr.first { $0.identifier.uuidString == identifiers }{
                         print("Found peripheral: \(matchedPeripheral.name ?? "Unknown")")
                         try self.manager.poSconnectDevice(matchedPeripheral);
                             self.manager.writePeripheral = matchedPeripheral;
                 self.addreesCurrent = "";
                         print("connect successfully.")
                 result("connected")
                     }else{
                         print("Peripheral not found")
//                         result("Peripheral not found")
                     }
             } catch {
                 print("Failed connect: \(error)")
//               result(FlutterError.init(code: "ERROR_CODE", message: "Failed connect", details: error))
             }
               }else{

//                 result(FlutterError.init(code: "ERROR_CODE", message: "Please check your printer is enable or not ", details: "error"))
               }
           }

        @objc
        func getListDevice(result: @escaping FlutterResult) {
                 if dataArr.count > 0 {
                     var arr: [[String: Any]] = []

                     for peripheral in dataArr {
                         let peripheralDict: [String: Any] = [
                             "deviceAddress": peripheral.identifier.uuidString,
                             "name": peripheral.name ?? "Unknown"
                         ]

                         arr.append(peripheralDict)
                     }
                     print("found device: \(arr)")
                     result(arr)
                 } else {
                     print("not found device")
                     result(FlutterError.init(code: "ERROR_CODE", message: "not found device", details: "error"))
                 }
             }

         @objc
         func rawDataBLE(_ encode :String, result: @escaping FlutterResult) {
            if let data = Data(base64Encoded: encode) {
             do {
                 try self.manager.posWriteCommand(with: data)
                 print("Command sent successfully.")
                 result("successfully")
             } catch {
                 print("Failed to send command: \(error)")
                result(FlutterError.init(code: "ERROR_CODE", message: "Failed to send command", details: error))
             }
             }else{
               print("Failed to decode base64 string into data.")
               result(FlutterError.init(code: "ERROR_CODE", message: "Failed to decode base64 string into data", details: "error"))

              }
           }

         @objc
      func image64BaseBLE(_ base64String :String,width:Int, result: @escaping FlutterResult) {
             guard let imageData = Data(base64Encoded: base64String) else {
                 print("Failed to decode base64 string into data.")
                 result(FlutterError.init(code: "ERROR_CODE", message: "Failed to decode base64 string into data", details: "error"))
                 return
             }
             guard let image = UIImage(data: imageData) else {
                 print("Failed to create UIImage from data.")
                 result(FlutterError.init(code: "ERROR_CODE", message: "Failed to create UIImage from data", details: "error"))

                 return
             }

              do {
                  let align: Data = PosCommand.selectAlignment(1)
                  let height: Data = PosCommand.printAndFeedLine()
                  let imageM = ImageTranster.imageCompress(forWidthScale: image, targetWidth: CGFloat(width))
                  let imgData: Data = PosCommand.printRasteBmp(withM: RasterNolmorWH, andImage: imageM ,andType: Dithering)
  //                 let cut: Data = PosCommand.selectCutPageModelAndCutpage(withM: 1, andN: 1)
                   let cut = Data("    ".utf8)
                   let spaceH1 = Data("    ".utf8)
                   let spaceH2 = Data("    ".utf8)
                   let concatenatedData = align + imgData + cut + spaceH1 + spaceH2 + height
                 try self.manager.posWriteCommand(with: concatenatedData)
                 print("Command sent successfully 3.")
                  result("successfully")
             } catch {
                 print("Failed to send command: \(error)")
                 result(FlutterError.init(code: "ERROR_CODE", message: "Failed to send command", details: error))

             }
         }

         public func poSdidUpdatePeripheralList(_ peripherals: [Any]!, rssiList: [Any]!) {
             if let peripherals = peripherals as? [CBPeripheral], let rssiList = rssiList as? [NSNumber] {
                        self.dataArr = peripherals
                        self.rssiList = rssiList

                          print("update BLE devices : \(String(describing: peripherals[0]))");
                    }
         }

         public func poSdidConnect(_ peripheral: CBPeripheral!) {
             print(" BLE poSdidConnect");
         }

         public func poSdidFail(toConnect peripheral: CBPeripheral!, error: Error!) {
             print(" BLE poSdidFail");
         }

         public func poSdidDisconnectPeripheral(_ peripheral: CBPeripheral!, isAutoDisconnect: Bool) {
             print(" BLE poSdidDisconnectPeripheral ");
         }

         public func poSdidWriteValue(for character: CBCharacteristic!, error: Error!) {
             print(" BLE poSdidWriteValue");
         }


         @objc
             func byteMerger(byte1: [UInt8], byte2: [UInt8]) -> [UInt8] {
                 var byte3 = [UInt8]()
                 byte3.append(contentsOf: byte1)
                 byte3.append(contentsOf: byte2)
                 return byte3
             }

             @objc
             func setLang(_ codepage :String) {
              
             }
             @objc
             func printLangPrinter() {
                 
             }
             @objc
             func getLangModel() {
               
             }
             @objc
             func cancelChinese() {
            

             }


         // ------------------ bluetooth function ------------------ //

      @objc
         func clearLoops() {
           self.printList.removeAll();
           self.queueList.removeAll();
           self.isConnectedList.removeAll();
           self.objectArray.removeAll();
           self.dataArr.removeAll()
   
         }

       @objc
       func connectNet(_ ip:String,result: @escaping FlutterResult) -> Void {
          do{
            if(self.objectArray.contains(ip)){
                self.addreesCurrent = ip;
                print("conenct printer 1 : ",ip);
                result("conenct printer");
            }else{
                print("conenct printer 2: ",ip);
              self.objectArray.append(ip);
              if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
              let printer = PrinterManager()

              let queue = DispatchQueue(label: "com.receipt.printer\(index)", qos: .default, attributes: .concurrent, autoreleaseFrequency: .inherit, target: nil)
              self.printList.append(printer)
              self.queueList.append(queue);
              self.isConnectedList.append(false);
              if(self.objectArray.count > 0 ){
                  print("objectArray printer 2: ",ip);
                  self.connectIP(ip,result: result)
              }
             }
            }
          } catch{
              self.addreesCurrent = "";
            print("connect printer catch : ",ip);
           result(FlutterError.init(code: "ERROR_CODE", message: "connect printer catch", details: ""));
          }
        }

      @objc
      func connectIP(_ ip:String,result: @escaping FlutterResult) -> Void {
          if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
              print("objectArray printer 3: ",ip);
            if(self.printList.count > 0 && self.queueList.count > 0)
            {
                print("printList printer 3: ",ip);
              if (self.isConnectedList[index]) {
                self.disconnectNet(ip,result: result);
                self.isConnectedList[index]=false;
             self.connectNet(ip,result: result);
              self.addreesCurrent = "";
                print("disconnect printer index \n",ip);
              }
              else
              {
                self.queueList[index].async {
                  self.printList[index]=PrinterManager.share(0,threadID: "com.receipt.printer\(index)");
                  let isConnected = self.printList[index].connectWifiPrinter(ip, port: 9100);
                  if(isConnected){
                    self.isConnectedList[index]=true;
                    print("check test true : ",isConnected);
                      self.addreesCurrent = ip;
                      result("conenct printer");
                    self.printList[index].startMonitor();
                  }else{
                      self.addreesCurrent = "";
                    print("check test false : ",isConnected);
                     result(FlutterError.init(code: "ERROR_CODE", message: "check test false", details: ""));
                  }
                }
              }
            }
          }
        }

      @objc
       func disconnectNet(_ ip :String,result: @escaping FlutterResult) {
         if(self.isConnectedList.count > 0){
           if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
             if(self.isConnectedList[index]){
                 self.printList[index].disConnectPrinter();
                 self.printList.remove(at: index);
                 self.objectArray.remove(at: index);
//                 self.clearLoops();
               result("disconnect");
                 self.addreesCurrent = "";
             }
           }else{
               self.printList.removeAll();
               self.objectArray.removeAll();
               result("disconnect");
           }
         }
       }

      @objc
      func printImgNet(_ ip:String,base64String :String,width:Int,isDisconnect:Bool,result: @escaping FlutterResult)  -> Void {
               if(self.isConnectedList.count > 0){
                 if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
                   if(self.isConnectedList[index]){
                     queueList[index].async {
                       // Convert the Img base64 string to Data
                       if let imageData = Data(base64Encoded: base64String) {
                         if let image = UIImage(data: imageData) {
                           let align:Data=PosCommand.selectAlignment(1);
                           let Hight  :Data=PosCommand.printAndFeedLine();
                             let imageM = ImageTranster.imageCompress(forWidthScale: image, targetWidth: CGFloat(width))
                             let imgData:Data=PosCommand.printRasteBmp(withM: RasterNolmorWH, andImage: imageM, andType: Dithering);
                           let cut:Data=PosCommand.selectCutPageModelAndCutpage(withM: 1, andN: 1);
                           let spaceH1 = Data("    ".utf8);
                           let spaceH2 = Data("    ".utf8);
                           let concatenatedData = align + imgData + cut + spaceH1 + spaceH2 + Hight
                           let printSucceed = self.printList[index].sendData(toPrinter: concatenatedData);
                           if(printSucceed){
                             print("print1 succeessfully\n");
                               if(isDisconnect){
                                  self.disconnectNet(ip,result: result);
                               }
                               result("print done");
                           }else{
                         let status = self.printList[index].getPrinterStatus();
                         if(status == "Error"){
                           result(FlutterError.init(code: "ERROR_CODE", message: status, details: ip));
                         }
                        else if(status == "Printer Disconnected"){
                           if(self.objectArray.count > 0 ){
                        self.connectIP(ip,result: result);
                        }
                        }
                         else if(status == "Normal"){
                             result("print Normal");
                         }
                        else{
                          result(FlutterError.init(code: "ERROR_CODE", message: status, details: ip));
                        }
                           }
                         }
                       }
                     }
                   }
                 }
               }
           }

   @objc
      func printRawData(_ ip:String,base64String :String,isDisconnect:Bool,result: @escaping FlutterResult) -> Void   {
           if(self.isConnectedList.count > 0){
             if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
               if(self.isConnectedList[index]){
                 print(" print index : ",index);
                 queueList[index].async {
                   if let data = Data(base64Encoded: base64String) {
                     // 'data' now contains the decoded bytes
                     _ = [UInt8](data)
                      let printSucceed = self.printList[index].sendData(toPrinter: data);
                       if(printSucceed){
                         print("print1 succeessfully\n");
                           if(isDisconnect){
                              self.disconnectNet(ip,result: result);
                           }

                           result("print done");
                       }else{
                         let status = self.printList[index].getPrinterStatus();
                         if(status == "Error"){
                           result(FlutterError.init(code: "ERROR_CODE", message: status, details: ip));
                         }
                        else if(status == "Printer Disconnected"){
                           self.connectIP(ip,result: result);
                        }
                         else if(status == "Normal"){
                             result("print Normal");
                         }
                        else{
                          result(FlutterError.init(code: "ERROR_CODE", message: status, details: ip));
                        }
                       }
                 }
               }
             }
           }
        }
      }

      @objc
      func sendConfigNet(_ ip:String,data : Data) -> Void   {
              if(self.isConnectedList.count > 0){
                if let index = self.objectArray.firstIndex(where: { $0 == ip }) {
                  if(self.isConnectedList[index]){
                    print(" print index : ",index);
                    queueList[index].async {
                       self.printList[index].sendData(toPrinter: data);
                  }
                }
              }
           }
         }


  }
