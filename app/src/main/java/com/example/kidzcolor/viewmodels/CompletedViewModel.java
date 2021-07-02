package com.example.kidzcolor.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.kidzcolor.models.PathModel;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.repository.DrawableRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompletedViewModel extends AndroidViewModel {

    private DrawableRepository drawableRepository;

    public CompletedViewModel(@NonNull @NotNull Application application) {
        super(application);

        drawableRepository = DrawableRepository.getInstance();
    }

    public VectorModel getSelectedViewModel(){return drawableRepository.getModel();}

}
