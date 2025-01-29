import UIKit
import Flutter
import CoreBluetooth

@main
@objc class AppDelegate: FlutterAppDelegate, CBCentralManagerDelegate {

    private var channel: FlutterMethodChannel?
    private var centralManager: CBCentralManager?
    private var result: FlutterResult?
    private var scannedDevices: [[String: String]] = []


    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        let controller : FlutterViewController = window?.rootViewController as! FlutterViewController
           let bleChannel = FlutterMethodChannel(name: "ble_scanner", binaryMessenger: controller.binaryMessenger)
         bleChannel.setMethodCallHandler { (call: FlutterMethodCall, result: @escaping FlutterResult)  in
             self.result = result
              switch call.method {
             case "scanDevices":
                self.scanDevices()
             default:
                 result(FlutterMethodNotImplemented)
             }
         }
        GeneratedPluginRegistrant.register(with: self)
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }

    private func scanDevices() {
        if(centralManager == nil){
               centralManager = CBCentralManager(delegate: self, queue: nil, options: [CBCentralManagerOptionShowPowerAlertKey: true])
               return
           }
              if centralManager?.state != .poweredOn {
                     result?(FlutterError(code: "BLUETOOTH_DISABLED", message: "Bluetooth is disabled", details: nil))
                     return
                 }
        if #available(iOS 13.0, *) {
           if let authorization = centralManager?.authorization {
             if authorization != .allowedWhenInUse {
                centralManager = CBCentralManager(delegate: self, queue: nil, options: [CBCentralManagerOptionShowPowerAlertKey: true])
                  return
                 }
           }
          }
        scannedDevices.removeAll()
        centralManager?.scanForPeripherals(withServices: nil, options: nil)
        DispatchQueue.main.asyncAfter(deadline: .now() + 10.0){
             self.centralManager?.stopScan()
           self.result?(self.scannedDevices)
        }
    }

    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state != .poweredOn {
           self.result?(FlutterError(code: "BLUETOOTH_DISABLED", message: "Bluetooth is disabled", details: nil))
        }
    }

      public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
           let deviceName = peripheral.name ?? "Unknown"
            let deviceMap = [
              "name": deviceName,
              "address": peripheral.identifier.uuidString
            ]
           if(!scannedDevices.contains(where: { $0["address"] == deviceMap["address"] })){
              scannedDevices.append(deviceMap);
           }
      }
}