package com.example.kidzcolor.mvvm;

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

import java.util.List;

public abstract class DataFetcher {
    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<List<VectorEntity>>> results = new MediatorLiveData<>();

    public DataFetcher(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
        init();
    }

    private void init() {

        final LiveData<List<VectorEntity>> dbSource = loadFromDb();

        results.addSource(dbSource, new Observer<List<VectorEntity>>() {
            @Override
            public void onChanged(List<VectorEntity> vectorEntities) {
                if(shouldFetch()) {
                    fetchFromFirestore(dbSource);
                }
                else {
                    results.addSource(dbSource, new Observer<List<VectorEntity>>() {
                        @Override
                        public void onChanged(List<VectorEntity> vectorEntities) {
                            setValue(Resource.success(vectorEntities));
                        }
                    });
                }
            }
        });
    }

    private void fetchFromFirestore(final LiveData<List<VectorEntity>> dbSource) {
        FirestoreQueryLiveData firestoreQueryLiveData = createCall();

        results.addSource(firestoreQueryLiveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot queryDocumentSnapshots) {
                results.removeSource(dbSource);
                results.removeSource(firestoreQueryLiveData);

                if(firestoreQueryLiveData.success){
                    if(queryDocumentSnapshots != null) {

                        if(queryDocumentSnapshots.size() == 0) {
                            results.addSource(dbSource, new Observer<List<VectorEntity>>() {
                                @Override
                                public void onChanged(List<VectorEntity> vectorEntities) {
                                    if(vectorEntities != null){
                                        setValue(Resource.success(vectorEntities));
                                    }
                                }
                            });
                            return;
                        }

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                saveCallResult(queryDocumentSnapshots);

                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.addSource(loadFromDb(), new Observer<List<VectorEntity>>() {
                                            @Override
                                            public void onChanged(List<VectorEntity> vectorEntities) {
                                                if(vectorEntities != null)
                                                    setValue(Resource.success(vectorEntities));
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                } else if(!firestoreQueryLiveData.success) {
                    results.addSource(dbSource, new Observer<List<VectorEntity>>() {
                        @Override
                        public void onChanged(List<VectorEntity> vectorEntities) {
                            if(vectorEntities != null)
                                setValue(Resource.error("Network connectivity issues!", vectorEntities));
                        }
                    });
                } //end else if !firestoreQueryLiveData.success

            }
        });
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(QuerySnapshot queryDocumentSnapshots);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch();

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<List<VectorEntity>> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract FirestoreQueryLiveData createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<List<VectorEntity>>> getAsLiveData(){
        return results;
    };

    private void setValue(Resource<List<VectorEntity>> newValue){
        if(results.getValue() != newValue){
            results.setValue(newValue);
        }
    }

}
