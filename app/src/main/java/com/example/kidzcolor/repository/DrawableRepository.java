package com.example.kidzcolor.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidzcolor.FireBaseStorage;
import com.example.kidzcolor.models.PathModel;
import com.example.kidzcolor.models.ReplayDrawable;
import com.example.kidzcolor.models.VectorModel;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.persistance.ModelDAO;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.utils.Resource;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DrawableRepository {

    private static DrawableRepository instance;
    private MutableLiveData<VectorModelContainer> selectedModel;
    private FireBaseStorage fireBaseStorage;
    private ModelDAO modelDAO;

    private DrawableRepository(Context context) {
        selectedModel = new MutableLiveData<>();
        fireBaseStorage = new FireBaseStorage();
        modelDAO = ModelsDatabase.getInstance(context).getModelDAO();
    }

    public static DrawableRepository getInstance(Context context) {
        if(instance == null)
            instance = new DrawableRepository(context);
        return instance;
    }


    public LiveData<Resource<List<VectorModel>>> updateModelList(){}

    public LiveData<VectorModelContainer> getModel() {
        return selectedModel;
    }
}
