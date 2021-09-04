package com.example.kidzcolor.firestore;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class FirestoreQueryLiveData extends LiveData<QuerySnapshot> {

    private final Query query;
    public boolean success;

    public FirestoreQueryLiveData(Query query) {
        this.query = query;
    }

    @Override
    protected void onActive() {
        super.onActive();

        query.get(Source.SERVER).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                success = true;
                setValue(queryDocumentSnapshots);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                success = false;
                setValue(null);
            }
        });
    }

}
