package com.example.kidzcolor.viewmodels;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.kidzcolor.PositionListener;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.repository.DrawableRepository;

public class ColoringViewModel extends AndroidViewModel implements PositionListener {

    private DrawableRepository drawableRepository;
    private VectorMasterDrawable vectorDrawable;
    private VectorModel vectorModel;
    private int position = 0;

    public ColoringViewModel(@NonNull Application application) {
        super(application);
        vectorModel = new VectorModel(application.getApplicationContext(), "ic_school.xml");
        vectorDrawable = new VectorMasterDrawable(vectorModel);
    }

    public VectorModel getVectorModel() {
        return vectorModel;
    }

    public int getPosition() {return position;}

    @Override
    public void positionChanged(int newPosition) {
        position = newPosition;
    }
}
