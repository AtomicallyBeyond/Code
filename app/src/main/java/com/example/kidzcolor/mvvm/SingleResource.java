package com.example.kidzcolor.mvvm;

import com.example.kidzcolor.persistance.VectorEntity;

public class SingleResource {

    public Status status;

    private VectorEntity vectorEntity;

    public VectorEntity getVectorEntity() {
        return vectorEntity;
    }

    public SingleResource(int modelID){
        this.vectorEntity = new VectorEntity();
        vectorEntity.setId(modelID);
    }

    public SingleResource success(String model) {
        vectorEntity.setModel(model);
        status = Status.SUCCESS;
        return this;
    }

    public SingleResource error() {
        status = Status.ERROR;
        return this;
    }

    public SingleResource loading() {
        status = Status.LOADING;
        return this;
    }

    public enum Status { SUCCESS, ERROR, LOADING}
}
