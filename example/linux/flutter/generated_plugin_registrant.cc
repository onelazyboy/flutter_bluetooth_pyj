//
//  Generated file. Do not edit.
//

// clang-format off

#include "generated_plugin_registrant.h"

#include <charset_converter/charset_converter_plugin.h>
#include <flutter_bluetooth_pyj/flutter_bluetooth_pyj_plugin.h>

void fl_register_plugins(FlPluginRegistry* registry) {
  g_autoptr(FlPluginRegistrar) charset_converter_registrar =
      fl_plugin_registry_get_registrar_for_plugin(registry, "CharsetConverterPlugin");
  charset_converter_plugin_register_with_registrar(charset_converter_registrar);
  g_autoptr(FlPluginRegistrar) flutter_bluetooth_pyj_registrar =
      fl_plugin_registry_get_registrar_for_plugin(registry, "FlutterBluetoothPyjPlugin");
  flutter_bluetooth_pyj_plugin_register_with_registrar(flutter_bluetooth_pyj_registrar);
}
