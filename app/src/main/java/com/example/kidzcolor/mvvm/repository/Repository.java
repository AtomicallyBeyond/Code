package com.example.kidzcolor.mvvm.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.kidzcolor.mvvm.ModelFetcher;
import com.example.kidzcolor.mvvm.SingleResource;
import com.example.kidzcolor.utils.AppExecutors;
import com.example.kidzcolor.firestore.FirestoreQueryLiveData;
import com.example.kidzcolor.mvvm.DataFetcher;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.persistance.BackupModelDao;
import com.example.kidzcolor.persistance.BackupVector;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Repository {

    public static final int FETCH_COUNT = 10;

    private static Repository instance;
    private final ModelDao modelDao;
    private final BackupModelDao backupModelDao;
    private final SharedPrefs sharedPrefs;

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context){
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        backupModelDao = ModelsDatabase.getInstance(context).getBackupModelsDao();
        sharedPrefs = SharedPrefs.getInstance(context);
    }


    public LiveData<Resource<List<VectorEntity>>> fetchUpdates() {
        return new DataFetcher(AppExecutors.getInstance()){

            @Override
            protected void saveCallResult(QuerySnapshot queryDocumentSnapshots) {

                saveToCache(queryDocumentSnapshots);
            }

            @Override
            protected boolean shouldFetch() {
                return true;
            }

            @NonNull
            @NotNull
            @Override
            protected LiveData<List<VectorEntity>> loadFromDb() {
                return modelDao.getModels();
            }

            @NonNull
            @NotNull
            @Override
            protected FirestoreQueryLiveData createCall() {

                Query query  = getFireCollectionReference()
                        .orderBy("id", Query.Direction.DESCENDING)
                        .whereGreaterThan("id", sharedPrefs.getLastModified())
                        .limit(FETCH_COUNT);
                return new FirestoreQueryLiveData(query);
            }
        }.getAsLiveData();
    }

    public LiveData<SingleResource> fetchModel(int modelID) {
        return new ModelFetcher(modelID, AppExecutors.getInstance()) {

            @Override
            protected void saveCallResult(VectorEntity vectorEntity) {
                modelDao.insertSingleModel(vectorEntity);
                backupModelDao.insertSingleModel(vectorEntity);
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

    private void saveToCache(QuerySnapshot queryDocumentSnapshots) {
        int querySize = queryDocumentSnapshots.size();
        List<VectorEntity> list = new ArrayList<>(querySize);
        List<BackupVector> backUpList = new ArrayList<>(querySize);

        VectorEntity tempEntity;
        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                tempEntity = documentSnapshot.toObject(VectorEntity.class);

                if(tempEntity != null) {
                    list.add(tempEntity);
                    backUpList.add(new BackupVector(tempEntity));
                }
        }
        modelDao.insertVectorModels(
                list.toArray(new VectorEntity[querySize]));

        backupModelDao.insertVectorModels(
                backUpList.toArray(new BackupVector[querySize]));
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
