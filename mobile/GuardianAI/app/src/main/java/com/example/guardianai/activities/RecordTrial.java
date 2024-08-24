package com.example.guardianai.activities;//package com.example.guardianai.activities;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
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
//import java.io.File;
//import java.io.IOException;
//import java.util.Timer;
//import java.util.TimerTask;
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
//
//    private Button buttonStartRecording, buttonStopRecording;
//    private TextView textViewEmotion1, textViewEmotion2;
//    private MediaRecorder mediaRecorder;
//    private String audioFilePath;
//    private Timer timer;
//    private Handler handler;
//    private final String API_URL = "https://api-inference.huggingface.co/models/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition";
//    private final String API_KEY = "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";
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
//        // Set the audio file path
//        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_recording.3gp";
//        Log.d("Recording", "Audio file path: " + audioFilePath);
//
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setOutputFile(audioFilePath);
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//            Log.d("Recording", "Recording started successfully.");
//
//            // Start the timer to send audio every 5 seconds
//            timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    Log.d("API", "Sending audio to API.");
//                    sendAudioToAPI();
//                }
//            }, 0, 5000);
//        } catch (IOException e) {
//            Log.e("Recording", "Failed to start recording: " + e.getMessage(), e);
//        }
//    }
//
//    private void sendAudioToAPI() {
//        File audioFile = new File(audioFilePath);
//
//        if (!audioFile.exists()) {
//            Log.e("API", "Audio file does not exist: " + audioFilePath);
//            return;
//        }
//
//        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
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
//                Log.e("API", "API Request Failed", e);
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    try {
//                        String responseBody = response.body().string();
//                        JSONArray jsonArray = new JSONArray(responseBody);
//
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
//                        Log.d("API", "API Request Successful: " + responseBody);
//                    } catch (Exception e) {
//                        Log.e("API", "Failed to parse API response: " + e.getMessage(), e);
//                    }
//                } else {
//                    Log.e("API", "API Request was not successful. Response code: " + response.code());
//                }
//            }
//        });
//    }
//
//
//    private void stopRecording() {
//        try {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//            Log.d(TAG, "Recording stopped successfully.");
//
//            timer.cancel();
//            Log.d(TAG, "Timer canceled.");
//
//            buttonStartRecording.setVisibility(View.VISIBLE);
//            buttonStopRecording.setVisibility(View.GONE);
//        } catch (Exception e) {
//            Log.e(TAG, "Error stopping recording: " + e.getMessage(), e);
//        }
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 200) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "Audio recording permission granted.");
//            } else {
//                Log.e(TAG, "Audio recording permission denied.");
//            }
//        }
//    }
//}


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.guardianai.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecordTrial extends AppCompatActivity {

    private static final String TAG = "RecordTrial";
    private static final String API_URL = "https://api-inference.huggingface.co/models/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition";
    private static final String API_KEY = "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";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private Button buttonStartRecording, buttonStopRecording;
    private TextView textViewEmotion1, textViewEmotion2;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Handler handler;
    private ByteArrayOutputStream audioBuffer;
    private String audioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trial);

        buttonStartRecording = findViewById(R.id.buttonStartRecording);
        buttonStopRecording = findViewById(R.id.buttonStopRecording);
        textViewEmotion1 = findViewById(R.id.textViewEmotion1);
        textViewEmotion2 = findViewById(R.id.textViewEmotion2);

        handler = new Handler(Looper.getMainLooper());

        buttonStartRecording.setOnClickListener(v -> startRecording());
        buttonStopRecording.setOnClickListener(v -> stopRecording());

        // Check for audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 200);
        }
    }

    private void startRecording() {
        buttonStartRecording.setVisibility(View.GONE);
        buttonStopRecording.setVisibility(View.VISIBLE);

        audioBuffer = new ByteArrayOutputStream();

        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        byte[] audioData = new byte[bufferSize];
        audioRecord.startRecording();
        isRecording = true;

        // Start a new thread to read audio data
        new Thread(() -> {
            while (isRecording) {
                int read = audioRecord.read(audioData, 0, bufferSize);
                if (read > 0) {
                    audioBuffer.write(audioData, 0, read);
                }
            }
        }).start();
    }

    private void stopRecording() {
        try {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            Log.d(TAG, "Recording stopped successfully.");

            // Save the recorded audio to a file
            byte[] audioData = audioBuffer.toByteArray();
            saveAudioToFile(audioData);

            // Send the audio file to the API
            sendAudioToAPI();

            buttonStartRecording.setVisibility(View.VISIBLE);
            buttonStopRecording.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error stopping recording: " + e.getMessage(), e);
        }
    }

    private void saveAudioToFile(byte[] audioData) {
        File audioFile = new File(getExternalFilesDir(null), "audio.wav");
        audioFilePath = audioFile.getAbsolutePath();

        try (FileOutputStream outputStream = new FileOutputStream(audioFile)) {
            // Write WAV header
            writeWavHeader(outputStream, audioData.length);

            // Write audio data
            outputStream.write(audioData);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save audio file: " + e.getMessage(), e);
        }
    }

    private void writeWavHeader(FileOutputStream outputStream, int dataSize) throws IOException {
        int fileSize = 36 + dataSize; // Header size + audio data size
        int sampleRate = SAMPLE_RATE;
        short channels = 1;
        short bitsPerSample = 16;

        ByteBuffer header = ByteBuffer.allocate(44);
        header.order(ByteOrder.LITTLE_ENDIAN);

        header.put("RIFF".getBytes()); // ChunkID
        header.putInt(fileSize); // ChunkSize
        header.put("WAVE".getBytes()); // Format
        header.put("fmt ".getBytes()); // Subchunk1ID
        header.putInt(16); // Subchunk1Size
        header.putShort((short) 1); // AudioFormat
        header.putShort(channels); // NumChannels
        header.putInt(sampleRate); // SampleRate
        header.putInt(sampleRate * channels * bitsPerSample / 8); // ByteRate
        header.putShort((short) (channels * bitsPerSample / 8)); // BlockAlign
        header.putShort(bitsPerSample); // BitsPerSample
        header.put("data".getBytes()); // Subchunk2ID
        header.putInt(dataSize); // Subchunk2Size

        outputStream.write(header.array());
    }

    private void sendAudioToAPI() {
        // Create an OkHttpClient instance with a 60-second timeout
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Connection timeout
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // Write timeout
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // Read timeout
                .build();

        File audioFile = new File(audioFilePath);

        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist: " + audioFile.getAbsolutePath());
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), audioFile);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API Request Failed: ", e);
                Log.e(TAG, "Request URL: " + request.url());
                Log.e(TAG, "Request Headers: " + request.headers());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "API Request was not successful. Response code: " + response.code());
                    Log.e(TAG, "Response Headers: " + response.headers());
                    Log.e(TAG, "Response Body: " + response.body().string());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response Body: " + responseBody);

                    JSONArray jsonArray = new JSONArray(responseBody);

                    if (jsonArray.length() > 1) {
                        JSONObject emotion1 = jsonArray.getJSONObject(0);
                        JSONObject emotion2 = jsonArray.getJSONObject(1);

                        String emotionLabel1 = emotion1.getString("label");
                        String emotionLabel2 = emotion2.getString("label");

                        handler.post(() -> {
                            textViewEmotion1.setText("Emotion 1: " + emotionLabel1);
                            textViewEmotion2.setText("Emotion 2: " + emotionLabel2);
                        });

                    } else {
                        Log.e(TAG, "Unexpected response format. JSON array does not have enough elements.");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse API response: " + e.getMessage(), e);
                }
            }
        });
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with recording
            } else {
                // Permission denied, show a message or handle accordingly
            }
        }
    }
}


