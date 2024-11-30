package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.EventObserver;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventViewModel extends ViewModel {

    private final EventRepository eventRepository;

    private MutableLiveData<List<Event>> eventMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveData;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveData;

    public EventViewModel(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public LiveData<List<Event>> getEventsList(Context context, ProgressBar progressBar) {
        if (eventMutableLiveData == null) {
            eventMutableLiveData = new MutableLiveData<>();
            progressBar.setVisibility(View.VISIBLE);
            getEvents(context);
        }
        return eventMutableLiveData;
    }

    public LiveData<EventObserver<HttpStatus>> getHttpSatus() {
        if (httpStatusMutableLiveData == null)
            httpStatusMutableLiveData = new MutableLiveData<>();
        return httpStatusMutableLiveData;
    }

    public LiveData<EventObserver<HttpException>> getHttpException() {
        if (httpExceptionMutableLiveData == null)
            httpExceptionMutableLiveData = new MutableLiveData<>();
        return httpExceptionMutableLiveData;
    }

    public void getEvents(Context context) {
        eventRepository.getEvents(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.isSuccessful())
                    eventMutableLiveData.postValue(response.body());
                else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveData.postValue(new EventObserver<>(httpStatus));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveData.postValue(new EventObserver<>(httpException));
            }
        });
    }
}
