/// Rating type for survey questions
enum AgridDisplaySurveyRatingType {
  number(0),
  emoji(1);

  const AgridDisplaySurveyRatingType(this.value);
  final int value;

  static AgridDisplaySurveyRatingType fromInt(int type) {
    return AgridDisplaySurveyRatingType.values.firstWhere(
      (e) => e.value == type,
      orElse: () => AgridDisplaySurveyRatingType.number,
    );
  }

  @override
  String toString() => name;
}
