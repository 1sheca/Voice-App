package com.example.voice;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingsAdapter extends ArrayAdapter<String> {
    private MediaPlayer mediaPlayer;

    public RecordingsAdapter(@NonNull Context context, @NonNull ArrayList<String> recordings) {
        super(context, 0, recordings); // Pass context and recordings to ArrayAdapter's constructor
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String recordingPath = getItem(position);
        TextView textView = convertView.findViewById(android.R.id.text1);

        // Display the file name
        if (recordingPath != null) {
            textView.setText(new File(recordingPath).getName());
        }

        // Set click listener to play recording
        convertView.setOnClickListener(v -> playRecording(recordingPath));

        return convertView;
    }

    private void playRecording(String filePath) {
        if (filePath == null) {
            Toast.makeText(getContext(), "File path is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop existing playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            Toast.makeText(getContext(), "Playing: " + new File(filePath).getName(), Toast.LENGTH_SHORT).show();

            // Handle completion
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                Toast.makeText(getContext(), "Playback finished", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to play recording", Toast.LENGTH_SHORT).show();
        }
    }
}
