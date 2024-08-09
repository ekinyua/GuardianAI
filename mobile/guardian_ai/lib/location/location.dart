import 'package:flutter/material.dart';
import 'package:guardian_ai/location/alerts/alerts.dart';
import 'package:guardian_ai/location/distresscall/distresscall.dart';
import 'package:guardian_ai/location/livelocation/livelocation.dart';
import 'package:guardian_ai/location/safewalk/safewalk.dart';

class LocationScreen extends StatefulWidget {
  const LocationScreen({super.key});

  @override
  State<LocationScreen> createState() => _LocationScreenState();
}

class _LocationScreenState extends State<LocationScreen> {
  int _currentIndex = 0;
  final List<Widget> _screens = const [
    LiveLocationScreen(),
    SafeWalkScreen(),
    AlertScreen(),
    DistressCallScreen()
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        selectedItemColor: const Color(0xffFFA001),
        unselectedItemColor: const Color(0xffCDCDE0),
        type: BottomNavigationBarType.fixed,
        backgroundColor: Colors.black,
        currentIndex: _currentIndex,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.location_on),
            label: 'Live Location',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.add_circle),
            label: 'Safe Walk',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: 'Alerts',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.turned_in_outlined),
            label: 'Distress Call',
          ),
        ],
      ),
    );
  }
}
