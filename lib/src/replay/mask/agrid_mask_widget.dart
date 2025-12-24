import 'package:flutter/material.dart';

class AgridMaskWidget extends StatefulWidget {
  final Widget child;

  const AgridMaskWidget({
    super.key,
    required this.child,
  });

  @override
  AgridMaskWidgetState createState() => AgridMaskWidgetState();
}

class AgridMaskWidgetState extends State<AgridMaskWidget> {
  final GlobalKey _widgetKey = GlobalKey();

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      key: _widgetKey,
      child: widget.child,
    );
  }
}
