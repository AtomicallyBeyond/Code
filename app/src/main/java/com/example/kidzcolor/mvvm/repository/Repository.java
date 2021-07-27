package com.example.kidzcolor.mvvm.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidzcolor.AppExecutors;
import com.example.kidzcolor.firestore.FirestoreQueryLiveData;
import com.example.kidzcolor.firestore.FirestoreService;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.DataFetcher;
import com.example.kidzcolor.mvvm.Resource;
import com.example.kidzcolor.persistance.ModelDao;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.OriginalModelDao;
import com.example.kidzcolor.persistance.SavedVector;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.utils.SharedPrefs;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Repository {

    private static Repository instance;
    private ModelDao modelDao;
    private OriginalModelDao originalModelDao;
    private SharedPrefs sharedPrefs;
    private CollectionReference modelsFirestoreRef;
    private MutableLiveData<VectorModelContainer> selectedVectorContainer = new MutableLiveData<>();

    public void setSelectedVectorModel(VectorEntity entity) {
        this.selectedVectorContainer.setValue(
                new VectorModelContainer(entity));

/*        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                originalModelDao.insertVectorModels(
                        new SavedVector(currentVectorModel.getId(), currentVectorModel.getModel()));
            }
        });*/
    }

    public MutableLiveData<VectorModelContainer> getSelectedVectorModel() {
        return selectedVectorContainer;
    }

    public void resetSelectedVectorModel(){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                VectorEntity vectorEntity = new VectorEntity(
                        originalModelDao.getModelByID(selectedVectorContainer.getValue().getId()));

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        selectedVectorContainer.setValue(new VectorModelContainer(vectorEntity));
                    }
                });
                modelDao.insertVector(vectorEntity);
            }
        });
    }

    public void saveSelectedVectorModel() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                VectorEntity entity = selectedVectorContainer.getValue().getVectorEntity();
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        selectedVectorContainer.setValue(selectedVectorContainer.getValue());
                    }
                });
                modelDao.insertVector(entity);
            }
        });
    }

    public static Repository getInstance(Context context){
        if(instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context){
        modelDao = ModelsDatabase.getInstance(context).getModelsDao();
        originalModelDao = ModelsDatabase.getInstance(context).getOriginalModelsDao();
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
        int querySize = queryDocumentSnapshots.size();
        List<VectorEntity> list = new ArrayList<>(querySize);
        List<SavedVector> backUpList = new ArrayList<>(querySize);

        VectorEntity tempEntity;
        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
            tempEntity = documentSnapshot.toObject(VectorEntity.class);
            list.add(tempEntity);
            backUpList.add(new SavedVector(tempEntity));
        }
        modelDao.insertVectorModels(
                list.toArray(new VectorEntity[querySize]));

        originalModelDao.insertVectorModels(
                backUpList.toArray(new SavedVector[querySize]));
    }
}
