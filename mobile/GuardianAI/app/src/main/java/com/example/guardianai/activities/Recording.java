package com.example.guardianai.activities;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.guardianai.R;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;
import okhttp3.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Recording extends AppCompatActivity {

    private static final String TAG = "Recording";
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final String API_URL2 = "https://guardianai-m2kc.onrender.com/analyze_audio";
    private static final String API_URL = "https://api-inference.huggingface.co/models/ehcalabres/wav2vec2-lg-xlsr-en-speech-emotion-recognition";
    private static final String API_KEY = "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";
    private static final long RECORDING_INTERVAL_MS = 5000; // 5 seconds
    private static final int API_TIMEOUT_MS = 60000; // 60 seconds



    private AudioRecord audioRecord;
    private File audioFile;
    private File wavFile;
    private Handler handler;
    private Runnable saveAndSendAudioRunnable;
    private boolean isRecording = false;
    private ImageView micIcon;
    private TextView recordingControlText;
    private Animation pulseAnimation;

    private ByteArrayOutputStream audioBuffer;
    private String audioFilePath;
    private TextView textViewEmotion1, textViewEmotion2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording); // Ensure this matches your layout file
        textViewEmotion1 = findViewById(R.id.textViewEmotion1);
        textViewEmotion2 = findViewById(R.id.textViewEmotion2);

        micIcon = findViewById(R.id.mic_icon);
        recordingControlText = findViewById(R.id.recording_control_text);

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);

        handler = new Handler(Looper.getMainLooper());

        recordingControlText.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        }
    }
    private void startRecording() {
        recordingControlText.setText("Stop Recording");
        Log.d(TAG, "Starting recording");

        micIcon.startAnimation(pulseAnimation);


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
        if (isRecording) {
            try {
                // Stop recording
                isRecording = false;
                recordingControlText.setText("Start Recording");
                Log.d(TAG, "Stopping recording");

                // Stop and release AudioRecord
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                    Log.d(TAG, "Recording stopped successfully.");

                    // Save the recorded audio to a file
                    if (audioBuffer != null) {
                        byte[] audioData = audioBuffer.toByteArray();
                        saveAudioToFile(audioData);

                        // Send the audio file to the API
                        sendAudioToAPI();
                    } else {
                        Log.e(TAG, "Audio buffer is null");
                    }

                    // Stop pulse animation
                    micIcon.clearAnimation();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recording: " + e.getMessage(), e);
            }
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
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted");
            } else {
                Toast.makeText(this, "Permissions are required to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


