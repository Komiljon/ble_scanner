import 'package:flutter/material.dart';
import 'package:ble_scanner/ble_scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BLE Scanner',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final BleScanner _bleScanner = BleScanner();
  List<BleDevice> _devices = [];

  Future<void> _scan() async {
    try {
      final devices = await _bleScanner.scanDevices();
      setState(() {
        _devices = devices;
        print(devices);
      });
    } catch (e) {
      print("Error during scan: $e");
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString())));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('BLE Scanner'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              onPressed: _scan,
              child: const Text('Scan for Devices'),
            ),
            Expanded(
              child: ListView.builder(
                itemCount: _devices.length,
                itemBuilder: (context, index) {
                  final device = _devices[index];
                  return ListTile(
                    title: Text(device.name),
                    subtitle: Text(device.address),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
