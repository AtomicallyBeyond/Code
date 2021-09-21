package com.digitalartsplayground.easycolor.persistance;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.digitalartsplayground.easycolor.models.VectorMasterDrawable;
import com.digitalartsplayground.easycolor.models.VectorModel;

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

    @Ignore
    public VectorEntity(int id) {
        this.id = id;
    }

    @Ignore
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
