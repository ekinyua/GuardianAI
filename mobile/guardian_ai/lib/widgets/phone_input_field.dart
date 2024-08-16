import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import '../utils/utils.dart';

class PhoneInputField extends StatefulWidget {
  final TextEditingController controller;
  final String hint;
  final String title;
  final String? Function(String?)? validator;
  final TextInputType textInputType;
  const PhoneInputField(
      {super.key,
      required this.controller,
      required this.hint,
      required this.title,
      required this.validator,
      required this.textInputType});

  @override
  State<PhoneInputField> createState() => _PhoneInputFieldState();
}

class _PhoneInputFieldState extends State<PhoneInputField> {
  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          widget.title,
          style: Theme.of(context).textTheme.titleMedium!.copyWith(
                color: AppCustomColors.white,
                fontSize: 14.sp,
                fontWeight: FontWeight.w500,
              ),
        ),
        SizedBox(height: 5.h),
        Container(
          padding: const EdgeInsets.all(5),
          decoration: BoxDecoration(
            color: const Color(0xff1E1E2D),
            borderRadius: BorderRadius.circular(10),
          ),
          child: TextFormField(
            controller: widget.controller,
            autocorrect: false,
            validator: widget.validator,
            keyboardType: widget.textInputType,
            style: const TextStyle(color: Colors.white),
            cursorColor: Colors.white,
            decoration: InputDecoration(
              border: InputBorder.none,
              focusedBorder: InputBorder.none,
              enabledBorder: InputBorder.none,
              errorBorder: InputBorder.none,
              disabledBorder: InputBorder.none,
              errorMaxLines: 3,
              hintText: widget.hint,
              hintStyle: Theme.of(context).textTheme.titleMedium!.copyWith(
                    color: AppCustomColors.grayishblue,
                  ),
              contentPadding: const EdgeInsets.only(
                left: 15,
                bottom: 11,
                top: 11,
                right: 15,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
