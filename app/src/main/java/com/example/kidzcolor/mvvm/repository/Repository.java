package com.example.kidzcolor.mvvm.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.kidzcolor.mvvm.FetchServerMap;
import com.example.kidzcolor.firestore.FirestoreMap;
import com.example.kidzcolor.mvvm.ModelFetcher;
import com.example.kidzcolor.utils.AppExecutors;
import com.example.kidzcolor.firestore.FirestoreQueryLiveData;
import com.example.kidzcolor.persistance.BackupModelDao;
import com.example.kidzcolor.persistance.BackupVector;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import org.jetbrains.annotations.NotNull;

public class Repository {

    private static Repository instance;
    private final ModelDao modelDao;
    private final BackupModelDao backupModelDao;

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context){
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        backupModelDao = ModelsDatabase.getInstance(context).getBackupModelsDao();
    }

    public LiveData<FirestoreMap> fetchFirestoreMap() {

        return new FetchServerMap(){

            @NonNull
            @NotNull
            @Override
            protected FirestoreQueryLiveData createCall() {
                Query query  = getFireCollectionReference()
                        .whereEqualTo("id", 0);

                return new FirestoreQueryLiveData(query);
            }
        }.getAsLiveData();
    }


    public LiveData<VectorEntity> fetchModelWithEntity(VectorEntity vectorEntity) {
        return new ModelFetcher(vectorEntity, AppExecutors.getInstance()){

            @Override
            protected void saveCallResult(VectorEntity vectorEntity) {
                modelDao.insertSingleModel(vectorEntity);
                backupModelDao.insertSingleModel(new BackupVector(vectorEntity));
            }

            @NonNull
            @NotNull
            @Override
            protected FirestoreQueryLiveData createCall(int modelID) {
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
