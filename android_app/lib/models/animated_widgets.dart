import 'package:flutter/material.dart';
import 'dart:math' as math;

class _WaveClipper extends CustomClipper<Path>{

  const _WaveClipper(this.wavePoints);

  final List<Offset> wavePoints;

  @override
  Path getClip(Size size) {
    final path = Path();
    path.addPolygon(wavePoints, false);
    path.lineTo(size.width, size.height);
    path.lineTo(0.0, size.height);
    path.close();
    return path;
  }

  @override
  bool shouldReclip(covariant CustomClipper<Path> oldClipper) => true;

}

class WaveAnimationWidget extends StatefulWidget {
  const WaveAnimationWidget({
    Key key,
    @required this.size,
    @required this.yOffset,
    @required this.color,
  }) : super(key: key);

  final Size size;
  final double yOffset;
  final Color color;

  @override
  _WaveAnimationWidgetState createState() => _WaveAnimationWidgetState();
}

class _WaveAnimationWidgetState extends State<WaveAnimationWidget>
    with SingleTickerProviderStateMixin {

  final List<Offset> wavePoints = [];
  final waveWidth = math.pi / 270;
  final waveHeight = 20.0;
  AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
        vsync: this, duration: Duration(milliseconds: 3000)
    )..addListener(() {
      wavePoints.clear();

      final double waveSpeed = _controller.value * 1080;
      final double fullSphere = _controller.value * math.pi * 2;
      final double normalizer = math.cos(fullSphere);

      for(int i = 0; i <= widget.size.width.toInt(); ++i){
        final calc = math.sin((waveSpeed - i) * waveWidth);
        wavePoints.add( Offset (
          i.toDouble(),
          calc * waveHeight * normalizer + widget.yOffset
        ));
      }
    })..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder (
        animation: _controller,
        builder: (context, _){
        return ClipPath(
          clipper: _WaveClipper(wavePoints),
          child: Container(
            width: widget.size.width,
            height: widget.size.height,
            color: widget.color
          ),
        );
      }
    );
  }
}
