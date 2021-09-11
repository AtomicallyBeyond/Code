package com.example.kidzcolor;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.kidzcolor.firestore.FirestoreMap;
import com.example.kidzcolor.mvvm.ModelResource;
import com.example.kidzcolor.mvvm.SingleLiveEvent;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.BackupModelDao;
import com.example.kidzcolor.persistance.BackupVector;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.AppExecutors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModelsProvider {

    private static ModelsProvider instance;
    private final Repository repository;
    private final ModelDao modelDao;
    private final BackupModelDao backupModelDao;
    private final AppExecutors appExecutors;
    private VectorEntity selectedVectorEntity;
    private final SingleLiveEvent<Boolean> vectorModelChanged = new SingleLiveEvent<>();
    private final MediatorLiveData<HashMap<Integer, VectorEntity>> artworkLivedata = new MediatorLiveData<>();
    private MediatorLiveData<List<VectorEntity>> liveModels = new MediatorLiveData<>();
    private MediatorLiveData<ModelResource> liveAdapterUpdater = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> removeLoading = new MediatorLiveData<>();


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
        appExecutors = AppExecutors.getInstance();
        initializeModelProvider();

    }


    public void initializeModelProvider(){
        LiveData<FirestoreMap> liveMap = repository.fetchFirestoreMap();

        liveModels.addSource(liveMap, new Observer<FirestoreMap>() {
            @Override
            public void onChanged(FirestoreMap firestoreMap) {

                if(firestoreMap != null) {
                    liveModels.removeSource(liveMap);

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            firestoreMap.index.removeAll(modelDao.getAllIds());
                            List<VectorEntity> emptyModels = new ArrayList<>(firestoreMap.index.size());

                            for(Integer id : firestoreMap.index) {
                                emptyModels.add(new VectorEntity(id));
                            }

                            List<VectorEntity> databaseModels = modelDao.getModels();
                            HashMap<Integer, VectorEntity> artworkHashMap = new HashMap<>();

                            for(VectorEntity vectorEntity : databaseModels){
                                if(vectorEntity.isInProgress()) {
                                    artworkHashMap.put(vectorEntity.getId(), vectorEntity);
                                }
                            }

                            databaseModels.addAll(emptyModels);
                            Collections.shuffle(databaseModels);

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    artworkLivedata.setValue(artworkHashMap);
                                    liveModels.setValue(databaseModels);
                                    removeLoading.setValue(true);
                                }
                            });
                        }
                    });
                } else {
                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            List<VectorEntity> databaseModels = modelDao.getModels();
                            HashMap<Integer, VectorEntity> artworkHashMap = new HashMap<>();

                            for(VectorEntity vectorEntity : databaseModels){
                                if(vectorEntity.isInProgress()) {
                                    artworkHashMap.put(vectorEntity.getId(), vectorEntity);
                                }
                            }

                            Collections.shuffle(databaseModels);

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    artworkLivedata.setValue(artworkHashMap);
                                    liveModels.setValue(databaseModels);
                                    removeLoading.setValue(true);
                                }
                            });
                        }
                    });
                }

            }
        });
    }


    public void fetchModelWithEntity(VectorEntity vectorEntity, int position) {

        LiveData<VectorEntity> liveData = repository.fetchModelWithEntity(vectorEntity);

        liveAdapterUpdater.addSource(liveData, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {
                liveAdapterUpdater.removeSource(liveData);

                if(vectorEntity != null) {
                    ModelResource modelResource = new ModelResource();
                    modelResource.vectorEntity = vectorEntity;
                    modelResource.position = position;
                    liveAdapterUpdater.setValue(modelResource);
                } else
                    liveAdapterUpdater.setValue(null);
            }
        });
    }


    public LiveData<List<VectorEntity>> fetchLiveModels() {return liveModels;}

    public LiveData<Boolean> getRemoveLoading(){
        return removeLoading;
    }

    public LiveData<ModelResource> getAdapterUpdater() {
        return liveAdapterUpdater;
    }

    public SingleLiveEvent<Boolean> getVectorModelChanged() {
        return vectorModelChanged;
    }

    public LiveData<HashMap<Integer, VectorEntity>> getArtworkLiveList() { return artworkLivedata; }

    public VectorEntity getSelectedVectorModel() {
        return selectedVectorEntity;
    }

    public void setSelectedVectorModel(VectorEntity selectedVectorModel) {
        selectedVectorEntity = selectedVectorModel;
    }

    public void notifyVectorModelChange(boolean modelHasChanged){
        vectorModelChanged.setValue(modelHasChanged);
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

}