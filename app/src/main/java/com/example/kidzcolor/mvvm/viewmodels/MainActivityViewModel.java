package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.jetbrains.annotations.NotNull;

public class MainActivityViewModel extends AndroidViewModel {

    public boolean isLibraryCurrent() {
        return isLibraryCurrent;
    }

    public void setLibraryCurrent(boolean libraryCurrent) {
        isLibraryCurrent = libraryCurrent;
    }

    private boolean isLibraryCurrent = true;

    public MainActivityViewModel(@NonNull @NotNull Application application) {
        super(application);
    }



}
