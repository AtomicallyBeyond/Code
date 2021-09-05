package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.kidzcolor.ModelsProvider;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private ModelsProvider modelsProvider;
    private MediatorLiveData<Resource<List<VectorEntity>>> liveModelsList
            = new MediatorLiveData<>();
    private boolean isUpdated = false;

    public LibraryViewModel(@NonNull @NotNull Application application) {
        super(application);
        modelsProvider = ModelsProvider.getInstance(application);
    }

    public void fetchModel(int modelID){
        modelsProvider.fetchModel(modelID);
    }

    public LiveData<Resource<List<VectorEntity>>> getModelsList() { return  modelsProvider.getLibraryLiveList();}

    public void setCurrentVectorModel(VectorEntity selectedVectorEntity){
        modelsProvider.setSelectedVectorModel(selectedVectorEntity);
    }

    public LiveData<Boolean> getVectorModelChanged() {
        return modelsProvider.getVectorModelChanged();
    }
}
