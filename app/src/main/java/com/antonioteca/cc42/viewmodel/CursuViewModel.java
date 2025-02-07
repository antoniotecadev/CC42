package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Cursu;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.CursuRepository;
import com.antonioteca.cc42.utility.EventObserver;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CursuViewModel extends ViewModel {

    private final CursuRepository cursuRepository;

    private MutableLiveData<List<Cursu>> cursuListMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveDataCursu;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveDataCursu;

    public CursuViewModel(CursuRepository cursuRepository) {
        this.cursuRepository = cursuRepository;
    }

    public LiveData<List<Cursu>> getCursustLiveData(Context context, ProgressBar progressBar) {
        if (cursuListMutableLiveData == null) {
            cursuListMutableLiveData = new MutableLiveData<>();
            progressBar.setVisibility(View.VISIBLE);
            getCursus(context);
        }
        return cursuListMutableLiveData;
    }

    public LiveData<EventObserver<HttpStatus>> getHttpSatusCursu() {
        if (httpStatusMutableLiveDataCursu == null)
            httpStatusMutableLiveDataCursu = new MutableLiveData<>();
        return httpStatusMutableLiveDataCursu;
    }

    public LiveData<EventObserver<HttpException>> getHttpExceptionCursu() {
        if (httpExceptionMutableLiveDataCursu == null)
            httpExceptionMutableLiveDataCursu = new MutableLiveData<>();
        return httpExceptionMutableLiveDataCursu;
    }

    public void getCursus(Context context) {
        cursuRepository.getCursus(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Cursu>> call, @NonNull Response<List<Cursu>> response) {
                if (response.isSuccessful())
                    cursuListMutableLiveData.setValue(response.body());
                else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveDataCursu.postValue(new EventObserver<>(httpStatus));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cursu>> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveDataCursu.postValue(new EventObserver<>(httpException));
            }
        });
    }

}
