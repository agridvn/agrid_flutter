import 'package:meta/meta.dart';

/// A wrapper exception that carries Agrid-specific metadata
@internal
class AgridException implements Exception {
  /// The original exception/error that was wrapped
  final Object source;
  final String mechanism;
  final bool handled;

  const AgridException({
    required this.source,
    required this.mechanism,
    this.handled = false,
  });
}
