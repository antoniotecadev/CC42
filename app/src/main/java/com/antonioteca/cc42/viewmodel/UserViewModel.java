package com.antonioteca.cc42.viewmodel;

import static com.antonioteca.cc42.dao.daofarebase.DaoEventFirebase.sinchronizationAttendanceList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.LocalAttendanceList;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A camada que interage com o Repositório para buscar dados e preparar essas informações para a View.
 * O ViewModel observa os dados e fornece-os para a View.
 */

public class UserViewModel extends ViewModel {

    private final CompositeDisposable compositeDisposable;
    private final UserRepository userRepository;

    private MutableLiveData<User> userMutableLiveData;
    private MutableLiveData<List<User>> userListMutableLiveData;
    private MutableLiveData<List<String>> userIdsWhoMarkedAttendanceMutableLiveData;
    private MutableLiveData<HttpStatus> httpStatusMutableLiveData;
    private MutableLiveData<HttpException> httpExceptionMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveDataEvent;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveDataEvent;

    public UserViewModel(UserRepository userRepository) {
        compositeDisposable = new CompositeDisposable();
        this.userRepository = userRepository;
    }


    public LiveData<User> getUser() {
        if (userMutableLiveData == null)
            userMutableLiveData = new MutableLiveData<>();
        return userMutableLiveData;
    }

    public LiveData<List<User>> getUsersEventLiveData(long eventId, Loading l, ProgressBar progressBar, Context context) {
        if (userListMutableLiveData == null) {
            userListMutableLiveData = new MutableLiveData<>();
            progressBar.setVisibility(View.VISIBLE);
            getUsersEvent(eventId, l, context);
        }
        return userListMutableLiveData;
    }

    public LiveData<List<String>> getUserIdsWhoMarkedPresence() {
        if (userIdsWhoMarkedAttendanceMutableLiveData == null)
            userIdsWhoMarkedAttendanceMutableLiveData = new MutableLiveData<>();
        return userIdsWhoMarkedAttendanceMutableLiveData;
    }

    public LiveData<HttpStatus> getHttpSatus() {
        if (httpStatusMutableLiveData == null)
            httpStatusMutableLiveData = new MutableLiveData<>();
        return httpStatusMutableLiveData;
    }

    public LiveData<HttpException> getHttpException() {
        if (httpExceptionMutableLiveData == null)
            httpExceptionMutableLiveData = new MutableLiveData<>();
        return httpExceptionMutableLiveData;
    }

    public LiveData<EventObserver<HttpStatus>> getHttpSatusEvent() {
        if (httpStatusMutableLiveDataEvent == null)
            httpStatusMutableLiveDataEvent = new MutableLiveData<>();
        return httpStatusMutableLiveDataEvent;
    }

    public LiveData<EventObserver<HttpException>> getHttpExceptionEvent() {
        if (httpExceptionMutableLiveDataEvent == null)
            httpExceptionMutableLiveDataEvent = new MutableLiveData<>();
        return httpExceptionMutableLiveDataEvent;
    }

    public void addUserLocalAttendanceList(
            LocalAttendanceList user,
            Context context,
            LayoutInflater layoutInflater,
            SharedViewModel sharedViewModel,
            Runnable runnableResumeCamera
    ) {
        compositeDisposable.add(userRepository.userAlreadyLocalAttendanceList(user.campusId, user.cursusId, user.eventId, user.userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(attendanceListList -> {
                    if (attendanceListList.isEmpty()) {
                        compositeDisposable.add(userRepository.insert(user)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    Util.startVibration(context);
                                    sharedViewModel.setMarkAttendanceUser(user.userId);
                                    String message = user.displayName + "\n" + context.getString(R.string.msg_sucess_mark_attendance_event);
                                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.sucess), message, "#4CAF50", runnableResumeCamera);
                                }, throwable -> {
                                    String message = context.getString(R.string.msg_error_mark_attendance_event) + ": " + throwable.getMessage();
                                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", runnableResumeCamera);
                                }));
                    } else {
                        String message = user.displayName + "\n" + context.getString(R.string.msg_you_already_mark_attendance_event);
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", runnableResumeCamera);
                    }
                }, throwable -> {
                    String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + throwable.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", runnableResumeCamera);
                }));
    }

    public boolean saveUser(User user) {
        return userRepository.saveUser(user);
    }

    public void getUser(Context context) {
        userRepository.getUser(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user == null)
                        return;
                    userRepository.getCoalition(user.uid, new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Coalition>> call, @NonNull Response<List<Coalition>> response) {
                            List<Coalition> coalitions = response.body();
                            if (coalitions == null)
                                return;
                            Coalition coalition = coalitions.get(0);
                            user.setCoalition(coalition);
                            userMutableLiveData.postValue(user);
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Coalition>> call, @NonNull Throwable throwable) {
                            userMutableLiveData.postValue(user);
                        }
                    });
                } else {
                    new Token(context).clear();
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveData.postValue(httpStatus);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
                new Token(context).clear();
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveData.postValue(httpException);
            }
        });
    }

    public void getUsersEvent(long eventId, Loading l, Context context) {
        l.isLoading = true;
        userRepository.loadUsersEventPaginated(eventId, l, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    userListMutableLiveData.postValue(response.body());
                } else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveDataEvent.postValue(new EventObserver<>(httpStatus));
                }
                l.isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveDataEvent.postValue(new EventObserver<>(httpException));
                l.isLoading = false;
            }
        });
    }

    public void synchronizedAttendanceList(FirebaseDatabase firebaseDatabase, int campusId, int cursusId, long eventId, SwipeRefreshLayout swipeRefreshLayout, Context context,
                                           LayoutInflater layoutInflater) {
        compositeDisposable.add(userRepository.geIdsUserLocalAttendanceList(campusId, cursusId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userIdsWhoMarkedAttendanceLocal -> getUserIdsWhoMarkedAttendance(
                        userIdsWhoMarkedAttendanceLocal,
                        firebaseDatabase,
                        String.valueOf(campusId),
                        String.valueOf(cursusId),
                        String.valueOf(eventId),
                        swipeRefreshLayout,
                        context,
                        layoutInflater), throwable -> {

                }));
    }

    private void getUserIdsWhoMarkedAttendance(List<Long> userIdsWhoMarkedAttendanceLocal, FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, SwipeRefreshLayout swipeRefreshLayout, Context context,
                                               LayoutInflater layoutInflater) {
        DatabaseReference participantsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        List<String> userIdsWhoMarkedAttendance = new ArrayList<>();
        userIdsWhoMarkedAttendance.add("-1");
        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIdsWhoMarkedAttendance.add(dataSnapshot.getKey());
//                        Boolean isParticipant = dataSnapshot.getValue(Boolean.class);
//                        if (Boolean.TRUE.equals(isParticipant)) {
//                            userIdsWhoMarkedAttendance.add(dataSnapshot.getKey());
//                        }
                    }
                    for (Long userIdLocal : userIdsWhoMarkedAttendanceLocal) {
                        if (!userIdsWhoMarkedAttendance.contains(String.valueOf(userIdLocal))) {
                            Util.showAlertDialogSynchronized(context, () -> sinchronizationAttendanceList(
                                    userIdsWhoMarkedAttendanceMutableLiveData,
                                    userIdsWhoMarkedAttendanceLocal,
                                    userIdsWhoMarkedAttendance,
                                    firebaseDatabase,
                                    campusId,
                                    cursusId,
                                    eventId,
                                    swipeRefreshLayout,
                                    context,
                                    layoutInflater));
                            break;
                        }
                    }
                } else {
                    if (!userIdsWhoMarkedAttendanceLocal.isEmpty()) {
                        Util.showAlertDialogSynchronized(context, () -> sinchronizationAttendanceList(
                                userIdsWhoMarkedAttendanceMutableLiveData,
                                userIdsWhoMarkedAttendanceLocal,
                                userIdsWhoMarkedAttendance,
                                firebaseDatabase,
                                campusId,
                                cursusId,
                                eventId,
                                swipeRefreshLayout,
                                context,
                                layoutInflater));
                    }
                }
                userIdsWhoMarkedAttendanceMutableLiveData.postValue(userIdsWhoMarkedAttendance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                userIdsWhoMarkedAttendanceMutableLiveData.postValue(userIdsWhoMarkedAttendance);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}