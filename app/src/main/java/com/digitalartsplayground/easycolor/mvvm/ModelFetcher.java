package com.digitalartsplayground.easycolor.mvvm;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.firestore.FirestoreEntity;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public abstract class ModelFetcher {
    private int fetchCount = 0;
    private final AppExecutors appExecutors;
    private final MediatorLiveData<VectorEntity> liveVectorEntity = new MediatorLiveData<>();

    public ModelFetcher() {
        this.appExecutors = AppExecutors.getInstance();

        LiveData<VectorEntity> liveDBModel = loadFromDb();

        liveVectorEntity.addSource(liveDBModel, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {
                if(vectorEntity != null) {
                    liveVectorEntity.setValue(vectorEntity);
                } else {
                    liveVectorEntity.removeSource(liveDBModel);
                    fetchFromFirestore();
                }
            }
        });
    }

    private void fetchFromFirestore() {
        FirestoreQueryLiveData firestoreQueryLiveData = createCall();

        liveVectorEntity.addSource(firestoreQueryLiveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot queryDocumentSnapshots) {


                if(firestoreQueryLiveData.success){

                    if(queryDocumentSnapshots != null) {

                        if(queryDocumentSnapshots.size() == 0) {
                            liveVectorEntity.removeSource(firestoreQueryLiveData);
                            liveVectorEntity.setValue(null);
                            return;
                        }

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {

                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots
                                        .getDocuments()
                                        .get(0);

                                FirestoreEntity firestoreEntity = documentSnapshot
                                        .toObject(FirestoreEntity.class);

                                VectorEntity vectorEntity = new VectorEntity();
                                vectorEntity.setModel(firestoreEntity.getModel());
                                vectorEntity.setId(firestoreEntity.getId());
                                saveCallResult(vectorEntity);

                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveVectorEntity.addSource(loadFromDb(), new Observer<VectorEntity>() {
                                            @Override
                                            public void onChanged(VectorEntity vectorEntity) {
                                                liveVectorEntity.setValue(vectorEntity);
                                            }
                                        });

                                    }
                                });
                            }
                        });

                    }
                } else {

                    if(fetchCount < 2) {
                        fetchCount++;
                        fetchFromFirestore();
                    }
                }
            }
        });
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(VectorEntity vectorEntity);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<VectorEntity> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract FirestoreQueryLiveData createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<VectorEntity> getAsLiveData(){
        return liveVectorEntity;
    }

}

