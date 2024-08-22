// ignore_for_file: avoid_print

import 'dart:convert';
import 'package:http/http.dart' as http;

void log(String message, {String level = 'INFO'}) {
  print('[$level] $message');
}

class RegisterAuth {
  final String name;
  final String email;
  final String password;
  final String phoneNumber;
  final List emergencyContact;

  RegisterAuth({
    required this.name,
    required this.email,
    required this.password,
    required this.phoneNumber,
    required this.emergencyContact,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'email': email,
      'password': password,
      'phoneNumber': phoneNumber,
      'emergencyContact': emergencyContact,
    };
  }
}

class UserRegistration {
  static const String apiUrl =
      'https://guardian-backend-6lro.onrender.com/api/v1/user/register';

  static Future<String?> registerUser(RegisterAuth user) async {
    try {
      final response = await http.post(
        Uri.parse(apiUrl),
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(user.toJson()),
      );

      // log('API URL: $apiUrl');
      // log('Request Body: ${jsonEncode(user.toJson())}');
      // log('Status Code: ${response.statusCode}');
      // log('Response Body: ${response.body}');

      if (response.statusCode == 201) {
        final responseData = jsonDecode(response.body);
        return responseData['user_id'];
      } else if (response.statusCode == 400) {
        final errorData = jsonDecode(response.body);
        if (errorData['error'] != null &&
            errorData['error'].contains('duplicate key error')) {
          log('Email is already registered. Please use a different email.',
              level: 'WARNING');
        } else if (errorData['error'] != null) {
          log('Error: ${errorData['error']}', level: 'WARNING');
        } else {
          log('Unknown error occurred.', level: 'WARNING');
        }
        throw Exception(errorData['error']);
      } else {
        log('Unexpected status code: ${response.statusCode}', level: 'SEVERE');
        throw Exception('Unexpected error occurred.');
      }
    } catch (e) {
      log('Error registering user: $e', level: 'SEVERE');
      return null;
    }
  }
}
