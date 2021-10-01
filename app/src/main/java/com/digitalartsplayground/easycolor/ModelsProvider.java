package com.digitalartsplayground.easycolor;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.mvvm.SingleLiveEvent;
import com.digitalartsplayground.easycolor.mvvm.repository.Repository;
import com.digitalartsplayground.easycolor.persistance.BackupModelDao;
import com.digitalartsplayground.easycolor.persistance.BackupVector;
import com.digitalartsplayground.easycolor.persistance.ModelDao;
import com.digitalartsplayground.easycolor.persistance.ModelsDatabase;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private MediatorLiveData<Integer> liveAdapterUpdater = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> removeLoading = new MediatorLiveData<>();


    public static synchronized ModelsProvider getInstance(Context context){
        if(instance == null){
            instance = new ModelsProvider(context);
        }
        return instance;
    }

    public static ModelsProvider getExistingInstance() {
        if(instance == null)
            return null;
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
                            firestoreMap.index.removeAll(new HashSet<Integer>(modelDao.getAllIds()));
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

                            Collections.shuffle(emptyModels);
                            Collections.shuffle(databaseModels);
                            emptyModels.addAll(databaseModels);

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    artworkLivedata.setValue(artworkHashMap);
                                    liveModels.setValue(emptyModels);
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

                }//end else

            }
        });
    }


    public void fetchModelWithEntity(VectorEntity vectorEntity, int position) {

        LiveData<Boolean> liveData = repository.fetchModelWithEntity(vectorEntity);

        liveAdapterUpdater.addSource(liveData, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                liveAdapterUpdater.removeSource(liveData);

                if(aBoolean) {
                    liveAdapterUpdater.setValue(position);
                } else
                    liveAdapterUpdater.setValue(null);
            }
        });
    }


    public LiveData<List<VectorEntity>> fetchLiveModels() {return liveModels;}

    public LiveData<Boolean> getRemoveLoading(){
        return removeLoading;
    }

    public LiveData<Integer> getAdapterUpdater() {
        return liveAdapterUpdater;
    }

    public SingleLiveEvent<Boolean> getVectorModelChanged() {
        return vectorModelChanged;
    }

    public LiveData<HashMap<Integer, VectorEntity>> getArtworkLiveList() { return artworkLivedata; }

    public VectorEntity getSelectedVectorModel() {
        return selectedVectorEntity;
    }

    public SingleLiveEvent<Boolean> setSelectedVectorModel(VectorEntity selectedVectorModel) {

        SingleLiveEvent<Boolean> modelSet = new SingleLiveEvent<>();
        selectedVectorEntity = selectedVectorModel;
        modelSet.setValue(true);
        return modelSet;
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
                selectedVectorEntity.loadDrawable();
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
                vectorEntity.loadDrawable();
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
                selectedVectorEntity.loadDrawable();

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