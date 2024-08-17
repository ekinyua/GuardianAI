import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _phoneController;
  List<TextEditingController> _emergencyContactControllers = [];

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController();
    _phoneController = TextEditingController();
    // Initialize with one emergency contact
    _emergencyContactControllers.add(TextEditingController());
    // TO-DO: Fetch current user data and populate fields
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    for (var controller in _emergencyContactControllers) {
      controller.dispose();
    }
    super.dispose();
  }

  void _addEmergencyContact() {
    setState(() {
      _emergencyContactControllers.add(TextEditingController());
    });
  }

  void _removeEmergencyContact(int index) {
    setState(() {
      _emergencyContactControllers.removeAt(index);
    });
  }

  Future<void> _updateProfile() async {
    if (_formKey.currentState!.validate()) {
      // TO-DO: Replace with actual user_id
      String userId = 'user_id_here';
      var url = Uri.parse('your_api_base_url/profile');
      var response = await http.put(
        url,
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'user_id': userId,
          'name': _nameController.text,
          'phone_number': _phoneController.text,
          'emergency_contacts': _emergencyContactControllers
              .map((controller) => controller.text)
              .toList(),
        }),
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Profile updated successfully')),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to update profile')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SafeWalk'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
      ),
      body: Container(
        color: Colors.black,
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Form(
            key: _formKey,
            child: ListView(
              children: [
                const Text(
                  'Profile',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 20),
                _buildTextField('Name', _nameController),
                _buildTextField('Phone Number', _phoneController),
                const SizedBox(height: 20),
                const Text(
                  'Emergency Contacts',
                  style: TextStyle(color: Colors.white, fontSize: 18),
                ),
                ...List.generate(
                  _emergencyContactControllers.length,
                  (index) => _buildEmergencyContactField(index),
                ),
                TextButton(
                  onPressed: _addEmergencyContact,
                  child: const Text('+ Add Emergency Contact'),
                ),
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: _updateProfile,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  child: const Text('Save'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTextField(String label, TextEditingController controller) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        controller: controller,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          labelText: label,
          labelStyle: TextStyle(color: Colors.grey[400]),
          enabledBorder: const UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.grey),
          ),
          focusedBorder: const UnderlineInputBorder(
            borderSide: BorderSide(color: Colors.blue),
          ),
        ),
        validator: (value) {
          if (value == null || value.isEmpty) {
            return 'Please enter $label';
          }
          return null;
        },
      ),
    );
  }

  Widget _buildEmergencyContactField(int index) {
    return Row(
      children: [
        Expanded(
          child: _buildTextField(
            'Emergency Contact ${index + 1}',
            _emergencyContactControllers[index],
          ),
        ),
        IconButton(
          icon: const Icon(Icons.remove_circle, color: Colors.red),
          onPressed: () => _removeEmergencyContact(index),
        ),
      ],
    );
  }
}