package com.digitalartsplayground.easycolor.mvvm;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.easycolor.persistance.VectorEntity;
import com.digitalartsplayground.easycolor.firestore.FirestoreEntity;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class ModelFetcher {
    private VectorEntity vectorEntity;
    private final AppExecutors appExecutors;
    private final MediatorLiveData<Boolean> liveVectorEntity = new MediatorLiveData<>();

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
                            liveVectorEntity.setValue(false);
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

                                    ModelFetcher.this.vectorEntity.setModel(firestoreEntity.getModel());

                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveVectorEntity.removeSource(firestoreQueryLiveData);
                                        liveVectorEntity.setValue(true);

                                    }
                                });

                                saveCallResult(ModelFetcher.this.vectorEntity);

                                if(firestoreEntity.getCount() != null)
                                    updateFirestoreCount(firestoreEntity.getCount() + 1, documentSnapshot.getReference());
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

    // Update document count on Firestore
    @WorkerThread
    protected abstract void updateFirestoreCount(int count, DocumentReference documentReference);

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Boolean> getAsLiveData(){
        return liveVectorEntity;
    }

}

