package com.example.kidzcolor;


import androidx.annotation.NonNull;

import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.repository.DrawableRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class FireBaseStorage {

    private StorageReference firebase;
    private File storageFile;
    private boolean success = false;

    public FireBaseStorage() {
        firebase = FirebaseStorage.getInstance().getReference();

        try {

            final File storageFile = File.createTempFile("school", "xml");
            StorageReference childRef = firebase.child("school2.xml");
            childRef.getFile(storageFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    success = true;
                    DrawableRepository.getInstance().setModel(new VectorModelContainer(storageFile));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    success = false;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getStorageFile() {return storageFile;}

    public boolean isSuccess(){return success;}
}
