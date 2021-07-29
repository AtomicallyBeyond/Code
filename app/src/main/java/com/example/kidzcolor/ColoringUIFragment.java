/*
package com.example.kidzcolor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import com.example.kidzcolor.models.ReplayDrawable;
import com.example.kidzcolor.models.VectorModelContainer;

import org.jetbrains.annotations.NotNull;

public class ColoringUIFragment extends DialogFragment {

    private AppCompatImageView imageView;
    private AppCompatImageButton returnButton;
    private AppCompatImageButton playButton;
    private AppCompatImageButton resetButton;
    private VectorModelContainer vectorModelContainer;
    private ReplayDrawable replayDrawable;
    private ColoringActivity coloringActivity;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coloringActivity = ((ColoringActivity)getActivity());
        vectorModelContainer = coloringActivity.getVectorModelContainer();
        replayDrawable = new ReplayDrawable(vectorModelContainer);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_completed, container, false);
        imageView = view.findViewById(R.id.reset_view);
        returnButton = view.findViewById(R.id.back_button);
        playButton = view.findViewById(R.id.play_button);
        resetButton = view.findViewById(R.id.replay_button);

        imageView.setImageDrawable(replayDrawable);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replayDrawable.startReplay();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coloringActivity.resetVectorModel();
                getDialog().dismiss();
            }
        });

        return view;
    }
}
*/
