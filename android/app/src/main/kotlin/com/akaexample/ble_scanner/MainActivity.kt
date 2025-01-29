package com.akaexample.ble_scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.ConcurrentHashMap

class MainActivity: FlutterActivity(), MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanCallback: ScanCallback? = null
    private val scannedDevices: MutableMap<String, Map<String, String>> = ConcurrentHashMap()
    private var resultScan: Result? = null
    private val permissionRequestCode = 100


    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "ble_scanner")
        channel.setMethodCallHandler(this)
        context = this
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "scanDevices" -> {
                resultScan = result
                scanDevices()
            }
            else -> result.notImplemented()
        }
    }


    private fun scanDevices() {
        if (bluetoothAdapter?.isEnabled == false) {
            resultScan?.error("BLUETOOTH_DISABLED", "Bluetooth is disabled.", null)
            return
        }
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), permissionRequestCode)
            return
        }
        startScan()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if(grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED } ) {
                startScan()
            } else {
                resultScan?.error("LOCATION_PERMISSION_DENIED", "Location and Bluetooth scan permission is required.", null)
            }
        }
    }

    private fun startScan() {
       try{
            scannedDevices.clear()
             val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
             if (bluetoothLeScanner == null) {
                resultScan?.error("BLUETOOTH_SCANNER_ERROR", "Bluetooth scanner not available", null)
                    return;
            }

            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                    try {
                        val device = scanResult.device
                        val deviceMap = mapOf(
                            "name" to (device.name ?: "Unknown"),
                            "address" to device.address
                        )
                        Log.d("BleScannerPlugin", "Device found: ${deviceMap["name"]} - ${deviceMap["address"]}")
                        scannedDevices[device.address] = deviceMap
                    } catch (e: Exception) {
                        Log.e("BleScannerPlugin", "Error in onScanResult: $e")
                        resultScan?.error("SCAN_RESULT_ERROR", "Error in onScanResult: $e", null)
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                   Log.e("BleScannerPlugin", "Error in onScanFailed: $errorCode")
                    resultScan?.error("SCAN_FAILED", "Bluetooth scan failed with error code: $errorCode", null)
                }
            }
             try{
                 bluetoothLeScanner.startScan(scanCallback)
             } catch (e: Exception){
                 Log.e("BleScannerPlugin", "Error in startScan: $e")
                resultScan?.error("SCAN_START_FAILED", "Failed to start scan: $e", null)
                return;
             }
              android.os.Handler().postDelayed({
               stopScan(bluetoothLeScanner)
            }, 5000)
        } catch (e: Exception) {
            Log.e("BleScannerPlugin", "Error in startScan : $e")
            resultScan?.error("SCAN_START_FAILED", "Failed to start scan: $e", null)
        }
    }
    private fun stopScan(bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner?){
        try{
             if (bluetoothLeScanner != null && scanCallback != null) {
              bluetoothLeScanner.stopScan(scanCallback)
              scanCallback = null
             }
            val list = scannedDevices.values.toList();
            resultScan?.success(list)

        } catch (e: Exception){
            Log.e("BleScannerPlugin", "Error in stopScan: $e")
             resultScan?.error("SCAN_STOP_FAILED", "Failed to stop scan: $e", null)
       }
    }
}
