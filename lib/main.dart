import 'package:flutter/material.dart';
import 'package:flutter_ble_ser/src/ble_manager.dart';
import 'package:flutter_ble_ser/src/constants/ble_device_connection_state.dart';
import 'package:flutter_ble_ser/src/models/ble_devise.dart';
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
  List<BleDevice?> list = [];

  BleManager bleManager = BleManager();

  List<String?> _readResults = [];

  TextEditingController textEditingController = TextEditingController();

  Future<void> startScan() async {
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

    bleManager.setAdvertise(uuid: 'b2c10ae9-3747-4d14-abd1-0450be65fb05');
    bleManager
        .startScan(
      serviseUuid: 'b2c10ae9-3747-4d14-abd1-0450be65fb05',
    )
        .listen((devise) {
      if (!list.contains(devise)) {
        list.add(devise);
        setState(() {});
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
        actions: [
          IconButton(
              onPressed: () {
                startScan.call();
              },
              icon: const Icon(Icons.download_sharp))
        ],
      ),
      body: SingleChildScrollView(
        child: Column(children: [
          ...List.generate(list.length, (index) {
            return _item(list[index]);
          }),
          const SizedBox(
            height: 20,
          ),
          TextField(
            controller: textEditingController,
            decoration: const InputDecoration(
              hintText: "Input",
            ),
          ),
          const SizedBox(
            height: 10,
          ),
          MaterialButton(
            color: Colors.red,
            onPressed: () async {
              final isWrited =
                  await bleManager.writeData(textEditingController.text);
              print(isWrited);
            },
            child: Text("Send"),
          ),
          const SizedBox(
            height: 20,
          ),
          ...List.generate(_readResults.length, (index) {
            return Padding(
              padding: const EdgeInsets.all(10),
              child: Text(_readResults[index] ?? ""),
            );
          })
        ]),
      ),
// This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget _item(BleDevice? device) {
    return ListTile(
      title: Text("${device?.name}"),
      subtitle: Text("${device?.id}"),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          FilledButton(
              onPressed: () async {
                bleManager.readData().listen((event) {
                  if (!_readResults.contains(event)) {
                    _readResults.add(event);
                    setState(() {});
                    print('---> flutter read $event');
                  }
                });
              },
              child: Text("Read")),
          FilledButton(
              onPressed: () async {
                final deviseState =
                    await bleManager.connectToDevice(device!.id ?? "");

                if (BleDeviceConnectionState.connected == deviseState) {
                  device.isConnacted = true;
                  setState(() {});
                } else {
                  device.isConnacted = false;
                  setState(() {});
                }
                print(' --- flutter   $deviseState');
              },
              child:
                  Text(device?.isConnacted == true ? "Connected" : "Connect")),
        ],
      ),
    );
  }

  @override
  void dispose() {
    bleManager.dispose();
    super.dispose();
  }
}
