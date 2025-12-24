import 'agrid_display_survey.dart';

/// Called when a survey is shown to the user
typedef OnSurveyShown = void Function(AgridDisplaySurvey survey);

/// Called when a user responds to a survey question
typedef OnSurveyResponse = Future<AgridSurveyNextQuestion> Function(
  AgridDisplaySurvey survey,
  int questionIndex,
  Object? response,
);

/// Called when a survey is closed
typedef OnSurveyClosed = void Function(AgridDisplaySurvey survey);

/// Represents the next question to show in a survey
class AgridSurveyNextQuestion {
  const AgridSurveyNextQuestion({
    required this.questionIndex,
    required this.isSurveyCompleted,
  });

  final int questionIndex;
  final bool isSurveyCompleted;
}
