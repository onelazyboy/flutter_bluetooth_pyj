import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bluetooth_pyj/flutter_bluetooth_pyj.dart';
import 'package:flutter_bluetooth_pyj/flutter_bluetooth_pyj_platform_interface.dart';
import 'package:flutter_bluetooth_pyj/flutter_bluetooth_pyj_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterBluetoothPyjPlatform
    with MockPlatformInterfaceMixin
    implements FlutterBluetoothPyjPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterBluetoothPyjPlatform initialPlatform =
      FlutterBluetoothPyjPlatform.instance;

  test('$MethodChannelFlutterBluetoothPyj is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterBluetoothPyj>());
  });

  test('getPlatformVersion', () async {
    // FlutterBluetoothPyj flutterBluetoothPyjPlugin = FlutterBluetoothPyj();
    // MockFlutterBluetoothPyjPlatform fakePlatform = MockFlutterBluetoothPyjPlatform();
    // FlutterBluetoothPyjPlatform.instance = fakePlatform;
    //
    // expect(await flutterBluetoothPyjPlugin.getPlatformVersion(), '42');
  });
}
