package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daoapi.DaoApiUser;
import com.antonioteca.cc42.model.LoginResponse;
import com.antonioteca.cc42.model.Subscription;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.Util;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A camada que interage com o Repositório para buscar dados e preparar essas informações para a View.
 * O ViewModel observa os dados e fornece-os para a View.
 */

public class UserViewModel extends ViewModel {

    private final CompositeDisposable compositeDisposable;
    private final UserRepository userRepository;

    //    private MutableLiveData<List<User>> userList;
    private MutableLiveData<User> userMutableLiveData;
    private MutableLiveData<List<User>> userListMutableLiveData;
    private MutableLiveData<List<String>> userIdsListMutableLiveData;
    private MutableLiveData<HttpStatus> httpStatusMutableLiveData;
    private MutableLiveData<HttpException> httpExceptionMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveDataEvent;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveDataEvent;

    public UserViewModel(UserRepository userRepository) {
        this.compositeDisposable = new CompositeDisposable();
        this.userRepository = userRepository;
    }

//    public MutableLiveData<List<User>> getUserList() {
//        if (userList == null)
//            userList = new MutableLiveData<>();
//        return userList;
//    }

    public LiveData<User> getUser() {
        if (userMutableLiveData == null)
            userMutableLiveData = new MutableLiveData<>();
        return userMutableLiveData;
    }

    public void getUsersEventLiveData(Context context, long eventId, Loading l, @NonNull ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        getUsersEvent(eventId, l, context);
    }

    public LiveData<List<User>> getUsersEventLiveData() {
        if (userListMutableLiveData == null) {
            userListMutableLiveData = new MutableLiveData<>();
        }
        return userListMutableLiveData;
    }

    public LiveData<List<User>> getUsersSubscriptionLiveData() {
        if (userListMutableLiveData == null) {
            userListMutableLiveData = new MutableLiveData<>();
        }
        return userListMutableLiveData;
    }

    public LiveData<List<String>> getUserIdsList() {
        if (userIdsListMutableLiveData == null)
            userIdsListMutableLiveData = new MutableLiveData<>();
        return userIdsListMutableLiveData;
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

    /*public void addUserLocalAttendanceList(
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
                                    sharedViewModel.setUserIdLiveData(user.userId);
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

    public void deleteLocalAttendanceList(int campusId,
                                          int cursusId,
                                          long eventId, Context context, LayoutInflater layoutInflater) {
        compositeDisposable.add(userRepository.deleteLocalAttendanceList(campusId, cursusId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, throwable -> {
                    String message = context.getString(R.string.msg_error_delete_local_attendance_lis) + ": " + throwable.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                }));
    }*/

//    QUANDO LOGAR NO CLIENTE
//    public boolean saveUser(User user) {
//        return userRepository.saveUser(user);
//    }

//    public void getUser(Context context) {
//        userRepository.getUser(new Callback<>() {
//            @Override
//            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
//                if (response.isSuccessful()) {
//                    User user = response.body();
//                    if (user == null)
//                        return;
//                    userRepository.getCoalition(user.uid, new Callback<>() {
//                        @Override
//                        public void onResponse(@NonNull Call<List<Coalition>> call, @NonNull Response<List<Coalition>> response) {
//                            if (response.isSuccessful()) {
//                                List<Coalition> coalitions = response.body();
//                                if (coalitions != null && !coalitions.isEmpty()) {
//                                    Coalition coalition = coalitions.get(0);
//                                    user.setCoalition(coalition);
//                                }
//                            }
//                            userMutableLiveData.postValue(user);
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<List<Coalition>> call, @NonNull Throwable throwable) {
//                            userMutableLiveData.postValue(user);
//                        }
//                    });
//                } else {
//                    new Token(context).clear();
//                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
//                    httpStatusMutableLiveData.postValue(httpStatus);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
//                new Token(context).clear();
//                HttpException httpException = HttpException.handleException(throwable, context);
//                httpExceptionMutableLiveData.postValue(httpException);
//            }
//        });
//    }

    public void getUsersEvent(long eventId, @NonNull Loading l, Context context) {
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

    public void getIdsUsersAttendanceList(@NonNull FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, Context context, LayoutInflater layoutInflater) {
        DatabaseReference participantsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        List<String> userIdsWithMarkedAttendance = new ArrayList<>();
        userIdsWithMarkedAttendance.add("-1");
        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIdsWithMarkedAttendance.add(dataSnapshot.getKey());
//                        Boolean isParticipant = dataSnapshot.getValue(Boolean.class);
//                        if (Boolean.TRUE.equals(isParticipant)) {
//                            userIdsWhoMarkedAttendance.add(dataSnapshot.getKey());
//                        }
                    }
                }
                userIdsListMutableLiveData.postValue(userIdsWithMarkedAttendance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null, null);
                userIdsListMutableLiveData.postValue(userIdsWithMarkedAttendance);
            }
        });
    }

    /*public void synchronizedAttendanceList(UserViewModel userViewModel, FirebaseDatabase firebaseDatabase, int campusId, int cursusId, long eventId, SwipeRefreshLayout swipeRefreshLayout, Context context,
                                           LayoutInflater layoutInflater) {
        compositeDisposable.add(userRepository.geIdsUserLocalAttendanceList(campusId, cursusId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userIdsWithMarkedAttendanceLocal -> getUserIdsWithMarkedAttendance(
                        userViewModel,
                        userIdsWithMarkedAttendanceLocal,
                        firebaseDatabase,
                        String.valueOf(campusId),
                        String.valueOf(cursusId),
                        String.valueOf(eventId),
                        swipeRefreshLayout,
                        context,
                        layoutInflater), throwable -> {
                    String message = context.getString(R.string.msg_error_get_ids_user_local) + ": " + throwable.getMessage();
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                    userIdsListMutableLiveData.postValue(new ArrayList<>());
                }));
    }

    private void getUserIdsWithMarkedAttendance(UserViewModel userViewModel, List<Long> userIdsWhoMarkedAttendanceLocal, FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, SwipeRefreshLayout swipeRefreshLayout, Context context,
                                                LayoutInflater layoutInflater) {
        DatabaseReference participantsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        List<String> userIdsWithMarkedAttendance = new ArrayList<>();
        userIdsWithMarkedAttendance.add("-1");
        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIdsWithMarkedAttendance.add(dataSnapshot.getKey());
//                        Boolean isParticipant = dataSnapshot.getValue(Boolean.class);
//                        if (Boolean.TRUE.equals(isParticipant)) {
//                            userIdsWhoMarkedAttendance.add(dataSnapshot.getKey());
//                        }
                    }
                    for (Long userIdLocal : userIdsWhoMarkedAttendanceLocal) {
                        if (!userIdsWithMarkedAttendance.contains(String.valueOf(userIdLocal))) {
                            Util.showAlertDialogSynchronized(context, () -> sinchronizationAttendanceList(
                                    userViewModel, userIdsListMutableLiveData,
                                    userIdsWhoMarkedAttendanceLocal,
                                    userIdsWithMarkedAttendance,
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
                                userViewModel, userIdsListMutableLiveData,
                                userIdsWhoMarkedAttendanceLocal,
                                userIdsWithMarkedAttendance,
                                firebaseDatabase,
                                campusId,
                                cursusId,
                                eventId,
                                swipeRefreshLayout,
                                context,
                                layoutInflater));
                    }
                }
                userIdsListMutableLiveData.postValue(userIdsWithMarkedAttendance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null);
                userIdsListMutableLiveData.postValue(userIdsWithMarkedAttendance);
            }
        });
    }*/

    public void getUsersSubscription(int cursusId, @NonNull Loading l, Context context) {
        l.isLoading = true;
        userRepository.loadUserSubscriptionPaginated(cursusId, l, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Subscription>> call, @NonNull Response<List<Subscription>> response) {
                if (response.isSuccessful()) {
                    List<User> userList = new ArrayList<>();
                    if (response.body() != null) {
                        for (Subscription subscription : response.body()) {
                            if (subscription.getUsers().getKind().equalsIgnoreCase("student"))
                                userList.add(subscription.getUsers());
                        }
                    }
                    userListMutableLiveData.postValue(userList);
                } else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveDataEvent.postValue(new EventObserver<>(httpStatus));
                }
                l.isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<List<Subscription>> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveDataEvent.postValue(new EventObserver<>(httpException));
                l.isLoading = false;
            }
        });
    }

    public void getUserIdsSubscriptionList(@NonNull FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String mealId, Context context, LayoutInflater layoutInflater) {
        DatabaseReference subscriptionsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("meals")
                .child(mealId)
                .child("subscriptions"); // Referência para os participantes do evento

        List<String> userIdsSubscription = new ArrayList<>();
        userIdsSubscription.add("-1");
        subscriptionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIdsSubscription.add(dataSnapshot.getKey());
                    }
                }
                userIdsListMutableLiveData.postValue(userIdsSubscription);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_subscription) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null, null);
                userIdsListMutableLiveData.postValue(userIdsSubscription);
            }
        });
    }

    public void loginWithIntra42Code(String code, String redirectUri, Context context, Callback<LoginResponse> callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://check-cadet.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DaoApiUser daoApiUser = retrofit.create(DaoApiUser.class);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("redirectUri", redirectUri);

        daoApiUser.loginWithIntra42Code(requestBody).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String error;
                    try {
                        error = response.errorBody() != null ? response.errorBody().string() : "Resposta inválida do servidor.";
                        callback.onFailure(call, new Exception(new JSONObject(error).getString("error")));
                    } catch (Exception ignored) {
                    }
                    return;
                }

                LoginResponse loginData = response.body();

                if (loginData.firebaseToken == null || loginData.user == null) {
                    callback.onFailure(call, new Exception("Resposta incompleta do servidor."));
                    return;
                }

                FirebaseAuth.getInstance().signInWithCustomToken(loginData.firebaseToken)
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Util.showAlertDialogBuild(context.getString(R.string.err), "Falha ao logar no Firebase.", context, null);
                                callback.onFailure(call, new Exception("Falha ao logar no Firebase."));
                                return;
                            }
                            TokenRepository token = new TokenRepository(context);
                            if (userRepository.saveUser(loginData.user)) {
                                if (token.saveAcessToken(loginData.token)) {
                                    callback.onResponse(call, response);
                                    return;
                                }
                                callback.onFailure(call, new Exception("Falha ao salvar token."));
                            } else
                                callback.onFailure(call, new Exception("Falha ao salvar usuário."));
                        });
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e("Login", "Erro ao logar com Intra42: " + t.getMessage());
                Util.showAlertDialogBuild(context.getString(R.string.err), "Falha ao autenticar com Intra 42.", context, null);
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