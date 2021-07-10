package com.example.kidzcolor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidzcolor.models.ReplayDrawable;
import com.example.kidzcolor.viewmodels.CompletedViewModel;

public class CompletedActivity extends AppCompatActivity {

    private CompletedViewModel completedViewModel;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);

        completedViewModel = new ViewModelProvider(this).get(CompletedViewModel.class);
        imageView = findViewById(R.id.replay_view);
        ReplayDrawable replayDrawable = new ReplayDrawable(completedViewModel.getSelectedViewModel().getValue());
        imageView.setImageDrawable(replayDrawable);


        ((AppCompatImageButton)findViewById(R.id.replay_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replayDrawable.startReplay();
            }
        });

        ((AppCompatImageButton)findViewById(R.id.back_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
