package com.example.guardianai.activities;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.guardianai.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SafeRoute extends AppCompatActivity implements OnMapReadyCallback {

    private static final String API_URL = "https://guardianai-m2kc.onrender.com/find_safest_route";
    private static final LatLng KENYA_CENTER = new LatLng(1.2921, 36.8219); // Coordinates of Nairobi, Kenya
    private static final float DEFAULT_ZOOM = 6.0f; // Zoom level for Kenya
    private static final String TAG = "SafeRoute"; // Tag for logging
    private TextView loadingMessage;
    private TextView displayValues;
    private GoogleMap mMap;
    private final Map<String, LatLng> townCoordinates = new HashMap<>();
    private final Map<String, LatLng> dangerZones = new HashMap<>();

    // Define a threshold distance (in meters) to trigger the alert
    private static final float DANGER_ZONE_THRESHOLD_METERS = 1000.0f; // 1 km

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_route);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check user's location periodically
        checkUserLocation();


        // Enable the back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the default title to use custom title
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize UI components
        EditText originInput = findViewById(R.id.origin_input);
        EditText destinationInput = findViewById(R.id.destination_input);
        EditText hourInput = findViewById(R.id.hour_input);
        Spinner dayOfWeekSpinner = findViewById(R.id.day_of_week_spinner);
        Button findSafestRouteButton = findViewById(R.id.find_safest_route_button);
        displayValues = findViewById(R.id.display_values);
        loadingMessage = findViewById(R.id.loading_message);

        // Set up Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        // Initialize town coordinates
        initializeTownCoordinates();
        initializeDangerZones();

        // Set up Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up button click listener
        findSafestRouteButton.setOnClickListener(v -> {
            String origin = originInput.getText().toString().trim();
            String destination = destinationInput.getText().toString().trim();
            String dayOfWeekString = dayOfWeekSpinner.getSelectedItem().toString();
            String hourText = hourInput.getText().toString().trim();

            int dayOfWeek = 0;
            try {
                dayOfWeek = dayOfWeekSpinner.getSelectedItemPosition(); // Get index of selected day
            } catch (Exception e) {
                // Handle invalid day of week input
                dayOfWeek = 0; // Default to Sunday
                Log.e(TAG, "Invalid day of week input", e);
            }

            int hour = 0;
            try {
                hour = Integer.parseInt(hourText);
                if (hour < 0 || hour > 23) {
                    hour = 0; // Set to default value or handle as needed
                }
            } catch (NumberFormatException e) {
                // Handle invalid hour input
                hour = 0; // Set to default value or handle as needed
                Log.e(TAG, "Invalid hour input", e);
            }

            // Create JSON request body
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("origin", origin);
                jsonBody.put("destination", destination);
                jsonBody.put("day_of_week", dayOfWeek);
                jsonBody.put("hour_of_day", hour);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error creating JSON request body", e);
            }

            loadingMessage.setVisibility(View.VISIBLE);

            // Increase timeout settings
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase connection timeout
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase write timeout
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // Increase read timeout
                    .build();

            RequestBody body = RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        loadingMessage.setVisibility(View.GONE);
                        Toast.makeText(SafeRoute.this, "Failed to get response", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API request failed", e);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        loadingMessage.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                JSONArray routeDescriptionArray = jsonResponse.getJSONArray("route_description");
                                JSONArray safestPathArray = jsonResponse.getJSONArray("safest_path");

                                // Concatenate all route descriptions
                                StringBuilder routeDescription = new StringBuilder();
                                for (int i = 0; i < routeDescriptionArray.length(); i++) {
                                    routeDescription.append(routeDescriptionArray.optString(i)).append("\n");
                                }

                                // Parse route description
//                                String routeDescription = routeDescriptionArray.length() > 0 ? routeDescriptionArray.getString(0) : "No description available";

                                // Parse safest path
                                if (mMap != null) {
                                    plotRouteOnMap(safestPathArray);
                                }

                                displayValues.setVisibility(View.VISIBLE);
                                displayValues.setText("Route Description: " + routeDescription);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error parsing JSON response", e);
                                displayValues.setVisibility(View.VISIBLE);
                                displayValues.setText("Error parsing response");
                            }
                        } else {
                            Log.e(TAG, "API response failed: " + response.message());
                            displayValues.setVisibility(View.VISIBLE);
                            displayValues.setText("Request failed: " + response.message());
                        }
                    });
                }
            });
        });
    }

    private void checkUserLocation() {
        // Get the user's current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    checkProximityToDangerZones(userLatLng);
                }
            }
        });
    }

    private void checkProximityToDangerZones(LatLng userLocation) {
        for (Map.Entry<String, LatLng> entry : dangerZones.entrySet()) {
            LatLng dangerZoneLatLng = entry.getValue();
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude,
                    dangerZoneLatLng.latitude, dangerZoneLatLng.longitude, results);
            float distance = results[0];

            if (distance < DANGER_ZONE_THRESHOLD_METERS) {
                // Alert the user if within the danger zone threshold
                Toast.makeText(this, "Warning: Approaching danger zone " + entry.getKey(), Toast.LENGTH_LONG).show();
                break; // Alert once per check
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Center the map around Kenya
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KENYA_CENTER, DEFAULT_ZOOM));

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        displayDangerZones();
    }

    private void initializeTownCoordinates() {
        // Initialize town coordinates here
        townCoordinates.put("Nairobi", new LatLng(-1.286389, 36.817223));
        townCoordinates.put("Mombasa", new LatLng(-4.043477, 39.668206));
        townCoordinates.put("Nakuru", new LatLng(-0.303099, 36.066285));
        townCoordinates.put("Eldoret", new LatLng(0.514277, 35.269065));
        townCoordinates.put("Kisumu", new LatLng(-0.091702, 34.767956));
        townCoordinates.put("Nyeri", new LatLng(-0.421250, 36.945752));
        townCoordinates.put("Machakos", new LatLng(-1.516820, 37.266485));
        townCoordinates.put("Kericho", new LatLng(-0.366690, 35.291340));
        townCoordinates.put("Meru", new LatLng(-0.047200, 37.648000));
        townCoordinates.put("Kitale", new LatLng(1.002559, 34.986032));
        townCoordinates.put("Garissa", new LatLng(-0.456550, 39.664640));
        townCoordinates.put("Isiolo", new LatLng(0.353073, 37.582666));
        townCoordinates.put("Bungoma", new LatLng(0.591278, 34.564658));
        townCoordinates.put("Wajir", new LatLng(1.737327, 40.065940));
        townCoordinates.put("Mandera", new LatLng(3.930530, 41.855910));
        townCoordinates.put("Malindi", new LatLng(-3.219186, 40.116944));
        townCoordinates.put("Lamu", new LatLng(-2.271189, 40.902012));
        townCoordinates.put("Thika", new LatLng(-1.033349, 37.069328));
        townCoordinates.put("Namanga", new LatLng(-2.545290, 36.792530));
        townCoordinates.put("Kitui", new LatLng(-1.374818, 38.010555));
        townCoordinates.put("Naivasha", new LatLng(-0.707222, 36.431944));
        townCoordinates.put("Narok", new LatLng(-1.078850, 35.860000));
        townCoordinates.put("Busia", new LatLng(0.4605, 34.1115));
        townCoordinates.put("Bomet", new LatLng(-0.7827, 35.3428));
        townCoordinates.put("Marsabit", new LatLng(2.3342, 37.9891));
        townCoordinates.put("Voi", new LatLng(-3.3962, 38.5565));
    }

    private void plotRouteOnMap(JSONArray safestPathArray) {

        if (safestPathArray.length() < 2) {
            Log.e(TAG, "Insufficient data to plot route");
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions().color(getResources().getColor(R.color.button_color)).width(5);

        for (int i = 0; i < safestPathArray.length(); i++) {
            String townName = safestPathArray.optString(i);
            LatLng coordinates = townCoordinates.get(townName);
            if (coordinates != null) {
                polylineOptions.add(coordinates);
            } else {
                Log.e(TAG, "Town coordinates not found for: " + townName);
            }
        }

        if (mMap != null) {

            mMap.clear(); // Clear previous markers and routes
            mMap.addPolyline(polylineOptions);
            displayDangerZones();

            // Zoom to fit the polyline
            if (polylineOptions.getPoints().size() > 0) {
                LatLng firstPoint = polylineOptions.getPoints().get(0);
                LatLng lastPoint = polylineOptions.getPoints().get(polylineOptions.getPoints().size() - 1);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(firstPoint);
                builder.include(lastPoint);
                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }

    private void displayDangerZones() {
        for (Map.Entry<String, LatLng> entry : dangerZones.entrySet()) {
            LatLng dangerZoneLatLng = entry.getValue();

            // Draw a circle around each danger zone
            Circle dangerZoneCircle = mMap.addCircle(new CircleOptions()
                    .center(dangerZoneLatLng)
                    .radius(500) // Radius in meters
                    .strokeColor(0xFFFF0000) // Red color
                    .fillColor(0x44FF0000)); // Translucent red

            // Add a marker with the danger zone name
            mMap.addMarker(new MarkerOptions()
                    .position(dangerZoneLatLng)
                    .title("Danger Zone: " + entry.getKey()));

            // Optional: Add a click listener to display danger zone information
            mMap.setOnMarkerClickListener(marker -> {
                String markerTitle = marker.getTitle();
                Toast.makeText(this, markerTitle, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }
    private void initializeDangerZones() {
        // Add specific danger zone coordinates
        dangerZones.put("Zone 1", new LatLng(-1.3032, 36.8238)); // Nairobi Central
        dangerZones.put("Zone 2", new LatLng(-1.2864, 36.8172)); // Nairobi CBD
        dangerZones.put("Zone 3", new LatLng(-1.2921, 36.8219)); // Nairobi University
        dangerZones.put("Zone 4", new LatLng(-1.3078, 36.8261)); // Westlands
        dangerZones.put("Zone 5", new LatLng(-4.0435, 39.6682)); // Mombasa Central
        dangerZones.put("Zone 6", new LatLng(-0.3031, 36.0663)); // Nakuru Central
        dangerZones.put("Zone 7", new LatLng(0.5143, 35.2691)); // Eldoret Central
        dangerZones.put("Zone 8", new LatLng(-0.0917, 34.7679)); // Kisumu Central
        dangerZones.put("Zone 9", new LatLng(-0.4213, 36.9458)); // Nyeri Central
        dangerZones.put("Zone 10", new LatLng(-1.5168, 37.2665)); // Machakos Central
        dangerZones.put("Zone 11", new LatLng(-0.3667, 35.2913)); // Kericho Central
        dangerZones.put("Zone 12", new LatLng(-0.0472, 37.6480)); // Meru Central
        dangerZones.put("Zone 13", new LatLng(1.0026, 34.9860)); // Kitale Central
        dangerZones.put("Zone 14", new LatLng(-0.4566, 39.6646)); // Garissa Central
        dangerZones.put("Zone 15", new LatLng(0.3531, 37.5827)); // Isiolo Central
        dangerZones.put("Zone 16", new LatLng(0.5913, 34.5647)); // Bungoma Central
        dangerZones.put("Zone 17", new LatLng(1.7373, 40.0659)); // Wajir Central
        dangerZones.put("Zone 18", new LatLng(3.9305, 41.8559)); // Mandera Central
        dangerZones.put("Zone 19", new LatLng(-3.2192, 40.1169)); // Malindi Central
        dangerZones.put("Zone 20", new LatLng(-2.2712, 40.9020)); // Lamu Central
        dangerZones.put("Zone 21", new LatLng(-1.0333, 37.0693)); // Thika Central
        dangerZones.put("Zone 22", new LatLng(-2.5453, 36.7925)); // Namanga Central
        dangerZones.put("Zone 23", new LatLng(-1.3748, 38.0106)); // Kitui Central
        dangerZones.put("Zone 24", new LatLng(-0.7072, 36.4319)); // Naivasha Central
        dangerZones.put("Zone 25", new LatLng(-1.0789, 35.8600)); // Narok Central
        dangerZones.put("Zone 26", new LatLng(0.4605, 34.1115)); // Busia Central
        dangerZones.put("Zone 27", new LatLng(-0.7827, 35.3428)); // Bomet Central
        dangerZones.put("Zone 28", new LatLng(2.3342, 37.9891)); // Marsabit Central
        dangerZones.put("Zone 29", new LatLng(-3.3962, 38.5565)); // Voi Central
        dangerZones.put("Zone 30", new LatLng(-0.4292, 39.6465)); // Kilifi Central
        dangerZones.put("Zone 31", new LatLng(-0.4680, 39.6524)); // Kaloleni
        dangerZones.put("Zone 32", new LatLng(-1.2929, 36.7925)); // Kawangware, Nairobi
        dangerZones.put("Zone 33", new LatLng(-1.3176, 36.8355)); // Kibera, Nairobi
        dangerZones.put("Zone 34", new LatLng(-1.2805, 36.8164)); // Gikomba Market, Nairobi
        dangerZones.put("Zone 35", new LatLng(-1.2419, 36.8837)); // Kasarani, Nairobi
        dangerZones.put("Zone 36", new LatLng(-1.2417, 36.8588)); // Roysambu, Nairobi
        dangerZones.put("Zone 37", new LatLng(-1.2793, 36.8244)); // Ngara, Nairobi
        dangerZones.put("Zone 38", new LatLng(-1.3064, 36.8143)); // Mathare, Nairobi
        dangerZones.put("Zone 39", new LatLng(-1.3161, 36.8427)); // Dandora, Nairobi
        dangerZones.put("Zone 40", new LatLng(-1.2217, 36.8924)); // Kahawa West, Nairobi
        dangerZones.put("Zone 41", new LatLng(-1.3621, 36.8448)); // Umoja, Nairobi
        dangerZones.put("Zone 42", new LatLng(-1.2894, 36.8593)); // South B, Nairobi
        dangerZones.put("Zone 43", new LatLng(-1.3296, 36.8882)); // Pipeline, Nairobi
        dangerZones.put("Zone 44", new LatLng(-1.2993, 36.8962)); // Embakasi, Nairobi
        dangerZones.put("Zone 45", new LatLng(-1.3271, 36.8332)); // Ruaraka, Nairobi
        dangerZones.put("Zone 46", new LatLng(-1.3012, 36.8634)); // Donholm, Nairobi
        dangerZones.put("Zone 47", new LatLng(-1.2852, 36.8187)); // Globe Roundabout, Nairobi
        dangerZones.put("Zone 48", new LatLng(-1.2935, 36.8071)); // Nairobi River, Nairobi
        dangerZones.put("Zone 49", new LatLng(-1.2942, 36.7914)); // Westlands, Nairobi
        dangerZones.put("Zone 50", new LatLng(-1.2983, 36.7931)); // Parklands, Nairobi
    }

}







//package com.example.guardianai.activities;
//
//
//
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import com.example.guardianai.R;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.Circle;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class SafeRoute extends AppCompatActivity implements OnMapReadyCallback {
//
//    private static final String API_URL = "https://guardianai-m2kc.onrender.com/find_safest_route";
//    private static final LatLng KENYA_CENTER = new LatLng(1.2921, 36.8219); // Coordinates of Nairobi, Kenya
//    private static final float DEFAULT_ZOOM = 6.0f; // Zoom level for Kenya
//    private static final String TAG = "SafeRoute"; // Tag for logging
//    private TextView loadingMessage;
//    private TextView displayValues;
//    private GoogleMap mMap;
//    private final Map<String, LatLng> townCoordinates = new HashMap<>();
//    private final Map<String, LatLng> dangerZones = new HashMap<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_safe_route);
//
//        // Set up toolbar with back button
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        // Enable the back arrow in the toolbar
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the default title to use custom title
//        }
//
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//
//        // Initialize UI components
//        EditText originInput = findViewById(R.id.origin_input);
//        EditText destinationInput = findViewById(R.id.destination_input);
//        EditText hourInput = findViewById(R.id.hour_input);
//        Spinner dayOfWeekSpinner = findViewById(R.id.day_of_week_spinner);
//        Button findSafestRouteButton = findViewById(R.id.find_safest_route_button);
//        displayValues = findViewById(R.id.display_values);
//        loadingMessage = findViewById(R.id.loading_message);
//
//        // Set up Spinner
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        dayOfWeekSpinner.setAdapter(adapter);
//
//        // Initialize town coordinates
//        initializeTownCoordinates();
//        initializeDangerZones();
//
//        // Set up Map
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//
//        // Set up button click listener
//        findSafestRouteButton.setOnClickListener(v -> {
//            String origin = originInput.getText().toString().trim();
//            String destination = destinationInput.getText().toString().trim();
//            String dayOfWeekString = dayOfWeekSpinner.getSelectedItem().toString();
//            String hourText = hourInput.getText().toString().trim();
//
//            int dayOfWeek = 0;
//            try {
//                dayOfWeek = dayOfWeekSpinner.getSelectedItemPosition(); // Get index of selected day
//            } catch (Exception e) {
//                // Handle invalid day of week input
//                dayOfWeek = 0; // Default to Sunday
//                Log.e(TAG, "Invalid day of week input", e);
//            }
//
//            int hour = 0;
//            try {
//                hour = Integer.parseInt(hourText);
//                if (hour < 0 || hour > 23) {
//                    hour = 0; // Set to default value or handle as needed
//                }
//            } catch (NumberFormatException e) {
//                // Handle invalid hour input
//                hour = 0; // Set to default value or handle as needed
//                Log.e(TAG, "Invalid hour input", e);
//            }
//
//            // Create JSON request body
//            JSONObject jsonBody = new JSONObject();
//            try {
//                jsonBody.put("origin", origin);
//                jsonBody.put("destination", destination);
//                jsonBody.put("day_of_week", dayOfWeek);
//                jsonBody.put("hour_of_day", hour);
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Log.e(TAG, "Error creating JSON request body", e);
//            }
//
//            loadingMessage.setVisibility(View.VISIBLE);
//
//            // Increase timeout settings
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase connection timeout
//                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Increase write timeout
//                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // Increase read timeout
//                    .build();
//
//            RequestBody body = RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
//            Request request = new Request.Builder()
//                    .url(API_URL)
//                    .post(body)
//                    .build();
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    runOnUiThread(() -> {
//                        loadingMessage.setVisibility(View.GONE);
//                        Toast.makeText(SafeRoute.this, "Failed to get response", Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, "API request failed", e);
//                    });
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String responseBody = response.body().string();
//                    runOnUiThread(() -> {
//                        loadingMessage.setVisibility(View.GONE);
//
//                        if (response.isSuccessful()) {
//                            try {
//                                JSONObject jsonResponse = new JSONObject(responseBody);
//                                JSONArray routeDescriptionArray = jsonResponse.getJSONArray("route_description");
//                                JSONArray safestPathArray = jsonResponse.getJSONArray("safest_path");
//
//                                // Concatenate all route descriptions
//                                StringBuilder routeDescription = new StringBuilder();
//                                for (int i = 0; i < routeDescriptionArray.length(); i++) {
//                                    routeDescription.append(routeDescriptionArray.optString(i)).append("\n");
//                                }
//
//                                // Parse route description
////                                String routeDescription = routeDescriptionArray.length() > 0 ? routeDescriptionArray.getString(0) : "No description available";
//
//                                // Parse safest path
//                                if (mMap != null) {
//                                    plotRouteOnMap(safestPathArray);
//                                }
//
//                                displayValues.setVisibility(View.VISIBLE);
//                                displayValues.setText("Route Description: " + routeDescription);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                Log.e(TAG, "Error parsing JSON response", e);
//                                displayValues.setVisibility(View.VISIBLE);
//                                displayValues.setText("Error parsing response");
//                            }
//                        } else {
//                            Log.e(TAG, "API response failed: " + response.message());
//                            displayValues.setVisibility(View.VISIBLE);
//                            displayValues.setText("Request failed: " + response.message());
//                        }
//                    });
//                }
//            });
//        });
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // Center the map around Kenya
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KENYA_CENTER, DEFAULT_ZOOM));
//        displayDangerZones();
//    }
//
//    private void initializeTownCoordinates() {
//        // Initialize town coordinates here
//        townCoordinates.put("Nairobi", new LatLng(-1.286389, 36.817223));
//        townCoordinates.put("Mombasa", new LatLng(-4.043477, 39.668206));
//        townCoordinates.put("Nakuru", new LatLng(-0.303099, 36.066285));
//        townCoordinates.put("Eldoret", new LatLng(0.514277, 35.269065));
//        townCoordinates.put("Kisumu", new LatLng(-0.091702, 34.767956));
//        townCoordinates.put("Nyeri", new LatLng(-0.421250, 36.945752));
//        townCoordinates.put("Machakos", new LatLng(-1.516820, 37.266485));
//        townCoordinates.put("Kericho", new LatLng(-0.366690, 35.291340));
//        townCoordinates.put("Meru", new LatLng(-0.047200, 37.648000));
//        townCoordinates.put("Kitale", new LatLng(1.002559, 34.986032));
//        townCoordinates.put("Garissa", new LatLng(-0.456550, 39.664640));
//        townCoordinates.put("Isiolo", new LatLng(0.353073, 37.582666));
//        townCoordinates.put("Bungoma", new LatLng(0.591278, 34.564658));
//        townCoordinates.put("Wajir", new LatLng(1.737327, 40.065940));
//        townCoordinates.put("Mandera", new LatLng(3.930530, 41.855910));
//        townCoordinates.put("Malindi", new LatLng(-3.219186, 40.116944));
//        townCoordinates.put("Lamu", new LatLng(-2.271189, 40.902012));
//        townCoordinates.put("Thika", new LatLng(-1.033349, 37.069328));
//        townCoordinates.put("Namanga", new LatLng(-2.545290, 36.792530));
//        townCoordinates.put("Kitui", new LatLng(-1.374818, 38.010555));
//        townCoordinates.put("Naivasha", new LatLng(-0.707222, 36.431944));
//        townCoordinates.put("Narok", new LatLng(-1.078850, 35.860000));
//        townCoordinates.put("Busia", new LatLng(0.4605, 34.1115));
//        townCoordinates.put("Bomet", new LatLng(-0.7827, 35.3428));
//        townCoordinates.put("Marsabit", new LatLng(2.3342, 37.9891));
//        townCoordinates.put("Voi", new LatLng(-3.3962, 38.5565));
//    }
//
//    private void plotRouteOnMap(JSONArray safestPathArray) {
//
//        if (safestPathArray.length() < 2) {
//            Log.e(TAG, "Insufficient data to plot route");
//            return;
//        }
//
//        PolylineOptions polylineOptions = new PolylineOptions().color(getResources().getColor(R.color.button_color)).width(5);
//
//        for (int i = 0; i < safestPathArray.length(); i++) {
//            String townName = safestPathArray.optString(i);
//            LatLng coordinates = townCoordinates.get(townName);
//            if (coordinates != null) {
//                polylineOptions.add(coordinates);
//            } else {
//                Log.e(TAG, "Town coordinates not found for: " + townName);
//            }
//        }
//
//        if (mMap != null) {
//
//            mMap.clear(); // Clear previous markers and routes
//            mMap.addPolyline(polylineOptions);
//            displayDangerZones();
//
//            // Zoom to fit the polyline
//            if (polylineOptions.getPoints().size() > 0) {
//                LatLng firstPoint = polylineOptions.getPoints().get(0);
//                LatLng lastPoint = polylineOptions.getPoints().get(polylineOptions.getPoints().size() - 1);
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                builder.include(firstPoint);
//                builder.include(lastPoint);
//                LatLngBounds bounds = builder.build();
//                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//            }
//        }
//    }
//
//    private void displayDangerZones() {
//        for (Map.Entry<String, LatLng> entry : dangerZones.entrySet()) {
//            LatLng dangerZoneLatLng = entry.getValue();
//
//            // Draw a circle around each danger zone
//            Circle dangerZoneCircle = mMap.addCircle(new CircleOptions()
//                    .center(dangerZoneLatLng)
//                    .radius(500) // Radius in meters
//                    .strokeColor(0xFFFF0000) // Red color
//                    .fillColor(0x44FF0000)); // Translucent red
//
//            // Add a marker with the danger zone name
//            mMap.addMarker(new MarkerOptions()
//                    .position(dangerZoneLatLng)
//                    .title("Danger Zone: " + entry.getKey()));
//
//            // Optional: Add a click listener to display danger zone information
//            mMap.setOnMarkerClickListener(marker -> {
//                String markerTitle = marker.getTitle();
//                Toast.makeText(this, markerTitle, Toast.LENGTH_SHORT).show();
//                return true;
//            });
//        }
//    }
//    private void initializeDangerZones() {
//        // Add specific danger zone coordinates
//        dangerZones.put("Zone 1", new LatLng(-1.3032, 36.8238)); // Nairobi Central
//        dangerZones.put("Zone 2", new LatLng(-1.2864, 36.8172)); // Nairobi CBD
//        dangerZones.put("Zone 3", new LatLng(-1.2921, 36.8219)); // Nairobi University
//        dangerZones.put("Zone 4", new LatLng(-1.3078, 36.8261)); // Westlands
//        dangerZones.put("Zone 5", new LatLng(-4.0435, 39.6682)); // Mombasa Central
//        dangerZones.put("Zone 6", new LatLng(-0.3031, 36.0663)); // Nakuru Central
//        dangerZones.put("Zone 7", new LatLng(0.5143, 35.2691)); // Eldoret Central
//        dangerZones.put("Zone 8", new LatLng(-0.0917, 34.7679)); // Kisumu Central
//        dangerZones.put("Zone 9", new LatLng(-0.4213, 36.9458)); // Nyeri Central
//        dangerZones.put("Zone 10", new LatLng(-1.5168, 37.2665)); // Machakos Central
//        dangerZones.put("Zone 11", new LatLng(-0.3667, 35.2913)); // Kericho Central
//        dangerZones.put("Zone 12", new LatLng(-0.0472, 37.6480)); // Meru Central
//        dangerZones.put("Zone 13", new LatLng(1.0026, 34.9860)); // Kitale Central
//        dangerZones.put("Zone 14", new LatLng(-0.4566, 39.6646)); // Garissa Central
//        dangerZones.put("Zone 15", new LatLng(0.3531, 37.5827)); // Isiolo Central
//        dangerZones.put("Zone 16", new LatLng(0.5913, 34.5647)); // Bungoma Central
//        dangerZones.put("Zone 17", new LatLng(1.7373, 40.0659)); // Wajir Central
//        dangerZones.put("Zone 18", new LatLng(3.9305, 41.8559)); // Mandera Central
//        dangerZones.put("Zone 19", new LatLng(-3.2192, 40.1169)); // Malindi Central
//        dangerZones.put("Zone 20", new LatLng(-2.2712, 40.9020)); // Lamu Central
//        dangerZones.put("Zone 21", new LatLng(-1.0333, 37.0693)); // Thika Central
//        dangerZones.put("Zone 22", new LatLng(-2.5453, 36.7925)); // Namanga Central
//        dangerZones.put("Zone 23", new LatLng(-1.3748, 38.0106)); // Kitui Central
//        dangerZones.put("Zone 24", new LatLng(-0.7072, 36.4319)); // Naivasha Central
//        dangerZones.put("Zone 25", new LatLng(-1.0789, 35.8600)); // Narok Central
//        dangerZones.put("Zone 26", new LatLng(0.4605, 34.1115)); // Busia Central
//        dangerZones.put("Zone 27", new LatLng(-0.7827, 35.3428)); // Bomet Central
//        dangerZones.put("Zone 28", new LatLng(2.3342, 37.9891)); // Marsabit Central
//        dangerZones.put("Zone 29", new LatLng(-3.3962, 38.5565)); // Voi Central
//        dangerZones.put("Zone 30", new LatLng(-0.4292, 39.6465)); // Kilifi Central
//        dangerZones.put("Zone 31", new LatLng(-0.4680, 39.6524)); // Kaloleni
//        dangerZones.put("Zone 32", new LatLng(-1.2929, 36.7925)); // Kawangware, Nairobi
//        dangerZones.put("Zone 33", new LatLng(-1.3176, 36.8355)); // Kibera, Nairobi
//        dangerZones.put("Zone 34", new LatLng(-1.2805, 36.8164)); // Gikomba Market, Nairobi
//        dangerZones.put("Zone 35", new LatLng(-1.2419, 36.8837)); // Kasarani, Nairobi
//        dangerZones.put("Zone 36", new LatLng(-1.2417, 36.8588)); // Roysambu, Nairobi
//        dangerZones.put("Zone 37", new LatLng(-1.2793, 36.8244)); // Ngara, Nairobi
//        dangerZones.put("Zone 38", new LatLng(-1.3064, 36.8143)); // Mathare, Nairobi
//        dangerZones.put("Zone 39", new LatLng(-1.3161, 36.8427)); // Dandora, Nairobi
//        dangerZones.put("Zone 40", new LatLng(-1.2217, 36.8924)); // Kahawa West, Nairobi
//        dangerZones.put("Zone 41", new LatLng(-1.3621, 36.8448)); // Umoja, Nairobi
//        dangerZones.put("Zone 42", new LatLng(-1.2894, 36.8593)); // South B, Nairobi
//        dangerZones.put("Zone 43", new LatLng(-1.3296, 36.8882)); // Pipeline, Nairobi
//        dangerZones.put("Zone 44", new LatLng(-1.2993, 36.8962)); // Embakasi, Nairobi
//        dangerZones.put("Zone 45", new LatLng(-1.3271, 36.8332)); // Ruaraka, Nairobi
//        dangerZones.put("Zone 46", new LatLng(-1.3012, 36.8634)); // Donholm, Nairobi
//        dangerZones.put("Zone 47", new LatLng(-1.2852, 36.8187)); // Globe Roundabout, Nairobi
//        dangerZones.put("Zone 48", new LatLng(-1.2935, 36.8071)); // Nairobi River, Nairobi
//        dangerZones.put("Zone 49", new LatLng(-1.2942, 36.7914)); // Westlands, Nairobi
//        dangerZones.put("Zone 50", new LatLng(-1.2983, 36.7931)); // Parklands, Nairobi
//    }
//
//}
//
//
//


