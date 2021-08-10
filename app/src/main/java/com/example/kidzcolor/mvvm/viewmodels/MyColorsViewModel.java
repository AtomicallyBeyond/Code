package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.mvvm.SingleLiveEvent;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyColorsViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<List<VectorEntity>> liveModelsList = new MediatorLiveData<>();

    public MyColorsViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        observeUpdates();
    }

    public void observeUpdates(){
        LiveData<List<VectorEntity>> liveData = repository.getMyColorModels();

        liveModelsList.addSource(liveData, new Observer<List<VectorEntity>>() {
            @Override
            public void onChanged(List<VectorEntity> vectorEntities) {
                liveModelsList.removeSource(liveData);
                liveModelsList.setValue(vectorEntities);
            }
        });
    }

    public LiveData<List<VectorEntity>> getModelsList() {
        return liveModelsList;
    }

    public void setCurrentVectorModel(VectorEntity vectorEntity){
        repository.setSelectedVectorModel(vectorEntity);
    }

    public LiveData<Boolean> getVectorModelChanged() {
        return repository.getVectorModelChanged();
    }
}
