/// Content type for text-based survey elements
enum AgridDisplaySurveyTextContentType {
  /// Content should be rendered as HTML
  html(0),

  /// Content should be rendered as plain text
  text(1);

  const AgridDisplaySurveyTextContentType(this.value);

  final int value;

  /// Create from raw int value
  static AgridDisplaySurveyTextContentType fromInt(int value) {
    return AgridDisplaySurveyTextContentType.values.firstWhere(
      (e) => e.value == value,
      orElse: () => AgridDisplaySurveyTextContentType.text, // Default to text
    );
  }
}
