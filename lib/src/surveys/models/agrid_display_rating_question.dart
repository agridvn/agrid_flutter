import 'package:flutter/foundation.dart';
import 'agrid_survey_question_type.dart';
import 'agrid_display_survey_question.dart';
import 'agrid_display_survey_rating_type.dart';

/// Rating question type
@immutable
class AgridDisplayRatingQuestion extends AgridDisplaySurveyQuestion {
  const AgridDisplayRatingQuestion({
    required super.id,
    required super.question,
    required this.ratingType,
    required this.scaleLowerBound,
    required this.scaleUpperBound,
    required this.lowerBoundLabel,
    required this.upperBoundLabel,
    super.description,
    super.descriptionContentType,
    super.optional,
    super.buttonText,
  }) : super(
          type: AgridSurveyQuestionType.rating,
        );

  final AgridDisplaySurveyRatingType ratingType;
  final int scaleLowerBound;
  final int scaleUpperBound;
  final String lowerBoundLabel;
  final String upperBoundLabel;
}
