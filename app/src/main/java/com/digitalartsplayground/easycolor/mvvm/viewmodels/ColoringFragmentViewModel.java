package com.digitalartsplayground.easycolor.mvvm.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.easycolor.models.VectorModelContainer;
import com.digitalartsplayground.easycolor.mvvm.Repository;
import com.digitalartsplayground.easycolor.mvvm.SingleLiveEvent;
import com.digitalartsplayground.easycolor.models.VectorEntity;
import com.digitalartsplayground.easycolor.models.BackupVector;
import com.digitalartsplayground.easycolor.utils.AppExecutors;

public class ColoringFragmentViewModel extends AndroidViewModel {

    private int modelID;
    private int position;
    private Repository repository;
    private MediatorLiveData<VectorModelContainer> vectorModelLiveData = new MediatorLiveData<>();
    private SingleLiveEvent<Boolean> isCompleted = new SingleLiveEvent<>();

    public ColoringFragmentViewModel(@NonNull Application application) {
        super(application);

        repository = Repository.getInstance(application);
    }

    public void fetchModel(int modelID) {

        this.modelID = modelID;
        LiveData<VectorEntity> liveModel = repository.fetchLiveModel(modelID);

        vectorModelLiveData.addSource(liveModel, new Observer<VectorEntity>() {
            @Override
            public void onChanged(VectorEntity vectorEntity) {
                if(vectorEntity != null) {
                    VectorModelContainer vectorModelContainer = new VectorModelContainer(vectorEntity);
                    vectorModelLiveData.setValue(vectorModelContainer);
                }
            }
        });
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public LiveData<VectorModelContainer> getVectorModelContainer() {
        return vectorModelLiveData;
    }


    public SingleLiveEvent<Boolean> getIsCompleted() {return isCompleted;}


    public void resetVectorModel() {
        LiveData<BackupVector> liveData = repository.fetchLiveBackUpModel(modelID);

        vectorModelLiveData.addSource(liveData, new Observer<BackupVector>() {
            @Override
            public void onChanged(BackupVector backupVector) {

                if(backupVector != null) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            repository.insertModel(new VectorEntity(backupVector));
                            AppExecutors.getInstance().mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    isCompleted.setValue(false);
                                    fetchModel(modelID);
                                }
                            });
                        }
                    });
                }

                vectorModelLiveData.removeSource(liveData);
            }
        });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if(vectorModelLiveData.getValue() != null) {
            vectorModelLiveData.getValue().saveModel();
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    repository.insertModel(vectorModelLiveData.getValue().getVectorEntity());
/*                    repository.updateSelectedModel(
                            vectorModelLiveData
                                    .getValue()
                                    .getVectorEntity()
                                    .getId());*/
                }
            });
        }
    }

}
