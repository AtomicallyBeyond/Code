package com.digitalartsplayground.easycolor.mvvm.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.digitalartsplayground.easycolor.mvvm.FetchServerMap;
import com.digitalartsplayground.easycolor.mvvm.ModelFetcher;
import com.digitalartsplayground.easycolor.persistance.BackupModelDao;
import com.digitalartsplayground.easycolor.persistance.BackupVector;
import com.digitalartsplayground.easycolor.persistance.ModelsDatabase;
import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.digitalartsplayground.easycolor.persistance.ModelDao;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
                        .whereEqualTo("id", -1);

                return new FirestoreQueryLiveData(query);
            }
        }.getAsLiveData();
    }


    public LiveData<Boolean> fetchModelWithEntity(VectorEntity vectorEntity) {
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

            @Override
            protected void updateFirestoreCount(int count, DocumentReference documentReference) {
                documentReference.update("count", count);
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
