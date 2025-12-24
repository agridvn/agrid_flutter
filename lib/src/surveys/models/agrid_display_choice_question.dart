import 'package:flutter/foundation.dart';
import 'agrid_survey_question_type.dart';
import 'agrid_display_survey_question.dart';

/// choice question type
@immutable
class AgridDisplayChoiceQuestion extends AgridDisplaySurveyQuestion {
  const AgridDisplayChoiceQuestion({
    required super.id,
    required super.question,
    required this.choices,
    required this.isMultipleChoice,
    this.hasOpenChoice = false,
    this.shuffleOptions = false,
    super.description,
    super.descriptionContentType,
    super.optional,
    super.buttonText,
  }) : super(
          type: isMultipleChoice
              ? AgridSurveyQuestionType.multipleChoice
              : AgridSurveyQuestionType.singleChoice,
        );

  final List<String> choices;
  final bool isMultipleChoice;
  final bool hasOpenChoice;
  final bool shuffleOptions;
}
