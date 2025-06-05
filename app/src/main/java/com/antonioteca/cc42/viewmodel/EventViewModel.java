package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventViewModel extends ViewModel {

    private final EventRepository eventRepository;
    private MutableLiveData<Boolean> isPresent;
    private MutableLiveData<List<Event>> eventMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveData;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveData;

    public EventViewModel() {
        eventRepository = null;
    }

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

    public LiveData<Boolean> getUserIsPresent(Context context, LayoutInflater layoutInflater, FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, String userId) {
        if (isPresent == null) {
            isPresent = new MutableLiveData<>();
            getParticipantWithMarkedAttendance(context, layoutInflater, firebaseDatabase, campusId, cursusId, eventId, userId);
        }
        return isPresent;
    }

    public void getEvents(Context context) {
        eventRepository.getEvents(new Callback<>() {
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

    private void getParticipantWithMarkedAttendance(Context context, LayoutInflater layoutInflater, FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, String userId) {
        DatabaseReference participantsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean present = false;
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key = dataSnapshot.getKey();
                        if (key != null && key.equals(String.valueOf(userId))) {
                            present = true;
                            break;
                        }
                    }
                }
                isPresent.setValue(present);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isPresent.setValue(false);
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null, null);
            }
        });
    }
}
