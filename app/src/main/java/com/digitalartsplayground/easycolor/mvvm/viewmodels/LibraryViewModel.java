package com.digitalartsplayground.easycolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.easycolor.ModelsProvider;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private ModelsProvider modelsProvider;

    public LibraryViewModel(@NonNull @NotNull Application application) {
        super(application);
        modelsProvider = ModelsProvider.getInstance(application);
    }

    public LiveData<List<VectorEntity>> fetchLiveModels() {
        return modelsProvider.fetchLiveModels();
    }

    public void fetchModelWithEntity(VectorEntity vectorEntity, int position) {
        modelsProvider.fetchModelWithEntity(vectorEntity, position);
    }

    public LiveData<Integer> getAdapterUpdater() {
        return modelsProvider.getAdapterUpdater();
    }

    public void setCurrentVectorModel(VectorEntity selectedVectorEntity){
        modelsProvider.setSelectedVectorModel(selectedVectorEntity);
    }

    public LiveData<Boolean> getVectorModelChanged() {
        return modelsProvider.getVectorModelChanged();
    }
}