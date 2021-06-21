package com.example.kidzcolor.repository;

import com.example.kidzcolor.models.VectorModel;

public class DrawableRepository {

    private static DrawableRepository instance;
    private VectorModel selectedModel;

    private DrawableRepository(){
    }

    public static DrawableRepository getInstance(){
        if(instance == null)
            instance = new DrawableRepository();
        return instance;
    }

    //only temporary
    public void setModel(VectorModel vectorModel){
        selectedModel = vectorModel;
    }

    public VectorModel getModel() {
        return selectedModel;
    }
}
