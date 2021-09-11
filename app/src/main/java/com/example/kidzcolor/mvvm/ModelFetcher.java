package com.example.kidzcolor.mvvm;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.utils.AppExecutors;
import com.example.kidzcolor.firestore.FirestoreQueryLiveData;
import com.example.kidzcolor.persistance.VectorEntity;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class ModelFetcher {
    private VectorEntity vectorEntity;
    private final AppExecutors appExecutors;
    private final MediatorLiveData<VectorEntity> liveVectorEntity = new MediatorLiveData<>();

    public ModelFetcher(VectorEntity vectorEntity, AppExecutors appExecutors) {
        this.vectorEntity = vectorEntity;
        this.appExecutors = appExecutors;
        fetchFromFirestore();
    }



    private void fetchFromFirestore() {
        FirestoreQueryLiveData firestoreQueryLiveData = createCall(vectorEntity.getId());

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

                                ModelFetcher.this.vectorEntity = queryDocumentSnapshots
                                        .getDocuments()
                                        .get(0)
                                        .toObject(VectorEntity.class);

                                saveCallResult(vectorEntity);


                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveVectorEntity.removeSource(firestoreQueryLiveData);
                                        liveVectorEntity.setValue(vectorEntity);

                                    }
                                });
                            }
                        });

                    }
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchFromFirestore();
                        }
                    }, 1000);
                }
            }
        });
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(VectorEntity vectorEntity);

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract FirestoreQueryLiveData createCall(int modelID);

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<VectorEntity> getAsLiveData(){
        return liveVectorEntity;
    }

}

