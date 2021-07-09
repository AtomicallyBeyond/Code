package com.example.kidzcolor.utils;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.kidzcolor.AppExecutors;

public abstract class NetworkBoundResource<CacheObject, RequestObject> {

    private static final String TAG = "NetworkBoundResource";

    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<CacheObject>> modelResults = new MediatorLiveData<>();

    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();

    }

    private void init(){

        // update LiveData for loading status
        modelResults.setValue((Resource<CacheObject>) Resource.loading(null));

        // observe LiveData source from local db
        final LiveData<CacheObject> dbSource = loadFromDb();

        modelResults.addSource(dbSource, new Observer<CacheObject>() {

            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {

                modelResults.removeSource(dbSource);

                if(shouldFetch(cacheObject)){
                    // get data from the network
                    fetchFromNetwork(dbSource);
                }
                else{
                    modelResults.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(Resource.success(cacheObject));
                        }
                    });
                }
            }
        });
    }

    /**
     * 1) observe local db
     * 2) if <condition/> query the network
     * 3) stop observing the local db
     * 4) insert new data into local db
     * 5) begin observing local db again to see the refreshed data from network
     * @param dbSource
     */
    private void fetchFromNetwork(final LiveData<CacheObject> dbSource){

        Log.d(TAG, "fetchFromNetwork: called.");

        // update LiveData for loading status
        modelResults.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {
                setValue(Resource.loading(cacheObject));
            }
        });

        final LiveData<ApiResponse<RequestObject>> apiResponse = createCall();

        modelResults.addSource(apiResponse, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(@Nullable final ApiResponse<RequestObject> requestObjectApiResponse) {
                modelResults.removeSource(dbSource);
                modelResults.removeSource(apiResponse);

                /*
                    3 cases:
                       1) ApiSuccessResponse
                       2) ApiErrorResponse
                       3) ApiEmptyResponse
                 */

                if(requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse){
                    Log.d(TAG, "onChanged: ApiSuccessResponse.");

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {

                            // save the response to the local db
                            saveCallResult((RequestObject) processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse));

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    modelResults.addSource(loadFromDb(), new Observer<CacheObject>() {
                                        @Override
                                        public void onChanged(@Nullable CacheObject cacheObject) {
                                            setValue(Resource.success(cacheObject));
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse){
                    Log.d(TAG, "onChanged: ApiEmptyResponse");
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            modelResults.addSource(loadFromDb(), new Observer<CacheObject>() {
                                @Override
                                public void onChanged(@Nullable CacheObject cacheObject) {
                                    setValue(Resource.success(cacheObject));
                                }
                            });
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse){
                    Log.d(TAG, "onChanged: ApiErrorResponse.");
                    modelResults.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(
                                    Resource.error(
                                            ((ApiResponse.ApiErrorResponse) requestObjectApiResponse).getErrorMessage(),
                                            cacheObject
                                    )
                            );
                        }
                    });
                }
            }
        });
    }

    private CacheObject processResponse(ApiResponse.ApiSuccessResponse response){
        return (CacheObject) response.getBody();
    }

    private void setValue(Resource<CacheObject> newValue){
        if(modelResults.getValue() != newValue){
            modelResults.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestObject item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable CacheObject data);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<CacheObject> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<CacheObject>> getAsLiveData(){
        return modelResults;
    };
}
