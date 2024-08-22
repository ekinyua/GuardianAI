import 'package:flutter/material.dart';

class AlertScreen extends StatefulWidget {
  const AlertScreen({super.key});

  @override
  State<AlertScreen> createState() => _AlertScreenState();
}

class _AlertScreenState extends State<AlertScreen> {
  bool highCrimeAlert = false;
  bool notifyContacts = false;
  bool unusualRouteAlert = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SafeWalk'),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {
              // Add settings functionality here
            },
          ),
        ],
      ),
      body: Container(
        color: Colors.black,
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Alerts & Notifications',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 20),
              _buildAlertTile(
                'Get alerts when you\'re near a',
                'If you are walking, biking or driving through a high crime area, we will',
                highCrimeAlert,
                (value) => setState(() => highCrimeAlert = value),
              ),
              _buildAlertTile(
                'We\'ll notify your contacts if',
                'If you\'re walking, biking or driving and don\'t reach your destination',
                notifyContacts,
                (value) => setState(() => notifyContacts = value),
              ),
              _buildAlertTile(
                'We\'ll alert you of unusual',
                'If you\'re walking, biking or driving and take an unusual route, we\'ll',
                unusualRouteAlert,
                (value) => setState(() => unusualRouteAlert = value),
              ),
              const Spacer(),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () {
                    // Add save functionality here
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                  ),
                  child: const Text('Save'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAlertTile(String title, String subtitle, bool value, ValueChanged<bool> onChanged) {
    return ListTile(
      leading: Icon(
        _getIconForTitle(title),
        color: Colors.white,
      ),
      title: Text(
        title,
        style: const TextStyle(color: Colors.white),
      ),
      subtitle: Text(
        subtitle,
        style: TextStyle(color: Colors.grey[400]),
      ),
      trailing: Switch(
        value: value,
        onChanged: onChanged,
        activeColor: Colors.blue,
      ),
    );
  }

  IconData _getIconForTitle(String title) {
    if (title.contains('alerts when you\'re near')) {
      return Icons.shield_outlined;
    } else if (title.contains('notify your contacts')) {
      return Icons.directions_walk;
    } else {
      return Icons.location_on_outlined;
    }
  }
}