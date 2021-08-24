package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.ModelsProvider;
import com.example.kidzcolor.mvvm.SingleLiveEvent;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.VectorEntity;

public class ColoringViewModel extends AndroidViewModel implements PositionListener {

    private ModelsProvider modelsProvider;
    private MediatorLiveData<VectorModelContainer> vectorModelLiveData = new MediatorLiveData<>();
    private SingleLiveEvent<Boolean> isCompleted = new SingleLiveEvent<>();
    private int position = 0;

    public SingleLiveEvent<Boolean> getIsCompleted() {return isCompleted;}

    public ColoringViewModel(@NonNull Application application) {
        super(application);

        modelsProvider = ModelsProvider.getInstance(application);
        VectorModelContainer vectorModelContainer = new VectorModelContainer(modelsProvider.getSelectedVectorModel());
        vectorModelLiveData.setValue(vectorModelContainer);

    }

    public LiveData<VectorModelContainer> getVectorModelContainer() {
        return vectorModelLiveData;
    }

    public int getPosition() {return position;}

    @Override
    public void positionChanged(int newPosition) {
        position = newPosition;
    }

    public void resetVectorModel() {
        LiveData<VectorEntity> liveData = modelsProvider.resetSelectedVectorModel();
        vectorModelLiveData.addSource(liveData, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {
                vectorModelLiveData.removeSource(liveData);
                position = 0;
                vectorModelLiveData.setValue(new VectorModelContainer(vectorEntity));
                isCompleted.setValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        vectorModelLiveData.getValue().saveModel();
        modelsProvider.saveSelectedVectorModel();
    }
}
