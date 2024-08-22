// ignore_for_file: avoid_print

import 'dart:convert';
import 'package:http/http.dart' as http;

class LoginCredentials {
  final String email;
  final String password;

  LoginCredentials({
    required this.email,
    required this.password,
  });

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
    };
  }
}

class LoginResponse {
  final String message;
  final String? userId;

  LoginResponse({required this.message, this.userId});

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      message: json['message'],
      userId: json['user_id'],
    );
  }
}

class AuthService {
  static const String apiUrl =
      'https://guardian-backend-6lro.onrender.com/api/v1/user/login';

  static Future<LoginResponse> login(LoginCredentials credentials) async {
    try {
      final response = await http.post(
        Uri.parse(apiUrl),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(credentials.toJson()),
      );

      if (response.statusCode == 200) {
        final responseData = jsonDecode(response.body);
        return LoginResponse.fromJson(responseData);
      } else if (response.statusCode == 401) {
        return LoginResponse(message: 'Invalid credentials');
      } else {
        final errorData = jsonDecode(response.body);
        throw Exception(errorData['error'] ?? 'An error occurred');
      }
    } catch (e) {
      print('Error logging in: $e');
      throw Exception('Failed to login. Please try again.');
    }
  }
}
