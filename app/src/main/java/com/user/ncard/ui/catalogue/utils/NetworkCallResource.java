package com.user.ncard.ui.catalogue.utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.user.ncard.AppExecutors;
import com.user.ncard.api.ApiResponse;
import com.user.ncard.api.AppAuthenticationHandler;
import com.user.ncard.util.SharedPreferenceHelper;
import com.user.ncard.util.Utils;
import com.user.ncard.vo.Resource;

/**
 * Created by Pham on 18/8/17.
 */
public abstract class NetworkCallResource<RequestType> {
    private final AppExecutors appExecutors;

    private final MediatorLiveData<Resource<RequestType>> result = new MediatorLiveData<>();
    SharedPreferenceHelper sharedPreferenceHelper;

    @MainThread
    public NetworkCallResource(AppExecutors appExecutors, SharedPreferenceHelper sharedPreferenceHelper) {
        this.appExecutors = appExecutors;
        this.sharedPreferenceHelper = sharedPreferenceHelper;
        result.setValue(Resource.loading(null));
        fetchFromNetwork();
    }

    private void fetchFromNetwork() {
        Utils.getToken(sharedPreferenceHelper, new AppAuthenticationHandler() {
            @Override
            public void onSuccessAuthentication(@org.jetbrains.annotations.Nullable CognitoUserSession userSession, @org.jetbrains.annotations.Nullable CognitoDevice newDevice) {

                LiveData<ApiResponse<RequestType>> apiResponse = createCall();
                result.addSource(apiResponse,
                        response -> {
                            if (response.isSuccessful()) {
                                appExecutors.diskIO().execute(() -> {
                                    returnCallResult(processResponse(apiResponse.getValue()));
                                    result.postValue(Resource.success(response.getBody()));
                                });
                            } else {
                                onFetchFailed();
                                result.postValue(Resource.error(apiResponse.getValue().getErrorMessage(),
                                        apiResponse.getValue().getBody()));
                            }
                        });

            }
        });

    }

    protected void onFetchFailed() {
    }

    public LiveData<Resource<RequestType>> asLiveData() {
        return result;
    }

    @WorkerThread
    protected RequestType processResponse(ApiResponse<RequestType> response) {
        return response.getBody();
    }

    @WorkerThread
    protected abstract void returnCallResult(@NonNull RequestType item);


    @NonNull
    @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall();
}
