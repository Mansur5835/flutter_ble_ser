import 'package:flutter/material.dart';
import 'package:flutter_ble_ser/src/ble_manager.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            MaterialButton(
              color: Colors.red,
              onPressed: () async {
                if (await Permission.bluetoothScan.isDenied) {
                  await Permission.bluetoothScan.request();
                }
                if (await Permission.bluetoothConnect.isDenied) {
                  await Permission.bluetoothConnect.request();
                }
                if (await Permission.bluetoothAdvertise.isDenied) {
                  await Permission.bluetoothAdvertise.request();
                }
                if (await Permission.location.isDenied) {
                  await Permission.location.request();
                }

                BleManager().start().listen((event) {
                  print("flutter evet $event");
                });
              },
              child: const Text('Start'),
            ),
          ],
        ),
      ),
// This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
