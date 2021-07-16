package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.VectorEntity;

public class ColoringViewModel extends AndroidViewModel implements PositionListener {

    private Repository repository;
    private VectorModelContainer vectorModelContainer;
    private int position = 0;

    public ColoringViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        vectorModelContainer = new VectorModelContainer(repository.getCurrentVectorModel().model);
        //VectorModelContainer vectorModel = new VectorModel(application.getApplicationContext(), "shapes.xml");
        //drawableRepository.setModel(vectorModel);
    }

    public VectorModelContainer getVectorModelContainer() {
        return vectorModelContainer;
    }

    public int getPosition() {return position;}


    @Override
    public void positionChanged(int newPosition) {
        position = newPosition;
    }
}
