package com.example.voice;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecordingsActivity extends AppCompatActivity {

    private ListView recordingsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        recordingsListView = findViewById(R.id.recordingsListView);

        // Get the list of recordings passed from MainActivity
        ArrayList<String> recordingList = getIntent().getStringArrayListExtra("recordingList");

        // Set up adapter to display recordings
        RecordingsAdapter adapter = new RecordingsAdapter(this, recordingList);
        recordingsListView.setAdapter(adapter);
    }
}
