package com.example.kidzcolor;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.mvvm.SingleLiveEvent;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.BackupModelDao;
import com.example.kidzcolor.persistance.BackupVector;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.AppExecutors;

import java.util.HashMap;
import java.util.List;

public class ModelsProvider {

    private static ModelsProvider instance;
    private Repository repository;
    private ModelDao modelDao;
    private BackupModelDao backupModelDao;
    private VectorEntity selectedVectorEntity;
    private SingleLiveEvent<Boolean> vectorModelChanged = new SingleLiveEvent<>();
    private MediatorLiveData<Resource<List<VectorEntity>>> libraryLiveData = new MediatorLiveData<>();
    private MediatorLiveData<HashMap<Integer, VectorEntity>> artworkLivedata = new MediatorLiveData<>();


    public static ModelsProvider getInstance(Context context){
        if(instance == null){
            instance = new ModelsProvider(context);
        }
        return instance;
    }

    public ModelsProvider(Context context){
        repository = Repository.getInstance(context);
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        backupModelDao = ModelsDatabase.getInstance(context).getBackupModelsDao();
        fetchUpdates();
    }

    public SingleLiveEvent<Boolean> getVectorModelChanged() {
        return vectorModelChanged;
    }

    public void setSelectedVectorModel(VectorEntity selectedVectorModel) {
        selectedVectorEntity = selectedVectorModel;
    }

    public VectorEntity getSelectedVectorModel() {
        return selectedVectorEntity;
    }

    public LiveData<VectorEntity> resetSelectedVectorModel(){

        MutableLiveData<VectorEntity> liveData = new MutableLiveData<>();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                BackupVector backupVector = backupModelDao
                        .getModelByID(selectedVectorEntity.getId());

                selectedVectorEntity.setModel(backupVector.getModel());
                selectedVectorEntity.setInProgress(false);
                modelDao.insertVector(selectedVectorEntity);

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        liveData.setValue(selectedVectorEntity);
                    }
                });

            }
        });

        return liveData;
    }

    public void resetVectorModel(VectorEntity vectorEntity) {

        if(artworkLivedata.getValue().containsKey(vectorEntity.getId()))
            artworkLivedata.getValue().remove(vectorEntity.getId());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                BackupVector backupVector = backupModelDao
                        .getModelByID(vectorEntity.getId());

                vectorEntity.setModel(backupVector.getModel());
                vectorEntity.setInProgress(false);
                modelDao.insertVector(vectorEntity);
            }
        });
    }

    public void notifyVectorModelChange(boolean modelHasChanged){
        vectorModelChanged.setValue(modelHasChanged);
    }

    public void saveSelectedVectorModel() {
        vectorModelChanged.setValue(true);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                selectedVectorEntity.setModel(selectedVectorEntity.getModel());

                HashMap<Integer, VectorEntity> temp = artworkLivedata.getValue();
                int id = selectedVectorEntity.getId();

                if(selectedVectorEntity.isInProgress()) {
                    temp.put(id, selectedVectorEntity);
                } else {
                    if(temp.containsKey(id))
                        temp.remove(id);
                }

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        artworkLivedata.setValue(temp);
                    }
                });
                modelDao.insertVector(selectedVectorEntity);
            }
        });
    }

    private void fetchUpdates() {
        LiveData<Resource<List<VectorEntity>>> liveUpdates = repository.fetchUpdates();
        libraryLiveData.addSource(liveUpdates, new Observer<Resource<List<VectorEntity>>>() {
                @Override
                public void onChanged(Resource<List<VectorEntity>> listResource) {
                    libraryLiveData.removeSource(liveUpdates);

                    HashMap<Integer, VectorEntity> temp = new HashMap<>();

                    for(VectorEntity vectorEntity : listResource.data){
                        if(vectorEntity.isInProgress()) {
                            temp.put(vectorEntity.getId(), vectorEntity);
                        }
                    }

                    artworkLivedata.setValue(temp);
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


    public LiveData<HashMap<Integer, VectorEntity>> getStudioLiveList() {
        return artworkLivedata;
    }



}
