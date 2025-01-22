package com.akaexample.ble_scanner

import io.flutter.embedding.android.FlutterActivity
import android.content.BroadcastReceiver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.content.Intent
import android.content.IntentFilter
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.JSONMessageCodec

class MainActivity: FlutterActivity(){
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
            super.configureFlutterEngine(flutterEngine)

            MethodChannel(
                flutterEngine.dartExecutor.binaryMessenger,
                "ble_scanner"
            ).setMethodCallHandler { call, result ->
                if (call.method == "scanDevices") {
                    
                    var bluetoothAdapter: BluetoothAdapter? = null
                    var scanCallback: ScanCallback? = null
                    val scannedDevices: MutableList<Map<String, String>> = mutableListOf()
                    val permissionRequestCode = 100

                    if (bluetoothAdapter?.isEnabled == false) {
                        result.error("BLUETOOTH_DISABLED", "Bluetooth is disabled.", null)
                    }
                    // if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //     result.error("LOCATION_PERMISSION_DENIED", "Location permission is required.", null)
                    // }


                    scannedDevices.clear()
                    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
                    scanCallback = object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                            
                            val device = scanResult.device
                            val deviceMap = mapOf(
                                "name" to (device.name ?: "Unknown"),
                                "address" to device.address
                            )
                            if(!scannedDevices.any { it["address"] == device.address }){
                                scannedDevices.add(deviceMap);
                            }
                        }

                        override fun onScanFailed(errorCode: Int) {
                            result.error("SCAN_FAILED", "Bluetooth scan failed with error code: $errorCode", null)
                        }
                    }
                    bluetoothLeScanner?.startScan(scanCallback)
                    android.os.Handler().postDelayed({
                        bluetoothLeScanner?.stopScan(scanCallback)
                        scanCallback = null
                        result.success(scannedDevices)
                    }, 10000)
                } else {
                    result.notImplemented()
                }
            }
    }

}
