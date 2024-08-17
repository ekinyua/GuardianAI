// ignore_for_file: library_private_types_in_public_api, use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:guardian_ai/location/location.dart';
import 'package:guardian_ai/login/login.dart';
import 'package:guardian_ai/services/register_auth.dart';
import 'package:guardian_ai/widgets/phone_input_field.dart';
import 'package:page_transition/page_transition.dart';

import '../utils/utils.dart';
import '../widgets/password_input_field.dart';

class SignUpScreen extends StatefulWidget {
  const SignUpScreen({super.key});

  @override
  State<SignUpScreen> createState() => _SignUpScreenState();
}

class _SignUpScreenState extends State<SignUpScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _phoneNumberController = TextEditingController();
  final _emergencyContactController = TextEditingController();

  bool _isloading = false;

  void _submitForm() async {
    if (_formKey.currentState!.validate()) {
      Navigator.push(
        context,
        PageTransition(
          type: PageTransitionType.rightToLeft,
          child: const LocationScreen(),
        ),
      );
      setState(() {
        _isloading = true;
      });

      final user = RegisterAuth(
        name: _nameController.text,
        email: _emailController.text,
        password: _passwordController.text,
        phoneNumber: _phoneNumberController.text,
        emergencyContact: _emergencyContactController.text,
      );

      final userId = await UserRegistration.registerUser(user);

      setState(() {
        _isloading = false;
      });

      if (userId != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('User registered successfully',
                style: TextStyle(color: Colors.white, fontSize: 14)),
            backgroundColor: Color(0xff1E1E2D),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Registration failed. Please try again.',
                style: TextStyle(color: Colors.white, fontSize: 14)),
            backgroundColor: Color(0xff1E1E2D),
          ),
        );
      }
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _phoneNumberController.dispose();
    _emergencyContactController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    return SingleChildScrollView(
      scrollDirection: Axis.vertical,
      child: Column(
        children: [
          Image.asset('assets/logo.png'),
          Padding(
            padding: const EdgeInsets.all(20.0),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Sign Up',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 25.sp,
                      fontWeight: FontWeight.bold,
                      fontFamily: 'Poppins',
                    ),
                  ),
                  SizedBox(height: 18.h),
                  PhoneInputField(
                    controller: _nameController,
                    hint: "Enter you full name",
                    title: "Full name",
                    validator: (value) {
                      if (value!.isEmpty) {
                        return 'Please enter your full name';
                      }
                      return null;
                    },
                    textInputType: TextInputType.name,
                  ),
                  SizedBox(height: 10.h),
                  PhoneInputField(
                    controller: _emailController,
                    hint: 'marthargondo@gmail.com',
                    title: "Email",
                    validator: (value) {
                      if (!value!.isValidEmail) {
                        return 'Please enter a valid email';
                      }
                      return null;
                    },
                    textInputType: TextInputType.emailAddress,
                  ),
                  SizedBox(height: 10.h),
                  PasswordInputField(
                    controller: _passwordController,
                    hint: '********',
                    title: "Password",
                    validator: (value) {
                      if (value!.isEmpty) {
                        return 'Please enter a password';
                      }
                      return null;
                    },
                    textInputType: TextInputType.visiblePassword,
                  ),
                  SizedBox(height: 10.h),
                  PhoneInputField(
                    controller: _phoneNumberController,
                    hint: 'Enter your phone number',
                    title: "Phone Number",
                    validator: (value) {
                      if (value!.isEmpty) {
                        return 'Please enter your phone number';
                      }
                      return null;
                    },
                    textInputType: TextInputType.phone,
                  ),
                  SizedBox(height: 10.h),
                  PhoneInputField(
                    controller: _emergencyContactController,
                    hint: 'Enter your emergency contact',
                    title: "Emergency Contact",
                    validator: (value) {
                      if (value!.isEmpty) {
                        return 'Please enter your emergency contact';
                      }
                      return null;
                    },
                    textInputType: TextInputType.phone,
                  ),
                  SizedBox(height: 30.h),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 20.w),
                    child: ElevatedButton(
                      onPressed: _isloading ? null : _submitForm,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        minimumSize: const Size(double.infinity, 50),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      child: _isloading
                          ? const CircularProgressIndicator()
                          : Text(
                              'SignUp',
                              style: TextStyle(
                                  fontSize: 16.sp,
                                  fontWeight: FontWeight.w500,
                                  fontFamily: 'Poppins',
                                  color: Colors.white),
                            ),
                    ),
                  ),
                  SizedBox(height: 10.h),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Already have an account?',
                        style: TextStyle(
                          color: const Color(0xffCDCDE0),
                          fontSize: 14.sp,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      TextButton(
                        onPressed: () {
                          Navigator.push(
                              context,
                              PageTransition(
                                  type: PageTransitionType.leftToRight,
                                  child: const LoginScreen()));
                        },
                        child: Text(
                          'Login',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 14.sp,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}

/*
SafeArea(
      child: Scaffold(
        body: SingleChildScrollView(
          scrollDirection: Axis.vertical,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Image.asset('assets/logo.png'),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Sign Up',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 25.sp,
                        fontWeight: FontWeight.bold,
                        fontFamily: 'Poppins',
                      ),
                    ),
                    SizedBox(height: 18.h),
                    Text('Username',
                        style: TextStyle(
                          color: const Color(0xFFCDCDE0),
                          fontSize: 14.sp,
                          fontWeight: FontWeight.w500,
                        )),
                    SizedBox(height: 5.h),
                    Container(
                      padding: const EdgeInsets.all(5),
                      decoration: BoxDecoration(
                        color: const Color(0xff1E1E2D),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: TextFormField(
                        style: const TextStyle(color: Colors.white),
                        cursorColor: Colors.white,
                        decoration: const InputDecoration(
                          border: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          errorBorder: InputBorder.none,
                          disabledBorder: InputBorder.none,
                          errorMaxLines: 3,
                          hintText: 'Your Unique Username',
                          hintStyle: TextStyle(
                            color: Color(0xFF7B7B8B),
                          ),
                          contentPadding: EdgeInsets.only(
                            left: 15,
                            bottom: 11,
                            top: 11,
                            right: 15,
                          ),
                        ),
                      ),
                    ),
                    SizedBox(height: 10.h),
                    Text('Email',
                        style: TextStyle(
                          color: const Color(0xFFCDCDE0),
                          fontSize: 14.sp,
                          fontWeight: FontWeight.w500,
                        )),
                    SizedBox(height: 5.h),
                    Container(
                      padding: const EdgeInsets.all(5),
                      decoration: BoxDecoration(
                        color: const Color(0xff1E1E2D),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: TextFormField(
                        style: const TextStyle(color: Colors.white),
                        cursorColor: Colors.white,
                        decoration: const InputDecoration(
                          border: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          errorBorder: InputBorder.none,
                          disabledBorder: InputBorder.none,
                          errorMaxLines: 3,
                          hintText: 'marthargondo@gmail.com',
                          hintStyle: TextStyle(
                            color: Color(0xFF7B7B8B),
                          ),
                          contentPadding: EdgeInsets.only(
                            left: 15,
                            bottom: 11,
                            top: 11,
                            right: 15,
                          ),
                        ),
                      ),
                    ),
                    SizedBox(height: 10.h),
                    Text(
                      'Password',
                      style: TextStyle(
                        color: const Color(0xFFCDCDE0),
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
                        style: const TextStyle(color: Colors.white),
                        cursorColor: Colors.white,
                        keyboardType: TextInputType.visiblePassword,
                        decoration: const InputDecoration(
                          border: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          errorBorder: InputBorder.none,
                          disabledBorder: InputBorder.none,
                          errorMaxLines: 3,
                          hintText: '********',
                          hintStyle: TextStyle(
                            color: Color(0xFF7B7B8B),
                          ),
                          // suffix: IconButton(
                          //   icon: Icon(
                          //     _obscurePassword
                          //         ? Icons.visibility
                          //         : Icons.visibility_off,
                          //     color: const Color(0xFF7B7B8B),
                          //   ),
                          //   onPressed: () {
                          //     setState(() {
                          //       _obscurePassword = !_obscurePassword;
                          //     });
                          //   },
                          // ),
                          // hintStyle: const TextStyle(
                          //   color: Color(0xFF7B7B8B),
                          // ),
                          contentPadding: EdgeInsets.only(
                            left: 15,
                            bottom: 11,
                            top: 11,
                            right: 15,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              SizedBox(height: 30.h),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 20.w),
                child: ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                        context,
                        PageTransition(
                            type: PageTransitionType.rightToLeft,
                            child: const LocationScreen()));
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    minimumSize: const Size(double.infinity, 50),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: Text(
                    'SignUp',
                    style: TextStyle(
                        fontSize: 16.sp,
                        fontWeight: FontWeight.w500,
                        fontFamily: 'Poppins',
                        color: Colors.white),
                  ),
                ),
              ),
              SizedBox(height: 10.h),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'Already have an account?',
                    style: TextStyle(
                      color: const Color(0xffCDCDE0),
                      fontSize: 14.sp,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  TextButton(
                    onPressed: () {
                      Navigator.push(
                          context,
                          PageTransition(
                              type: PageTransitionType.leftToRight,
                              child: const LoginScreen()));
                    },
                    child: Text(
                      'Login',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 14.sp,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
    */
