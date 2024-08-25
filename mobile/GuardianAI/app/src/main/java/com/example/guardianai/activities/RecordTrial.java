package com.example.guardianai.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.SpeechRecognizer;
import android.speech.SpeechRecognizer.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.guardianai.R;


import java.util.ArrayList;
import java.util.Locale;

public class RecordTrial extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isRecording = false;
    private Button buttonStartRecording;
    private Button buttonStopRecording;
    private TextView textViewTime;
    private TextView textViewEmotion1;
    private Handler handler;
    private Runnable timeRunnable;
    private long startTime = 0L;
    private long timeElapsed = 0L;
    private long lastTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trial);

        buttonStartRecording = findViewById(R.id.buttonStartRecording);
        buttonStopRecording = findViewById(R.id.buttonStopRecording);
        textViewTime = findViewById(R.id.textViewTime);
        textViewEmotion1 = findViewById(R.id.textViewEmotion1);


        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Simulate emotion detection
                    String recognizedText = matches.get(0);
                    textViewEmotion1.setText(recognizedText); // Placeholder

                    Toast.makeText(RecordTrial.this, "Recognized Text: " + recognizedText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        buttonStartRecording.setOnClickListener(v -> startRecording());
        buttonStopRecording.setOnClickListener(v -> stopRecording());

        handler = new Handler();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                timeElapsed = System.currentTimeMillis() - startTime;
                int seconds = (int) (timeElapsed / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                textViewTime.setText(String.format("Time: %02d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void startRecording() {
        if (!isRecording) {
            isRecording = true;
            buttonStartRecording.setVisibility(Button.GONE);
            buttonStopRecording.setVisibility(Button.VISIBLE);
            startTime = System.currentTimeMillis();
            handler.post(timeRunnable);
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            buttonStartRecording.setVisibility(Button.VISIBLE);
            buttonStopRecording.setVisibility(Button.GONE);
            handler.removeCallbacks(timeRunnable);
            speechRecognizer.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


//package com.example.guardianai.activities;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.guardianai.R;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class RecordTrial extends AppCompatActivity {
//
//    private static final String TAG = "RecordTrial";
//    private static final String API_URL = "https://api-inference.huggingface.co/models/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition";
//    private static final String API_KEY = "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";
//    private static final int SAMPLE_RATE = 16000;
//    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//
//    private Button buttonStartRecording, buttonStopRecording;
//    private TextView textViewEmotion1, textViewEmotion2;
//    private AudioRecord audioRecord;
//    private boolean isRecording = false;
//    private Handler handler;
//    private ByteArrayOutputStream audioBuffer;
//    private String audioFilePath;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_record_trial);
//
//        buttonStartRecording = findViewById(R.id.buttonStartRecording);
//        buttonStopRecording = findViewById(R.id.buttonStopRecording);
//        textViewEmotion1 = findViewById(R.id.textViewEmotion1);
//        textViewEmotion2 = findViewById(R.id.textViewEmotion2);
//
//        handler = new Handler(Looper.getMainLooper());
//
//        buttonStartRecording.setOnClickListener(v -> startRecording());
//        buttonStopRecording.setOnClickListener(v -> stopRecording());
//
//        // Check for audio recording permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 200);
//        }
//    }
//
//    private void startRecording() {
//        buttonStartRecording.setVisibility(View.GONE);
//        buttonStopRecording.setVisibility(View.VISIBLE);
//
//        audioBuffer = new ByteArrayOutputStream();
//
//        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
//
//        byte[] audioData = new byte[bufferSize];
//        audioRecord.startRecording();
//        isRecording = true;
//
//        // Start a new thread to read audio data
//        new Thread(() -> {
//            while (isRecording) {
//                int read = audioRecord.read(audioData, 0, bufferSize);
//                if (read > 0) {
//                    audioBuffer.write(audioData, 0, read);
//                }
//            }
//        }).start();
//    }
//
//    private void stopRecording() {
//        try {
//            isRecording = false;
//            audioRecord.stop();
//            audioRecord.release();
//            audioRecord = null;
//            Log.d(TAG, "Recording stopped successfully.");
//
//            // Save the recorded audio to a file
//            byte[] audioData = audioBuffer.toByteArray();
//            saveAudioToFile(audioData);
//
//            // Send the audio file to the API
//            sendAudioToAPI();
//
//            buttonStartRecording.setVisibility(View.VISIBLE);
//            buttonStopRecording.setVisibility(View.GONE);
//        } catch (Exception e) {
//            Log.e(TAG, "Error stopping recording: " + e.getMessage(), e);
//        }
//    }
//
//    private void saveAudioToFile(byte[] audioData) {
//        File audioFile = new File(getExternalFilesDir(null), "audio.wav");
//        audioFilePath = audioFile.getAbsolutePath();
//
//        try (FileOutputStream outputStream = new FileOutputStream(audioFile)) {
//            // Write WAV header
//            writeWavHeader(outputStream, audioData.length);
//
//            // Write audio data
//            outputStream.write(audioData);
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to save audio file: " + e.getMessage(), e);
//        }
//    }
//
//    private void writeWavHeader(FileOutputStream outputStream, int dataSize) throws IOException {
//        int fileSize = 36 + dataSize; // Header size + audio data size
//        int sampleRate = SAMPLE_RATE;
//        short channels = 1;
//        short bitsPerSample = 16;
//
//        ByteBuffer header = ByteBuffer.allocate(44);
//        header.order(ByteOrder.LITTLE_ENDIAN);
//
//        header.put("RIFF".getBytes()); // ChunkID
//        header.putInt(fileSize); // ChunkSize
//        header.put("WAVE".getBytes()); // Format
//        header.put("fmt ".getBytes()); // Subchunk1ID
//        header.putInt(16); // Subchunk1Size
//        header.putShort((short) 1); // AudioFormat
//        header.putShort(channels); // NumChannels
//        header.putInt(sampleRate); // SampleRate
//        header.putInt(sampleRate * channels * bitsPerSample / 8); // ByteRate
//        header.putShort((short) (channels * bitsPerSample / 8)); // BlockAlign
//        header.putShort(bitsPerSample); // BitsPerSample
//        header.put("data".getBytes()); // Subchunk2ID
//        header.putInt(dataSize); // Subchunk2Size
//
//        outputStream.write(header.array());
//    }
//
//    private void sendAudioToAPI() {
//        // Create an OkHttpClient instance with a 60-second timeout
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Connection timeout
//                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // Write timeout
//                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // Read timeout
//                .build();
//
//        File audioFile = new File(audioFilePath);
//
//        if (!audioFile.exists()) {
//            Log.e(TAG, "Audio file does not exist: " + audioFile.getAbsolutePath());
//            return;
//        }
//
//        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), audioFile);
//
//        Request request = new Request.Builder()
//                .url(API_URL)
//                .addHeader("Authorization", "Bearer " + API_KEY)
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.e(TAG, "API Request Failed: ", e);
//                Log.e(TAG, "Request URL: " + request.url());
//                Log.e(TAG, "Request Headers: " + request.headers());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e(TAG, "API Request was not successful. Response code: " + response.code());
//                    Log.e(TAG, "Response Headers: " + response.headers());
//                    Log.e(TAG, "Response Body: " + response.body().string());
//                    return;
//                }
//
//                try {
//                    String responseBody = response.body().string();
//                    Log.d(TAG, "Response Body: " + responseBody);
//
//                    JSONArray jsonArray = new JSONArray(responseBody);
//
//                    if (jsonArray.length() > 1) {
//                        JSONObject emotion1 = jsonArray.getJSONObject(0);
//                        JSONObject emotion2 = jsonArray.getJSONObject(1);
//
//                        String emotionLabel1 = emotion1.getString("label");
//                        String emotionLabel2 = emotion2.getString("label");
//
//                        handler.post(() -> {
//                            textViewEmotion1.setText("Emotion 1: " + emotionLabel1);
//                            textViewEmotion2.setText("Emotion 2: " + emotionLabel2);
//                        });
//
//                    } else {
//                        Log.e(TAG, "Unexpected response format. JSON array does not have enough elements.");
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "Failed to parse API response: " + e.getMessage(), e);
//                }
//            }
//        });
//    }
//
//
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 200) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, you can proceed with recording
//            } else {
//                // Permission denied, show a message or handle accordingly
//            }
//        }
//    }
//}
//
//
