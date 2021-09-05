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
    private final int modelID;
    private final AppExecutors appExecutors;
    private final MediatorLiveData<SingleResource> liveVectorEntity = new MediatorLiveData<>();

    public ModelFetcher(int modelID, AppExecutors appExecutors) {
        this.modelID = modelID;
        this.appExecutors = appExecutors;
        init();
    }

    private void init() {
        SingleResource singleResource = new SingleResource(modelID);
        liveVectorEntity.setValue(singleResource.loading());
        fetchFromFirestore();
    }

    private void fetchFromFirestore() {
        FirestoreQueryLiveData firestoreQueryLiveData = createCall(modelID);

        liveVectorEntity.addSource(firestoreQueryLiveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot queryDocumentSnapshots) {

                if(firestoreQueryLiveData.success){

                    if(queryDocumentSnapshots != null) {

                        if(queryDocumentSnapshots.size() == 0) {
                            liveVectorEntity.setValue(liveVectorEntity.getValue().error());
                            return;
                        }

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {

                                VectorEntity vectorEntity = queryDocumentSnapshots
                                        .getDocuments()
                                        .get(0)
                                        .toObject(VectorEntity.class);

                                saveCallResult(vectorEntity);


                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {

                                        liveVectorEntity.getValue().success(vectorEntity.getModel());
                                        /*liveVectorEntity
                                                .setValue(liveVectorEntity.getValue().success(vectorEntity.getModel()))*/;
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
    public final LiveData<SingleResource> getAsLiveData(){
        return liveVectorEntity;
    }

}

