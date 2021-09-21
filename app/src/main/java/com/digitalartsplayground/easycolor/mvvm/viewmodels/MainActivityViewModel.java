package com.digitalartsplayground.easycolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.easycolor.ModelsProvider;

import org.jetbrains.annotations.NotNull;

public class MainActivityViewModel extends AndroidViewModel {

    private ModelsProvider modelsProvider;
    private boolean isLibraryCurrent = true;

    public MainActivityViewModel(@NonNull @NotNull Application application) {
        super(application);

        modelsProvider = ModelsProvider.getInstance(application);
    }

    public LiveData<Boolean> removeLoading(){
        return modelsProvider.getRemoveLoading();
    }

    public boolean isLibraryCurrent() {
        return isLibraryCurrent;
    }

    public void setLibraryCurrent(boolean libraryCurrent) {
        isLibraryCurrent = libraryCurrent;
    }







}
