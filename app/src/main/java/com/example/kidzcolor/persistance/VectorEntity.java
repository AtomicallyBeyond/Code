package com.example.kidzcolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;

@Entity(tableName = "models")
public class VectorEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "model")
    private String model;

    @ColumnInfo(name = "in_progress")
    private boolean isInProgress = false;

    @Ignore
    private VectorMasterDrawable vectorMasterDrawable;

    @Ignore
    public VectorMasterDrawable getDrawable() {
        return vectorMasterDrawable;
    }

    @Ignore
    public boolean isModelLoaded() {
        if(vectorMasterDrawable == null)
            return false;
        return true;
    }

    public VectorEntity() {

    }

    public VectorEntity(BackupVector savedVector) {
        id = savedVector.getSavedID();
        model = savedVector.getModel();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        vectorMasterDrawable = new VectorMasterDrawable(new VectorModel(model));
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }
}
