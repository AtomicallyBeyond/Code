package com.example.kidzcolor;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.VectorEntity;

import java.util.ArrayList;
import java.util.List;

public class ModelsProvider {

    private static ModelsProvider instance;
    private Repository repository;
    private MediatorLiveData<Resource<List<VectorEntity>>> libraryLiveData = new MediatorLiveData<>();
    private MediatorLiveData<List<VectorEntity>> studioLivedata = new MediatorLiveData<>();

    public static ModelsProvider getInstance(Context context){
        if(instance == null){
            instance = new ModelsProvider(context);
        }
        return instance;
    }

    public ModelsProvider(Context context){
        repository = Repository.getInstance(context);
        fetchUpdates();
    }

    private void fetchUpdates() {
        LiveData<Resource<List<VectorEntity>>> liveUpdates = repository.fetchUpdates();
        libraryLiveData.addSource(liveUpdates, new Observer<Resource<List<VectorEntity>>>() {
                @Override
                public void onChanged(Resource<List<VectorEntity>> listResource) {
                    libraryLiveData.removeSource(liveUpdates);
                    libraryLiveData.setValue(listResource);
                }
            });
    }

    public void fetchMore(){

        LiveData<Resource<List<VectorEntity>>> liveData = repository.fetchMore();

        libraryLiveData.addSource(liveData, new Observer<Resource<List<VectorEntity>>>() {
            @Override
            public void onChanged(Resource<List<VectorEntity>> listResource) {
                libraryLiveData.removeSource(liveData);
                libraryLiveData.setValue(listResource);
            }
        });
    }

    public LiveData<Resource<List<VectorEntity>>> getLibraryLiveList() { return  libraryLiveData;}

    private void populateStudioLiveList() {
        List<VectorEntity> tempList = new ArrayList<>();

        for(VectorEntity vectorEntity : libraryLiveData.getValue().data) {
            if(vectorEntity.isInProgress())
                tempList.add(vectorEntity);
        }

        studioLivedata.setValue(tempList);
    }

    public LiveData<List<VectorEntity>> getStudioLiveList() {
        return studioLivedata;
    }



}
