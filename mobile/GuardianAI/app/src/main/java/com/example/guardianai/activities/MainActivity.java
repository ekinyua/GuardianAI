package com.example.guardianai.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.guardianai.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_VOICE_COMMAND = 100;
    private static final String COMMAND_KEYWORD = "Banana and Cheese";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start voice recognition
        startVoiceRecognition();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                if (item.getItemId() == R.id.navigation_live_location) {
                    intent = new Intent(MainActivity.this, LocationSharing.class);
                } else if (item.getItemId() == R.id.navigation_safe_route) {
                    intent = new Intent(MainActivity.this, SafeRoute.class);
                } else if (item.getItemId() == R.id.navigation_recording) {
                    intent = new Intent(MainActivity.this, Recording.class);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    intent = new Intent(MainActivity.this, Profile.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

                return true; // Return true to indicate the item selection was handled
            }
        });
    }

    private void startVoiceRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'Banana and Cheese'");
            startActivityForResult(intent, REQUEST_CODE_VOICE_COMMAND);
        } else {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VOICE_COMMAND && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.contains(COMMAND_KEYWORD)) {
                // Handle the recognized command
                Toast.makeText(this, "Command recognized: " + COMMAND_KEYWORD, Toast.LENGTH_SHORT).show();
                // Perform the action associated with the command
                // For example, you could start a specific activity or trigger a function
            } else {
                Toast.makeText(this, "Command not recognized. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
