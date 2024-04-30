//
//  Generated file. Do not edit.
//

// clang-format off

#include "generated_plugin_registrant.h"

#include <charset_converter/charset_converter_plugin.h>
#include <flutter_bluetooth_pyj/flutter_bluetooth_pyj_plugin_c_api.h>

void RegisterPlugins(flutter::PluginRegistry* registry) {
  CharsetConverterPluginRegisterWithRegistrar(
      registry->GetRegistrarForPlugin("CharsetConverterPlugin"));
  FlutterBluetoothPyjPluginCApiRegisterWithRegistrar(
      registry->GetRegistrarForPlugin("FlutterBluetoothPyjPluginCApi"));
}
