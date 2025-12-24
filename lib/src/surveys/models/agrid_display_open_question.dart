import 'package:flutter/foundation.dart';
import 'agrid_survey_question_type.dart';
import 'agrid_display_survey_question.dart';

/// Open text question type
@immutable
class AgridDisplayOpenQuestion extends AgridDisplaySurveyQuestion {
  const AgridDisplayOpenQuestion({
    required super.id,
    required super.question,
    super.description,
    super.descriptionContentType,
    super.optional,
    super.buttonText,
  }) : super(
          type: AgridSurveyQuestionType.openText,
        );
}
