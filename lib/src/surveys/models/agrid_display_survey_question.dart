import 'package:flutter/foundation.dart';
import 'agrid_survey_question_type.dart';
import 'agrid_display_survey_text_content_type.dart';

/// Base class for all survey questions
@immutable
abstract class AgridDisplaySurveyQuestion {
  const AgridDisplaySurveyQuestion({
    required this.id,
    required this.type,
    required this.question,
    this.description,
    this.descriptionContentType = AgridDisplaySurveyTextContentType.text,
    this.optional = false,
    this.buttonText,
  });

  final String id;
  final AgridSurveyQuestionType type;
  final String question;
  final String? description;
  final AgridDisplaySurveyTextContentType descriptionContentType;
  final bool optional;
  final String? buttonText;
}
