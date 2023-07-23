import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_ble_ser/src/constants/ble_device_connection_state.dart';
import 'package:flutter_ble_ser/src/models/ble_devise.dart';
import 'package:flutter_ble_ser/src/utils/stream_buffer.dart';

class BleManager {
  final MethodChannel _channel = const MethodChannel('myChannel/methods');

  BleManager() {
    _channel.setMethodCallHandler((MethodCall call) async {
      _methodStreamController.add(call);
    });
  }

  final StreamController<MethodCall> _methodStreamController =
      StreamController.broadcast();
  Stream<MethodCall> get _methodStream => _methodStreamController.stream;

  Future<bool?> setAdvertise({required String uuid}) async {
    return await _channel.invokeMethod('setAdvertise', {'uuid': uuid});
  }

  Stream<BleDevice?> startScan({String? serviseUuid}) async* {
    final started =
        await _channel.invokeMethod('start', {"service": serviseUuid});

    Stream<Map?> scanResultsStream = _methodStream
        .where((m) => m.method == "scanResult")
        .map((m) => m.arguments);

    final buffer = BufferStream.listen(scanResultsStream);

    await for (final item in buffer.stream) {
      yield BleDevice.fromMap(item);
    }
  }

  Future<BleDeviceConnectionState?> connectToDevice(String id) async {
    Stream<String?> scanResultsStream = _methodStream
        .where((m) => m.method == "connectToDevise")
        .map((m) => m.arguments);

    await _channel.invokeMethod('connectToDevise', {"address": id});

    final buffer = BufferStream.listen(scanResultsStream);

    await for (final item in buffer.stream) {
      if (item == "connected") {
        return BleDeviceConnectionState.connected;
      } else if (item == "disconnected") {
        return BleDeviceConnectionState.disconnected;
      } else if (item == "connecting") {
        return BleDeviceConnectionState.connected;
      }
    }
    return null;
  }

  Stream<String?> readData() async* {
    Stream<String?> scanResultsStream =
        _methodStream.where((m) => m.method == "read").map((m) => m.arguments);

    await _channel.invokeMethod('read');

    final buffer = BufferStream.listen(scanResultsStream);

    await for (final item in buffer.stream) {
      yield item;
    }
  }

  Future<bool> writeData(String? data) async {
    Stream<bool?> scanResultsStream =
        _methodStream.where((m) => m.method == "write").map((m) => m.arguments);

    await _channel.invokeMethod('write', {"data": data});

    final buffer = BufferStream.listen(scanResultsStream);

    await for (final item in buffer.stream) {
      return item ?? false;
    }
    return false;
  }

  Future<void> dispose() async {
    await _channel.invokeMethod('dispose');
  }
}
