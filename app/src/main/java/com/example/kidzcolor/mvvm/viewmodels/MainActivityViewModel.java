package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidzcolor.ModelsProvider;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

    private ModelsProvider modelsProvider;
    private boolean isLibraryCurrent = true;

    public MainActivityViewModel(@NonNull @NotNull Application application) {
        super(application);

        modelsProvider = ModelsProvider.getInstance(application);
    }

    public LiveData<Resource<List<VectorEntity>>> initializeLibrary() {
        return modelsProvider.getLibraryLiveList();
    }

    public boolean isLibraryCurrent() {
        return isLibraryCurrent;
    }

    public void setLibraryCurrent(boolean libraryCurrent) {
        isLibraryCurrent = libraryCurrent;
    }







}
