// In order to *not* need this ignore, consider extracting the "web" version
// of your plugin as a separate package, instead of inlining it in the same
// package as the core of your plugin.
import 'package:flutter/services.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';

import 'src/agrid_flutter_platform_interface.dart';
import 'src/agrid_flutter_web_handler.dart';

/// A web implementation of the AgridFlutterPlatform of the AgridFlutter plugin.
class AgridFlutterWeb extends AgridFlutterPlatformInterface {
  /// Constructs a AgridFlutterWeb
  AgridFlutterWeb();

  static void registerWith(Registrar registrar) {
    final MethodChannel channel = MethodChannel(
      'agrid_flutter',
      const StandardMethodCodec(),
      registrar,
    );
    final AgridFlutterWeb instance = AgridFlutterWeb();
    channel.setMethodCallHandler(instance.handleMethodCall);
  }

  Future<dynamic> handleMethodCall(MethodCall call) =>
      handleWebMethodCall(call);
}
