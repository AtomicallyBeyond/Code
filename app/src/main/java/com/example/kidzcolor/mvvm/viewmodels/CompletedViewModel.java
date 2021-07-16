/*
package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.repository.DrawableRepository;

import org.jetbrains.annotations.NotNull;

public class CompletedViewModel extends AndroidViewModel {

    private DrawableRepository drawableRepository;

    public CompletedViewModel(@NonNull @NotNull Application application) {
        super(application);

        drawableRepository = DrawableRepository.getInstance(application);
    }

    public LiveData<VectorModelContainer> getSelectedViewModel(){return drawableRepository.getModel();}

}
*/
