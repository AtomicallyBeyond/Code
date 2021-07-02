package com.example.kidzcolor.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.PathModel;
import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.repository.DrawableRepository;

import java.util.List;

public class ColoringViewModel extends AndroidViewModel implements PositionListener {

    private DrawableRepository drawableRepository;
    private int position = 0;

    public ColoringViewModel(@NonNull Application application) {
        super(application);
        drawableRepository = DrawableRepository.getInstance();
        drawableRepository.setModel(new VectorModel(application.getApplicationContext(), "shapes.xml"));
    }

    public VectorModel getVectorModel() {
        return drawableRepository.getModel();
    }

    public int getPosition() {return position;}


    @Override
    public void positionChanged(int newPosition) {
        position = newPosition;
    }
}
