import 'package:meta/meta.dart';

import 'package:agrid_flutter/src/error_tracking/agrid_error_tracking_autocapture_integration.dart';
import 'agrid_config.dart';
import 'agrid_flutter_platform_interface.dart';
import 'agrid_observer.dart';

class Agrid {
  static AgridFlutterPlatformInterface get _agrid =>
      AgridFlutterPlatformInterface.instance;

  static final _instance = Agrid._internal();

  AgridConfig? _config;

  factory Agrid() {
    return _instance;
  }

  String? _currentScreen;

  /// Android and iOS only
  /// Only used for the manual setup
  /// Requires disabling the automatic init on Android and iOS:
  /// com.agrid.agrid.AUTO_INIT: false
  Future<void> setup(AgridConfig config) {
    _config = config; // Store the config

    _installFlutterIntegrations(config);

    return _agrid.setup(config);
  }

  void _installFlutterIntegrations(AgridConfig config) {
    // Install exception autocapture if enabled
    if (config.errorTrackingConfig.captureFlutterErrors ||
        config.errorTrackingConfig.capturePlatformDispatcherErrors) {
      AgridErrorTrackingAutoCaptureIntegration.install(
        config: config.errorTrackingConfig,
        agrid: _agrid,
      );
    }
  }

  void _uninstallFlutterIntegrations() {
    // Uninstall exception autocapture integration
    AgridErrorTrackingAutoCaptureIntegration.uninstall();
  }

  @internal
  AgridConfig? get config => _config;

  /// Returns the current screen name (or route name)
  /// Only returns a value if [AgridObserver] is used
  @internal
  String? get currentScreen => _currentScreen;

  Future<void> identify({
    required String userId,
    Map<String, Object>? userProperties,
    Map<String, Object>? userPropertiesSetOnce,
  }) =>
      _agrid.identify(
          userId: userId,
          userProperties: userProperties,
          userPropertiesSetOnce: userPropertiesSetOnce);

  Future<void> capture({
    required String eventName,
    Map<String, Object>? properties,
  }) {
    final propertiesCopy = properties == null ? null : {...properties};

    final currentScreen = _currentScreen;
    if (propertiesCopy != null &&
        !propertiesCopy.containsKey('\$screen_name') &&
        currentScreen != null) {
      propertiesCopy['\$screen_name'] = currentScreen;
    }
    return _agrid.capture(
      eventName: eventName,
      properties: propertiesCopy,
    );
  }

  Future<void> screen({
    required String screenName,
    Map<String, Object>? properties,
  }) {
    _currentScreen = screenName;
    return _agrid.screen(
      screenName: screenName,
      properties: properties,
    );
  }

  Future<void> alias({
    required String alias,
  }) =>
      _agrid.alias(
        alias: alias,
      );

  Future<String> getDistinctId() => _agrid.getDistinctId();

  Future<void> reset() => _agrid.reset();

  Future<void> disable() {
    // Uninstall Flutter-specific integrations when disabling
    _uninstallFlutterIntegrations();

    return _agrid.disable();
  }

  Future<void> enable() => _agrid.enable();

  Future<bool> isOptOut() => _agrid.isOptOut();

  Future<void> debug(bool enabled) => _agrid.debug(enabled);

  Future<void> register(String key, Object value) =>
      _agrid.register(key, value);

  Future<void> unregister(String key) => _agrid.unregister(key);

  Future<bool> isFeatureEnabled(String key) => _agrid.isFeatureEnabled(key);

  Future<void> reloadFeatureFlags() => _agrid.reloadFeatureFlags();

  Future<void> group({
    required String groupType,
    required String groupKey,
    Map<String, Object>? groupProperties,
  }) =>
      _agrid.group(
        groupType: groupType,
        groupKey: groupKey,
        groupProperties: groupProperties,
      );

  Future<Object?> getFeatureFlag(String key) =>
      _agrid.getFeatureFlag(key: key);

  Future<Object?> getFeatureFlagPayload(String key) =>
      _agrid.getFeatureFlagPayload(key: key);

  Future<void> flush() => _agrid.flush();

  /// Captures exceptions with optional custom properties
  ///
  /// [error] - The error/exception to capture
  /// [stackTrace] - Optional stack trace (if not provided, current stack trace will be used)
  /// [properties] - Optional custom properties to attach to the exception event
  Future<void> captureException(
          {required Object error,
          StackTrace? stackTrace,
          Map<String, Object>? properties}) =>
      _agrid.captureException(
          error: error, stackTrace: stackTrace, properties: properties);

  /// Closes the Agrid SDK and cleans up resources.
  ///
  /// Note: Please note that after calling close(), surveys will not be rendered until the SDK is re-initialized and the next navigation event occurs.
  Future<void> close() {
    _config = null;
    _currentScreen = null;
    AgridObserver.clearCurrentContext();

    // Uninstall Flutter integrations
    _uninstallFlutterIntegrations();

    return _agrid.close();
  }

  Future<String?> getSessionId() => _agrid.getSessionId();

  Agrid._internal();
}
