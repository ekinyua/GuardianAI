// ignore_for_file: avoid_print

import 'dart:convert';
import 'package:http/http.dart' as http;

class RegisterAuth {
  final String name;
  final String email;
  final String password;
  final String phoneNumber;
  final String emergencyContact;

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
        body: jsonEncode(
          user.toJson(),
        ),
      );

      if (response.statusCode == 200) {
        final responseData = jsonDecode(response.body);
        return responseData['user_id'];
      } else {
        final errorData = jsonDecode(response.body);
        throw Exception(errorData['Error']);
      }
    } catch (e) {
      print('Error registering user: $e');
      return null;
    }
  }
}
