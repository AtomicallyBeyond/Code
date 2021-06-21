package com.example.kidzcolor.viewmodels;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.repository.DrawableRepository;

public class ColoringViewModel extends AndroidViewModel {

    private DrawableRepository drawableRepository;
    private VectorMasterDrawable vectorDrawable;
    private VectorModel vectorModel;

    public ColoringViewModel(@NonNull Application application) {
        super(application);
        vectorModel = new VectorModel(application.getApplicationContext(), "ic_park.xml");
        vectorDrawable = new VectorMasterDrawable(vectorModel);
    }

    public VectorModel getVectorModel() {
        return vectorModel;
    }

}
