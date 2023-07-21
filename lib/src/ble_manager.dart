import 'dart:async';

import 'package:flutter/services.dart';

class BleManager {
  final _channel = const MethodChannel('myChannel');
  EventChannel eventChannel = const EventChannel('myEventChannel');
  final StreamController<MethodCall> _methodStreamController =
      StreamController.broadcast();

  Stream<MethodCall> get _methodStream => _methodStreamController.stream;

  BleManager() {
    _channel.setMethodCallHandler((MethodCall call) async {
      _methodStreamController.add(call);
    });
  }

  Stream<String?> start() async* {
    // _methodStream
    String? name = await _channel.invokeMethod('start');
    yield name;
  }
}
