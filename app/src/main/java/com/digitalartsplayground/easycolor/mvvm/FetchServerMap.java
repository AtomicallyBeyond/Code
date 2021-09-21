package com.digitalartsplayground.easycolor.mvvm;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.easycolor.firestore.FirestoreMap;
import com.digitalartsplayground.easycolor.firestore.FirestoreQueryLiveData;
import com.google.firebase.firestore.QuerySnapshot;

public abstract class FetchServerMap {
    private final MediatorLiveData<FirestoreMap> liveFireMap = new MediatorLiveData<>();
    private Boolean loadLocalCache = false;

    public FetchServerMap() {
        fetchFromFirestore();
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
                            loadLocalCache = true;
                            return;
                        }

                        FirestoreMap firestoreMap = queryDocumentSnapshots
                                .getDocuments()
                                .get(0)
                                .toObject(FirestoreMap.class);

                        liveFireMap.removeSource(firestoreQueryLiveData);
                        liveFireMap.setValue(firestoreMap);

                    }
                } else {

                    if(!loadLocalCache) {
                        liveFireMap.setValue(null);
                        loadLocalCache = true;
                    }
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

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract FirestoreQueryLiveData createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<FirestoreMap> getAsLiveData(){
        return liveFireMap;
    }

}