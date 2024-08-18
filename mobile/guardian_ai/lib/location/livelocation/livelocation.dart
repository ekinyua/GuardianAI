import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:guardian_ai/profile/profile.dart';
import 'package:location/location.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

class LiveLocationScreen extends StatefulWidget {
  const LiveLocationScreen({super.key});

  @override
  State<LiveLocationScreen> createState() => _LiveLocationScreenState();
}

class _LiveLocationScreenState extends State<LiveLocationScreen> {
  Location _locationController = new Location();
  static const LatLng _pNairobi = LatLng(-1.2921, 36.8219);
  LatLng? _currentP = null;

  @override
  void initState() {
    super.initState();
    getLocationUpdates();
  }

  String formatLocationForSharing() {
    if (_currentP == null) return "Location not available";
    return "My current location: https://www.google.com/maps/search/?api=1&query=${_currentP!.latitude},${_currentP!.longitude}";
  }

  void shareLocation() {
    String locationMessage = formatLocationForSharing();
    Share.share(locationMessage, subject: 'My Current Location');
  }

  void shareViaWhatsApp() async {
    String locationMessage = formatLocationForSharing();
    String whatsappUrl =
        "whatsapp://send?text=${Uri.encodeComponent(locationMessage)}";
    if (await canLaunch(whatsappUrl)) {
      await launch(whatsappUrl);
    } else {
      // WhatsApp not installed
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('WhatsApp is not installed on your device')),
      );
    }
  }

  void shareViaEmail() {
    String locationMessage = formatLocationForSharing();
    final Uri emailLaunchUri = Uri(
      scheme: 'mailto',
      path: '',
      query: encodeQueryParameters(<String, String>{
        'subject': 'My Current Location',
        'body': locationMessage
      }),
    );
    launch(emailLaunchUri.toString());
  }

  String? encodeQueryParameters(Map<String, String> params) {
    return params.entries
        .map((e) =>
            '${Uri.encodeComponent(e.key)}=${Uri.encodeComponent(e.value)}')
        .join('&');
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: const Color(0xff000000),
          leading: IconButton(
            icon: Icon(Icons.close, color: Colors.white, size: 24.r),
            onPressed: () {
              Navigator.pop(context);
            },
          ),
          title: Text(
            'Live Location sharings',
            style: TextStyle(
              fontSize: 20.sp,
              color: Colors.white,
              fontWeight: FontWeight.bold,
            ),
          ),
          actions: [
            IconButton(
              icon: Icon(Icons.person, color: Colors.white, size: 24.r),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (context) => const ProfileScreen()),
                );
              },
            ),
          ],
        ),
        body: Column(
          children: [
            SizedBox(
              height: 300.h, // Adjust this height as needed
              child: _currentP == null
                  ? const Center(
                      child: Text('Loading...'),
                    )
                  : GoogleMap(
                      initialCameraPosition: CameraPosition(
                        target: _currentP!,
                        zoom: 13,
                      ),
                      markers: {
                          Marker(
                              markerId: MarkerId("_currentLocation"),
                              icon: BitmapDescriptor.defaultMarker,
                              position: _currentP!),
                          Marker(
                              markerId: MarkerId("_sourceLocation"),
                              icon: BitmapDescriptor.defaultMarker,
                              position: _pNairobi),
                        }),
            ),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 20.w, vertical: 10.h),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Share your location with....',
                        style: TextStyle(
                          fontSize: 20.sp,
                          fontWeight: FontWeight.bold,
                          color: const Color(0xffFFFFFF),
                        ),
                      ),
                      Text(
                        'Click to select',
                        style: TextStyle(
                          fontSize: 14.sp,
                          color: const Color(0xff9EB0BA),
                        ),
                      ),
                    ],
                  ),
                  IconButton(
                    onPressed: () {},
                    icon: const Icon(
                      Icons.arrow_forward_ios,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
            ),
            SizedBox(height: 30.h),
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(
                  Icons.error_outline,
                  color: Color(0xffFFFFFF),
                ),
                SizedBox(width: 10.w),
                Text(
                  'Send a distress signal',
                  style: TextStyle(
                    fontSize: 16.sp,
                    color: const Color(0xffFFFFFF),
                  ),
                ),
              ],
            ),
            SizedBox(height: 30.h),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 20.w),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  ElevatedButton.icon(
                    onPressed: shareLocation,
                    icon: Icon(Icons.share, color: Colors.white),
                    label: Text('Share'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Color(0xff129CED),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10.r),
                      ),
                    ),
                  ),
                  ElevatedButton.icon(
                    onPressed: shareViaWhatsApp,
                    icon: Icon(Icons.share, color: Colors.white),
                    label: Text('WhatsApp'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10.r),
                      ),
                    ),
                  ),
                  ElevatedButton.icon(
                    onPressed: shareViaEmail,
                    icon: Icon(Icons.email, color: Colors.white),
                    label: Text('Email'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10.r),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> getLocationUpdates() async {
    bool _serviceEnabled;
    PermissionStatus _permissionGranted;

    _serviceEnabled = await _locationController.serviceEnabled();
    if (_serviceEnabled) {
      _serviceEnabled = await _locationController.requestService();
    } else {
      return;
    }

    _permissionGranted = await _locationController.hasPermission();
    if (_permissionGranted == PermissionStatus.denied) {
      _permissionGranted = await _locationController.requestPermission();
      if (_permissionGranted != PermissionStatus.granted) {
        return;
      }
    }

    _locationController.onLocationChanged
        .listen((LocationData currentLocation) {
      if (currentLocation.latitude != null &&
          currentLocation.longitude != null) {
        setState(() {
          _currentP =
              LatLng(currentLocation.latitude!, currentLocation.longitude!);
          print(_currentP);
        });
      }
    });
  }
}
