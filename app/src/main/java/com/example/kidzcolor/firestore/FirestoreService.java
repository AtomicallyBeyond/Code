package com.example.kidzcolor.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirestoreService {

    public static final int FETCH_COUNT = 10;
    private static FirestoreService instance;
    private static FirebaseFirestore database;
    private static CollectionReference filesRef;

    public static FirestoreService getInstance() {
        if(instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    private FirestoreService(){
        database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        database.setFirestoreSettings(settings);
        filesRef = database.collection("models");
    }

    public CollectionReference getFilesRef(){
        return filesRef;}
}
