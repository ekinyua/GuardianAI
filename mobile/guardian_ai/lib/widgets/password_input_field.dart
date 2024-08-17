import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import '../utils/utils.dart';

class PasswordInputField extends StatefulWidget {
  final TextEditingController controller;
  final String hint;
  final String title;
  final String? Function(String?)? validator;
  final TextInputType textInputType;
  const PasswordInputField(
      {super.key,
      required this.controller,
      required this.hint,
      required this.title,
      required this.validator,
      required this.textInputType});

  @override
  State<PasswordInputField> createState() => _PasswordInputFieldState();
}

class _PasswordInputFieldState extends State<PasswordInputField> {
  bool _obscureText = true;

  // bool _containsUppercase = false;
  // bool _containsLowercase = false;
  // bool _containsNumber = false;
  // bool _containsSpecialCharacter = false;
  // bool _containsEightCharacters = false;

  // void validate(value) {
  //   final upperCase = RegExp(r".*[A-Z].*");
  //   final lowerCase = RegExp(r".*[a-z].*");
  //   final number = RegExp(r".*[0-9].*");
  //   final specialCharacter = RegExp(r"[`!@#$%^&*()_+\-=\[\]{};':\\|,.<>\/?~]");

  //   if (upperCase.hasMatch(value)) {
  //     _containsUppercase = true;
  //   } else {
  //     _containsUppercase = false;
  //   }

  //   if (value.lenght >= 8) {
  //     _containsEightCharacters = true;
  //   } else {
  //     _containsEightCharacters = false;
  //   }

  //   if (lowerCase.hasMatch(value)) {
  //     _containsLowercase = true;
  //   } else {
  //     _containsLowercase = false;
  //   }

  //   if (number.hasMatch(value)) {
  //     _containsNumber = true;
  //   } else {
  //     _containsNumber = false;
  //   }

  //   if (specialCharacter.hasMatch(value)) {
  //     _containsSpecialCharacter = true;
  //   } else {
  //     _containsSpecialCharacter = false;
  //   }

  //   setState(() {});
  // }

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
            obscureText: _obscureText,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              border: InputBorder.none,
              focusedBorder: InputBorder.none,
              enabledBorder: InputBorder.none,
              errorBorder: InputBorder.none,
              disabledBorder: InputBorder.none,
              // errorMaxLines: 2,
              contentPadding: const EdgeInsets.only(
                left: 15,
                bottom: 11,
                top: 11,
                right: 15,
              ),
              hintText: widget.hint,
              hintStyle: const TextStyle(
                color: Color(0xFF7B7B8B),
              ),
              suffixIcon: IconButton(
                onPressed: () {
                  setState(() {
                    _obscureText = !_obscureText;
                  });
                },
                icon: Icon(
                  _obscureText ? Icons.visibility : Icons.visibility_off,
                  color: AppCustomColors.grayishblue,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
