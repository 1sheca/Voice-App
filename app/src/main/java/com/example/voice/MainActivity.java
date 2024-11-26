package com.example.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3; // Camera permission request code
    private static final int PICK_FILE_REQUEST = 4; // For file explorer or gallery

    private Button startButton, stopButton, playButton, viewRecordingsButton, openGalleryButton;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentFilePath;
    private File recordingsDirectory;
    private ArrayList<String> recordingList = new ArrayList<>();
    private ListView recordingsListView;
    private RecordingsAdapter recordingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);
        viewRecordingsButton = findViewById(R.id.viewRecordingsButton);
        openGalleryButton = findViewById(R.id.openGalleryButton); // Added gallery button
        recordingsListView = findViewById(R.id.recordingsListView);

        // Initialize recordings directory
        recordingsDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Request necessary permissions
        requestPermissions();

        // Set up the Recordings ListView
        recordingsAdapter = new RecordingsAdapter(this, recordingList);
        recordingsListView.setAdapter(recordingsAdapter);

        // Button click listeners
        startButton.setOnClickListener(v -> startRecording());
        stopButton.setOnClickListener(v -> stopRecording());
        playButton.setOnClickListener(v -> playRecording());
        viewRecordingsButton.setOnClickListener(v -> viewRecordings());
        openGalleryButton.setOnClickListener(v -> openGalleryOrFileExplorer()); // Open gallery/file explorer
    }

    private void requestPermissions() {
        // Check for audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Check for storage permission based on Android version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // For Android 9 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
            }
        }

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Audio recording permission is required", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        try {
            // Create a unique file for each recording
            currentFilePath = new File(recordingsDirectory, "recording_" + System.currentTimeMillis() + ".3gp").getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(currentFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            // Add the recording to the list
            recordingList.add(currentFilePath);
            recordingsAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Recording saved: " + currentFilePath, Toast.LENGTH_SHORT).show();
        }
    }

    private void playRecording() {
        if (currentFilePath == null) {
            Toast.makeText(this, "No recording to play", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing recording", Toast.LENGTH_SHORT).show();

            // Release MediaPlayer resources when playback finishes
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewRecordings() {
        loadExistingRecordings();

        if (recordingList.isEmpty()) {
            Toast.makeText(this, "No recordings available to view", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Recordings list updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExistingRecordings() {
        // Clear the current list to avoid duplicates
        recordingList.clear();

        // List files in the recordings directory
        File[] files = recordingsDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".3gp")) {
                    recordingList.add(file.getAbsolutePath());
                }
            }
        }

        // Notify the adapter to refresh the list view
        recordingsAdapter.notifyDataSetChanged();
    }

    private void openGalleryOrFileExplorer() {
        // Intent to open file explorer or gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("*/*"); // Select all file types
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            Toast.makeText(this, "Selected file: " + selectedUri, Toast.LENGTH_SHORT).show();
        }
    }
}
