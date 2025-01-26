import 'dart:async';

import 'package:flutter/services.dart';

class BleScanner {
  static const MethodChannel _channel = MethodChannel('ble_scanner');

  Future<List<BleDevice>> scanDevices() async {
    final List? result = await _channel.invokeMethod('scanDevices');
    if (result == null) {
      return [];
    }
    return result.map((e) => BleDevice.fromMap(e)).toList();
  }
}

class BleDevice {
  final String name;
  final String address;
  BleDevice({required this.name, required this.address});

  factory BleDevice.fromMap(Map map) {
    return BleDevice(name: map['name'] ?? "", address: map['address'] ?? "");
  }
}
