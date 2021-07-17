package com.example.kidzcolor.mvvm.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.kidzcolor.AppExecutors;
import com.example.kidzcolor.firestore.FirestoreQueryLiveData;
import com.example.kidzcolor.firestore.FirestoreService;
import com.example.kidzcolor.mvvm.DataFetcher;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Repository {

    private static Repository instance;
    private ModelDao modelDao;
    private SharedPrefs sharedPrefs;
    private CollectionReference modelsFirestoreRef;
    private VectorEntity currentVectorModel = null;

    public VectorEntity getCurrentVectorModel() {
        return currentVectorModel;
    }

    public void setCurrentVectorModel(VectorEntity currentVectorModel) {
        this.currentVectorModel = currentVectorModel;
    }

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context){
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        sharedPrefs = SharedPrefs.getInstance(context);
        modelsFirestoreRef = FirestoreService.getInstance().getFilesRef();
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

                int a = sharedPrefs.getLastModified();
                Query query  = modelsFirestoreRef
                        .orderBy("id", Query.Direction.DESCENDING)
                        .whereGreaterThan("id", sharedPrefs.getLastModified())
                        .limit(FirestoreService.FETCH_COUNT);
                return new FirestoreQueryLiveData(query);
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<VectorEntity>>> fetchMore() {
        return new DataFetcher(AppExecutors.getInstance()){

            @Override
            protected void saveCallResult(QuerySnapshot queryDocumentSnapshots) {
                saveToCache(queryDocumentSnapshots);
            }

            @Override
            protected boolean shouldFetch() {
                if(!sharedPrefs.getEndReached())
                    return true;
                return false;
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

                Query query = modelsFirestoreRef
                        .orderBy("id", Query.Direction.DESCENDING)
                        .whereLessThan("id", sharedPrefs.getLastVisible())
                        .limit(FirestoreService.FETCH_COUNT);
                return new FirestoreQueryLiveData(query);
            }

        }.getAsLiveData();
    }

    private void saveToCache(QuerySnapshot queryDocumentSnapshots) {
        List<VectorEntity> list = new ArrayList<>(queryDocumentSnapshots.size());

        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
            list.add(documentSnapshot.toObject(VectorEntity.class));
        }
        modelDao.insertVectorModels(
                list.toArray(new VectorEntity[queryDocumentSnapshots.size()]));
    }
}
