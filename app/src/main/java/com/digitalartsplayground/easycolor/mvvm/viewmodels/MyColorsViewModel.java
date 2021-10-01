package com.digitalartsplayground.easycolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.digitalartsplayground.easycolor.ModelsProvider;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class MyColorsViewModel extends AndroidViewModel {

    private ModelsProvider modelsProvider;

    public MyColorsViewModel(@NonNull @NotNull Application application) {
        super(application);
        modelsProvider = ModelsProvider.getExistingInstance();
    }

    public LiveData<HashMap<Integer, VectorEntity>> getModelsList() {
        return modelsProvider.getArtworkLiveList();
    }

    public void setCurrentVectorModel(VectorEntity vectorEntity){
        modelsProvider.setSelectedVectorModel(vectorEntity);
    }

    public LiveData<Boolean> getVectorModelChanged() {
        return modelsProvider.getVectorModelChanged();
    }

    public void resetVectorModel(VectorEntity vectorEntity){
        modelsProvider.resetVectorModel(vectorEntity);
        modelsProvider.notifyVectorModelChange(true);
    }
}
