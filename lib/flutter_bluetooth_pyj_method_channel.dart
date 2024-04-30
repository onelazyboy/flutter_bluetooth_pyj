import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_bluetooth_pyj_platform_interface.dart';

/// An implementation of [FlutterBluetoothPyjPlatform] that uses method channels.
class MethodChannelFlutterBluetoothPyj extends FlutterBluetoothPyjPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_bluetooth_pyj');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
