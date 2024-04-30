import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_bluetooth_pyj_method_channel.dart';

abstract class FlutterBluetoothPyjPlatform extends PlatformInterface {
  /// Constructs a FlutterBluetoothPyjPlatform.
  FlutterBluetoothPyjPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterBluetoothPyjPlatform _instance = MethodChannelFlutterBluetoothPyj();

  /// The default instance of [FlutterBluetoothPyjPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterBluetoothPyj].
  static FlutterBluetoothPyjPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterBluetoothPyjPlatform] when
  /// they register themselves.
  static set instance(FlutterBluetoothPyjPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
