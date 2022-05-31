package com.digitalartsplayground.easycolor.mvvm;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.utils.AppExecutors;
import com.digitalartsplayground.easycolor.utils.SharedPrefs;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class FetchServerMap {

    private int fetchCount = 0;
    private final long randomSeed;
    private final AppExecutors appExecutors;
    private final MediatorLiveData<FirestoreMap> liveFireMap = new MediatorLiveData<>();

    public FetchServerMap(long randomSeed) {
        this.randomSeed = randomSeed;
        appExecutors = AppExecutors.getInstance();
        init();
    }


    private void init() {

        if(shouldFetch()) {
            fetchFromFirestore();
        } else {
            liveFireMap.addSource(loadFromDb(), new Observer<FirestoreMap>() {
                @Override
                public void onChanged(FirestoreMap firestoreMap) {
                    if(firestoreMap != null) {
                        firestoreMap.shuffleList(randomSeed);
                        liveFireMap.setValue(firestoreMap);
                    }
                }
            });
        }
    }


    private void fetchFromFirestore() {
        FirestoreQueryLiveData firestoreQueryLiveData = createCall();

        liveFireMap.addSource(firestoreQueryLiveData, new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(QuerySnapshot queryDocumentSnapshots) {


                if(firestoreQueryLiveData.success){

                    if(queryDocumentSnapshots != null) {

                        if(queryDocumentSnapshots.size() == 0) {
                            liveFireMap.removeSource(firestoreQueryLiveData);
                            liveFireMap.setValue(null);
                            return;
                        }

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {

                                FirestoreMap firestoreMap = queryDocumentSnapshots
                                        .getDocuments()
                                        .get(0)
                                        .toObject(FirestoreMap.class);
                                saveCallResult(firestoreMap);

                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveFireMap.addSource(loadFromDb(), new Observer<FirestoreMap>() {
                                            @Override
                                            public void onChanged(FirestoreMap firestoreMap) {
                                                if(firestoreMap != null) {
                                                    liveFireMap.setValue(firestoreMap);
                                                }
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
    protected abstract void saveCallResult(FirestoreMap firestoreMap);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch();

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<FirestoreMap> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract FirestoreQueryLiveData createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<FirestoreMap> getAsLiveData(){
        return liveFireMap;
    }

}