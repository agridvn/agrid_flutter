import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:agrid_flutter/src/replay/element_parsers/element_data.dart';
import 'package:agrid_flutter/src/replay/element_parsers/element_parser.dart';
import 'package:agrid_flutter/src/replay/mask/agrid_mask_controller.dart';
import 'package:agrid_flutter/src/replay/mask/agrid_mask_widget.dart';

class ElementObjectParser {
  final ElementParser _elementParser = ElementParser();

  ElementData? relateRenderObject(
    ElementData activeElementData,
    Element element,
  ) {
    if (element.widget is AgridMaskWidget) {
      final elementData = _elementParser.relate(element, activeElementData);

      if (elementData != null) {
        activeElementData.addChildren(elementData);
        return elementData;
      }
    }

    if (element.widget is Text) {
      final elementData = _elementParser.relate(element, activeElementData);

      if (elementData != null) {
        activeElementData.addChildren(elementData);
        return elementData;
      }
    }

    if (element.widget is TextField) {
      final textField = element.widget as TextField;
      if (textField.obscureText) {
        final elementData = _elementParser.relate(element, activeElementData);

        if (elementData != null) {
          activeElementData.addChildren(elementData);
          return elementData;
        }
      }
    }

    if (element.renderObject is RenderImage) {
      final dataType = element.renderObject.runtimeType.toString();

      final parser = AgridMaskController.instance.parsers[dataType];
      if (parser != null) {
        final elementData = parser.relate(element, activeElementData);

        if (elementData != null) {
          activeElementData.addChildren(elementData);
          return elementData;
        }
      }
    }

    return null;
  }
}
