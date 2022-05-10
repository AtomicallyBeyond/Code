package com.digitalartsplayground.easycolor.mvvm;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.easycolor.persistance.BackupModelDao;
import com.digitalartsplayground.easycolor.persistance.BackupVector;
import com.digitalartsplayground.easycolor.persistance.ModelIDsDao;
import com.digitalartsplayground.easycolor.persistance.ModelsDatabase;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.digitalartsplayground.easycolor.persistance.ModelDao;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Repository {

    private static Repository instance;
    private final ModelDao modelDao;
    private final BackupModelDao backupModelDao;
    private final ModelIDsDao modelIDsDao;
    private final long randomSeed;

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context){
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        backupModelDao = ModelsDatabase.getInstance(context).getBackupModelsDao();
        modelIDsDao = ModelsDatabase.getInstance(context).getModelIDsDao();
        randomSeed = SharedPrefs.getInstance(context).getRandomSeed();
    }

    public void insertModel(VectorEntity vectorEntity) {
        modelDao.insertVector(vectorEntity);
    }

    public LiveData<VectorEntity> fetchLiveModel(int modelID) {
        return modelDao.getLiveModel(modelID);
    }

    public LiveData<BackupVector> fetchLiveBackUpModel(int modelID) {
        return backupModelDao.getLiveModel(modelID);
    }

    public LiveData<List<VectorEntity>> fetchLiveArtworkList() {
        return modelDao.getLiveModelsInProgress();
    }

    public LiveData<FirestoreMap> fetchFirestoreMap(boolean fetchFromServer) {

        return new FetchServerMap(randomSeed) {
            @Override
            protected void saveCallResult(FirestoreMap firestoreMap) {
                modelIDsDao.insertFirestoreMap(firestoreMap);
            }

            @Override
            protected boolean shouldFetch() {
                return fetchFromServer;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<FirestoreMap> loadFromDb() {
                return modelIDsDao.getLiveFirestoreMap();
            }

            @NonNull
            @NotNull
            @Override
            protected FirestoreQueryLiveData createCall() {
                Query query
                        = getFireCollectionReference().whereEqualTo("id", 0);
                return new FirestoreQueryLiveData(query);
            }

        }.getAsLiveData();
    }


    public LiveData<VectorEntity> fetchModelFromServer(int modelID) {
        return new ModelFetcher(){

            @Override
            protected void saveCallResult(VectorEntity vectorEntity) {
                modelDao.insertVector(vectorEntity);
                backupModelDao.insertSingleModel(new BackupVector(vectorEntity));
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<VectorEntity> loadFromDb() {
                return modelDao.getLiveModel(modelID);
            }

            @NonNull
            @NotNull
            @Override
            protected FirestoreQueryLiveData createCall() {
                Query query = getFireCollectionReference()
                        .whereEqualTo("id", modelID);
                return new FirestoreQueryLiveData(query);
            }

        }.getAsLiveData();
    }


    private CollectionReference getFireCollectionReference() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.setFirestoreSettings(settings);
        return database.collection("models");
    }

}