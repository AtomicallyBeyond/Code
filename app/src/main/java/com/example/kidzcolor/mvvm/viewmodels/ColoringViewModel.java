package com.example.kidzcolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidzcolor.AppExecutors;
import com.example.kidzcolor.interfaces.PositionListener;
import com.example.kidzcolor.models.VectorModelContainer;
import com.example.kidzcolor.mvvm.repository.Repository;
import com.example.kidzcolor.persistance.ModelsDatabase;
import com.example.kidzcolor.persistance.VectorEntity;
import com.example.kidzcolor.persistance.SavedVector;

public class ColoringViewModel extends AndroidViewModel implements PositionListener {

    private Repository repository;
/*    private LiveData<VectorEntity> vectorEntity;
    private MutableLiveData<VectorModelContainer> vectorModelContainer;*/
    private int position = 0;

    public ColoringViewModel(@NonNull Application application) {
        super(application);

/*        vectorEntity = repository.getSelectedVectorModel();*/

        repository = Repository.getInstance(application);
/*        vectorEntity = repository.getSelectedVectorModel().getValue();
        vectorModelContainer  = new VectorModelContainer(
                vectorEntity.getId(),
                vectorEntity.getModel());*/
    }

    public LiveData<VectorModelContainer> getVectorModelContainer() {
        return repository.getSelectedVectorModel();
    }

    public int getPosition() {return position;}


    @Override
    public void positionChanged(int newPosition) {
        position = newPosition;
    }

    public void resetVectorModel() {
        repository.resetSelectedVectorModel();
    }

    public void saveVectorModel(){repository.saveSelectedVectorModel();}

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.saveSelectedVectorModel();
    }
}
