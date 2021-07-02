package com.example.kidzcolor;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidzcolor.viewmodels.CompletedViewModel;

public class CompletedActivity extends AppCompatActivity {

    private CompletedViewModel completedViewModel;
    private ColoringReplay coloringReplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);

        completedViewModel = new ViewModelProvider(this).get(CompletedViewModel.class);
        coloringReplay = findViewById(R.id.replay_view);

        coloringReplay.setPathsList(completedViewModel.getColoredHistory());
        coloringReplay.startReplay();

    }
}
