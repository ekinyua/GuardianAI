import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

class OnboardingScreen extends StatelessWidget {
  const OnboardingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: const Color(0xff000000),
        body: Center(
            child: Column(
          children: [
            Image.asset('assets/logo.png'),
            // const SizedBox(height: 15),
            Image.asset('assets/homeimg.png'),
            SizedBox(height: 15.h),
            RichText(
              text: TextSpan(
                style: TextStyle(
                  fontSize: 30.sp,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                  fontFamily: 'Poppins',
                ),
                children: const [
                  TextSpan(text: 'Discover Endless\n'),
                  TextSpan(text: 'Possibilities'),
                ],
              ),
              textAlign: TextAlign.center,
            ),
            SizedBox(height: 5.h),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Text(
                'Your personal safety companion, using advanced AI technology to keep you safe and secure.',
                style: TextStyle(
                  color: const Color(0xffCDCDE0),
                  fontSize: 14.5.sp,
                  fontWeight: FontWeight.w300,
                ),
                textAlign: TextAlign.center,
              ),
            ),
            SizedBox(height: 20.h),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: ElevatedButton(
                onPressed: () {
                  // Handle button press
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: Text(
                  'Continue with Email',
                  style: TextStyle(
                      fontSize: 16.sp,
                      fontWeight: FontWeight.w500,
                      fontFamily: 'Poppins',
                      color: Colors.white),
                ),
              ),
            ),
          ],
        )),
      ),
    );
  }
}

// class CurvedLinePainter extends CustomPainter {
//   @override
//   void paint(Canvas canvas, Size size) {
//     var paint = Paint()
//       ..color = Colors.blue
//       ..style = PaintingStyle.stroke
//       ..strokeWidth = 4.0;

//     var path = Path();
//     path.moveTo(0, size.height * 0.8);
//     path.quadraticBezierTo(
//         size.width / 2, size.height * 0.2, size.width, size.height * 0.8);

//     canvas.drawPath(path, paint);
//   }

//   @override
//   bool shouldRepaint(CustomPainter oldDelegate) => false;
// }
