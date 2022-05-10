package com.digitalartsplayground.easycolor.mvvm.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.mvvm.Repository;
import com.digitalartsplayground.easycolor.persistance.BackupVector;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;


public class MainActivityViewModel extends AndroidViewModel {

    private final Repository repository;
    private final SharedPrefs sharedPrefs;

    private final MediatorLiveData<FirestoreMap> liveFirestoreMap = new MediatorLiveData<>();
    private final MediatorLiveData<List<Integer>> liveArtworkIDs = new MediatorLiveData<>();

    //Integer in HashMap is the modelID
    private final HashMap<Integer, VectorEntity> modelHashMap = new HashMap<>();
    private final HashMap<Integer, VectorEntity> artworkHashMap = new HashMap<>();

    private boolean isLibraryCurrent = true;


    public MainActivityViewModel(@NonNull @NotNull Application application) {
        super(application);
        sharedPrefs = SharedPrefs.getInstance(application);
        sharedPrefs.setRandomSeed(System.currentTimeMillis());
        repository = Repository.getInstance(application);
    }

    public LiveData<List<Integer>> getLiveArtworkIDs() {
        return liveArtworkIDs;
    }

    public HashMap<Integer, VectorEntity> getArtworkHashMap() {
        return artworkHashMap;
    }

    public void loadArtworkIDs() {
        liveArtworkIDs.addSource(repository.fetchArtworkIDs(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> integers) {
                if(integers != null) {
                    liveArtworkIDs.setValue(integers);
                }
            }
        });
    }

    public void fetchArtwork(int modelID) {
        LiveData<VectorEntity> liveModel = repository.fetchModelFromServer(modelID);
        liveArtworkIDs.addSource(liveModel, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {

                if(vectorEntity != null) {

                    VectorEntity savedVector = artworkHashMap.get(vectorEntity.getId());

                    if(savedVector == null) {
                        artworkHashMap.put(vectorEntity.getId(), vectorEntity);
                        vectorEntity.loadDrawable();
                    } else {
                        savedVector.setId(vectorEntity.getId());
                        savedVector.setModel(vectorEntity.getModel());
                        savedVector.loadDrawable();
                    }
                }

                liveArtworkIDs.removeSource(liveModel);
            }
        });
    }


    // FirestoreMap contains ids off all available models from cache.
    public void loadFirestoreMap() {

        long currentTime = System.currentTimeMillis();
        boolean fetchFromServer;

        if((currentTime - sharedPrefs.getFirestoreFetchedTime()) > (24 * 60 * 60 * 1000))
            fetchFromServer = true;
        else
            fetchFromServer = false;

        liveFirestoreMap.addSource(repository.fetchFirestoreMap(fetchFromServer), new Observer<FirestoreMap>() {
            @Override
            public void onChanged(FirestoreMap firestoreMap) {
                if(firestoreMap != null) {
                    liveFirestoreMap.setValue(firestoreMap);
                }
            }
        });
    }

    public LiveData<FirestoreMap> getLiveFirestoreMap() {
        return liveFirestoreMap;
    }


    public HashMap<Integer, VectorEntity> getModelHashMap() {
        return modelHashMap;
    }


    public void fetchModel(int modelID) {

        LiveData<VectorEntity> liveModel = repository.fetchModelFromServer(modelID);
        liveFirestoreMap.addSource(liveModel, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {

                if(vectorEntity != null) {

                    VectorEntity saveVector = modelHashMap.get(vectorEntity.getId());

                    if(saveVector == null) {
                        modelHashMap.put(vectorEntity.getId(), vectorEntity);
                        vectorEntity.loadDrawable();
                    } else {
                        saveVector.setId(vectorEntity.getId());
                        saveVector.setModel(vectorEntity.getModel());
                        saveVector.loadDrawable();
                    }
                }

                liveFirestoreMap.removeSource(liveModel);
            }
        });
    }


    public void resetVectorModel(int modelID) {

        LiveData<BackupVector> liveData = repository.fetchLiveBackUpModel(modelID);
        liveArtworkIDs.addSource(liveData, new Observer<BackupVector>() {
            @Override
            public void onChanged(BackupVector backupVector) {

                if(backupVector != null) {

                    final int modelID = backupVector.getSavedID();
                    VectorEntity modelEntity = modelHashMap.get(modelID);

                    if(modelEntity != null) {
                        modelEntity.setModel(backupVector.getModel());
                        modelEntity.loadDrawable();
                    }

                    modelEntity = artworkHashMap.get(modelID);
                    if(modelEntity != null) {
                        artworkHashMap.remove(backupVector.getSavedID());
                    }


                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            repository.insertModel(new VectorEntity(backupVector));
                        }
                    });
                }

                liveArtworkIDs.removeSource(liveData);
            }
        });
    }

    public boolean isLibraryCurrent() {
        return isLibraryCurrent;
    }

    public void setLibraryCurrent(boolean libraryCurrent) {
        isLibraryCurrent = libraryCurrent;
    }
}
