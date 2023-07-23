class BleDevice {
  String? name;
  String? id;
  String? uuid;
  bool? isConnacted;
  BleDevice({this.id, this.isConnacted = false, this.name, this.uuid});

  BleDevice.fromMap(Map? map) {
    id = map?['id'];
    name = map?['name'];
    isConnacted = map?['isConnected'];
    uuid = map?['uuid'];
  }

  @override
  bool operator ==(Object other) {
    return (other is BleDevice) && other.id == id;
  }
}
