import 'package:flutter/foundation.dart';
import 'agrid_survey_question_type.dart';
import 'agrid_display_survey_question.dart';

/// Link question type
@immutable
class AgridDisplayLinkQuestion extends AgridDisplaySurveyQuestion {
  const AgridDisplayLinkQuestion({
    required super.id,
    required super.question,
    required this.link,
    super.description,
    super.descriptionContentType,
    super.optional,
    super.buttonText,
  }) : super(
          type: AgridSurveyQuestionType.link,
        );

  final String link;
}
