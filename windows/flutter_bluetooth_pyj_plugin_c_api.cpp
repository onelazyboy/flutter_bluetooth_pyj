#include "include/flutter_bluetooth_pyj/flutter_bluetooth_pyj_plugin_c_api.h"

#include <flutter/plugin_registrar_windows.h>

#include "flutter_bluetooth_pyj_plugin.h"

void FlutterBluetoothPyjPluginCApiRegisterWithRegistrar(
    FlutterDesktopPluginRegistrarRef registrar) {
  flutter_bluetooth_pyj::FlutterBluetoothPyjPlugin::RegisterWithRegistrar(
      flutter::PluginRegistrarManager::GetInstance()
          ->GetRegistrar<flutter::PluginRegistrarWindows>(registrar));
}
