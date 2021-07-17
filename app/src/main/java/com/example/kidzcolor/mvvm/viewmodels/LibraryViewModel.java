package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.VectorEntity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private Repository repository;
    private MediatorLiveData<Resource<List<VectorEntity>>> liveModelsList
            = new MediatorLiveData<>();
    private boolean isUpdated = false;

    public LibraryViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
    }

    public void fetchUpdates() {
        if(!isUpdated) {
            LiveData<Resource<List<VectorEntity>>> liveUpdates = repository.fetchUpdates();
            liveModelsList.addSource(liveUpdates, new Observer<Resource<List<VectorEntity>>>() {
                @Override
                public void onChanged(Resource<List<VectorEntity>> listResource) {
                    liveModelsList.removeSource(liveUpdates);
                    isUpdated = true;
                    liveModelsList.setValue(listResource);
                }
            });
        }
    }

    public void fetchMore(){

        LiveData<Resource<List<VectorEntity>>> liveData = repository.fetchMore();

        liveModelsList.addSource(liveData, new Observer<Resource<List<VectorEntity>>>() {
            @Override
            public void onChanged(Resource<List<VectorEntity>> listResource) {
                liveModelsList.removeSource(liveData);
                liveModelsList.setValue(listResource);
            }
        });
    }

    public LiveData<Resource<List<VectorEntity>>> getModelsList() { return  liveModelsList;}

    public void setCurrentVectorModel(VectorEntity vectorEntity){
        repository.setCurrentVectorModel(vectorEntity);
    }
}
