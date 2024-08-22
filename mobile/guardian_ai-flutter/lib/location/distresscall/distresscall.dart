import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

class DistressCallScreen extends StatelessWidget {
  const DistressCallScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: const Color(0xff000000),
          leading: IconButton(
            icon: Icon(Icons.arrow_back, color: Colors.white, size: 24.r),
            onPressed: () {
              Navigator.pop(context);
            },
          ),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              height: 30.h,
            ),
            Center(
              child: Text(
                'Verify your distress call',
                style: TextStyle(
                  color: const Color(0xffE2E4E5),
                  fontWeight: FontWeight.bold,
                  fontSize: 24.sp,
                  fontFamily: 'Poppins',
                ),
              ),
            ),
            SizedBox(
              height: 20.h,
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0),
              child: Text(
                'We\'ll use voice biometrics to verify your\ndistress call.This is a one-time process.',
                style: TextStyle(
                  color: const Color(0xffBDC1C2),
                  fontSize: 14.sp,
                  fontWeight: FontWeight.w400,
                  fontFamily: 'Poppins',
                ),
              ),
            ),
            SizedBox(
              height: 20.h,
            ),
            ListTile(
              leading: Container(
                width: 45.w,
                height: 50.h,
                decoration: BoxDecoration(
                    color: const Color(0xff293338),
                    borderRadius: BorderRadius.circular(10.r)),
                child: Icon(
                  Icons.arrow_forward_ios,
                  color: const Color(0xffFFffff),
                  size: 30.r,
                ),
              ),
              title: Text(
                'When to use this feature',
                style: TextStyle(
                  color: const Color(0xffE2E4E5),
                  fontSize: 16.sp,
                  fontWeight: FontWeight.w500,
                  fontFamily: 'Poppins',
                ),
              ),
              subtitle: Text(
                'In case of emergency, when you can\'t\nspeak or are in a quiet place',
                style: TextStyle(
                  color: const Color(0xffBDC1C2),
                  fontSize: 12.sp,
                  fontWeight: FontWeight.w400,
                  fontFamily: 'Poppins',
                ),
              ),
              trailing: const Icon(
                Icons.arrow_forward_ios,
                color: Colors.white,
              ),
            ),
            SizedBox(
              height: 30.h,
            ),
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  margin: const EdgeInsets.all(10),
                  width: 210.w,
                  height: 50.h,
                  decoration: BoxDecoration(
                      color: const Color(0xff293338),
                      borderRadius: BorderRadius.circular(10.r)),
                  child: Center(
                    child: Text(
                      'Start verification',
                      style: TextStyle(
                          fontSize: 14.sp,
                          fontFamily: 'Poppins',
                          fontWeight: FontWeight.bold,
                          color: Colors.white),
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.all(10),
                  margin: const EdgeInsets.all(10),
                  width: 120.w,
                  height: 50.h,
                  decoration: BoxDecoration(
                      color: const Color(0xff129CED),
                      borderRadius: BorderRadius.circular(10.r)),
                  child: Center(
                    child: Text(
                      'Start Sharing',
                      style: TextStyle(
                          fontSize: 14.sp,
                          fontFamily: 'Poppins',
                          fontWeight: FontWeight.bold,
                          color: Colors.white),
                    ),
                  ),
                ),
              ],
            )
          ],
        ),
      ),
    );
  }
}
