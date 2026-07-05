package com.antonioteca.cc42.viewmodel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daoapi.DaoApiUser;
import com.antonioteca.cc42.model.Location;
import com.antonioteca.cc42.model.LoginResponse;
import com.antonioteca.cc42.model.Subscription;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.network.LocationSaveCallback;
import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.EventObserver;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    //    private CompositeDisposable compositeDisposable;
    private UserRepository userRepository;

    //    private MutableLiveData<List<User>> userList;
    private MutableLiveData<User> userMutableLiveData;
    private MutableLiveData<List<User>> userListMutableLiveData;
    private MutableLiveData<Map<String, Boolean>> userIdsMapListMutableLiveData;
    private MutableLiveData<Map.Entry<Set<String>, Integer>> userIdsAndQuantityListMutableLiveData;
    private MutableLiveData<HttpStatus> httpStatusMutableLiveData;
    private MutableLiveData<HttpException> httpExceptionMutableLiveData;
    private MutableLiveData<EventObserver<HttpStatus>> httpStatusMutableLiveDataEvent;
    private MutableLiveData<EventObserver<HttpException>> httpExceptionMutableLiveDataEvent;

    public UserViewModel(UserRepository userRepository) {
//        this.compositeDisposable = new CompositeDisposable();
        this.userRepository = userRepository;
    }

    public UserViewModel() {
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

    public LiveData<Map<String, Boolean>> getUserIdsMapList() {
        if (userIdsMapListMutableLiveData == null)
            userIdsMapListMutableLiveData = new MutableLiveData<>();
        return userIdsMapListMutableLiveData;
    }

    public LiveData<Map.Entry<Set<String>, Integer>> getUserIdsAndQuantityList() {
        if (userIdsAndQuantityListMutableLiveData == null)
            userIdsAndQuantityListMutableLiveData = new MutableLiveData<>();
        return userIdsAndQuantityListMutableLiveData;
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

    public void fetchAllBlockedUsers(FirebaseDatabase firebaseDatabase, String campusId, String cursusId, Set<Long> accumulatorId, String lastKey, Runnable onFinished) {
        int pageSize = 20;
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("campus").child(campusId)
                .child("cursus").child(cursusId)
                .child("blocked_users");

        // Prepara a query: ordena por chave e limita a 20
        Query query = ref.orderByKey().limitToFirst(pageSize);

        // Se não for a primeira página, começa após a última chave recebida
        if (lastKey != null) {
            query = query.startAfter(lastKey);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    // Não há mais dados, encerra a recursão
                    onFinished.run();
                    return;
                }
                String currentLastKey = null;
                for (DataSnapshot child : snapshot.getChildren()) {
//                    BlockedUser user = child.getValue(BlockedUser.class);
//                    if (user != null) {
//                        accumulator.add(user);
//                    }
                    String key = child.getKey();
                    if (key != null) {
                        accumulatorId.add(Long.valueOf(key));
                        currentLastKey = key;
                    }
                }

                // Se o Firebase retornou o número exato da página (20),
                // existe a possibilidade de haver mais dados.
                if (snapshot.getChildrenCount() == pageSize) {
                    // Chama a função novamente para buscar os próximos 20
                    fetchAllBlockedUsers(firebaseDatabase, cursusId, campusId, accumulatorId, currentLastKey, onFinished);
                } else {
                    // Se veio menos que 20, significa que era a última página
                    onFinished.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Em caso de erro, você decide se para ou processa o que já tem
                onFinished.run();
            }
        });
    }

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

    public void getUsersDataAttendanceList(@NonNull FirebaseDatabase firebaseDatabase, String campusId, String cursusId, String eventId, Context context, LayoutInflater layoutInflater) {
        DatabaseReference participantsRef = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("events")
                .child(eventId)
                .child("participants");  // Referência para os participantes do evento

        Map<String, Boolean> userIdsWithMarkedAttendance = new HashMap<>();
        userIdsWithMarkedAttendance.put("-1", false);
        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        userIdsWithMarkedAttendance.put(dataSnapshot.getKey(), dataSnapshot.child("checkout").getValue(Long.class) != null);
                    }
                }
                userIdsMapListMutableLiveData.postValue(userIdsWithMarkedAttendance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_attendance_event) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null, null);
                userIdsMapListMutableLiveData.postValue(userIdsWithMarkedAttendance);
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

    public void getUsersSubscription(int cursusId, @NonNull Loading l, Context context, Boolean activeParam, String rangeParam) {
        l.isLoading = true;
        userRepository.loadUserSubscriptionPaginated(cursusId, l, activeParam, rangeParam, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Subscription>> call, @NonNull Response<List<Subscription>> response) {
                if (response.isSuccessful()) {
                    List<User> userList = new ArrayList<>();
                    if (response.body() != null) {
                        for (Subscription subscription : response.body()) {
                            User u = subscription.getUsers();
                            if (u != null && u.getKind().equalsIgnoreCase("student")) {
                                u.grade = subscription.grade;
                                userList.add(u);
                            }
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

        Set<String> userIdsSubscription = new HashSet<>();
        userIdsSubscription.add("-1");
        final Integer[] quantityReceived = {0};
        subscriptionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Integer quantityReceivedUser = dataSnapshot.child("quantity").getValue(Integer.class);
                        userIdsSubscription.add(dataSnapshot.getKey());
                        if (quantityReceivedUser == null) continue;
                        quantityReceived[0] += quantityReceivedUser;
                    }
                }
                userIdsAndQuantityListMutableLiveData.postValue(Map.entry(userIdsSubscription, quantityReceived[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = context.getString(R.string.msg_error_check_subscription) + ": " + error.toException();
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), message, "#E53935", null, null);
                userIdsAndQuantityListMutableLiveData.postValue(Map.entry(userIdsSubscription, quantityReceived[0]));
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
                        error = response.errorBody() != null ? response.errorBody().toString() : context.getString(R.string.invalid_response_server);
                        callback.onFailure(call, new Exception(new JSONObject(error).getString("error")));
                    } catch (Exception ignored) {
                    }
                    return;
                }

                LoginResponse loginData = response.body();

                if (loginData.firebaseToken == null || loginData.user == null) {
                    callback.onFailure(call, new Exception(context.getString(R.string.incomplet_response_server)));
                    return;
                }

                FirebaseAuth.getInstance().signInWithCustomToken(loginData.firebaseToken)
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                callback.onFailure(call, new Exception(context.getString(R.string.failed_login_firebase) + task.getException()));
                                return;
                            }
                            TokenRepository token = new TokenRepository(context);
                            if (userRepository.saveUser(loginData.user)) {
                                if (token.saveAcessToken(loginData.token)) {
                                    callback.onResponse(call, response);
                                    return;
                                }
                                callback.onFailure(call, new Exception(context.getString(R.string.failed_save_token)));
                            } else
                                callback.onFailure(call, new Exception(context.getString(R.string.failed_save_user)));
                        });
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.failed_authentication_intra) + t.getMessage(), context, null);
            }
        });
    }

    public void getUserByLogin(Context context, String userLogin) {
        userRepository.getUserByLogin(userLogin, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    userMutableLiveData.postValue(user);
                } else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveData.postValue(httpStatus);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
                Util.showAlertDialogBuild(context.getString(R.string.err), throwable.getMessage(), context, null);
            }
        });
    }

    private static final String TAG = "FirebaseServiceLocation";

    public void saveUserLocation(
            @NonNull String userId,
            @NonNull String campusId,
            @NonNull String cursusId,
            @NonNull Location location,
            @NonNull LocationSaveCallback callback) {

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String path = String.format("campus/%s/cursus/%s/user_locations/%s", campusId, cursusId, userId);
            DatabaseReference userLocationRef = database.getReference(path);

            userLocationRef.setValue(location)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Localização salva com sucesso!");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erro ao salvar localização:", e);
                        callback.onError(e);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar a operação do Firebase:", e);
            callback.onError(e);
        }
    }

    public void getUserLocation(
            @NonNull String userId,
            @NonNull String campusId,
            @NonNull String cursusId,
            @NonNull LocationSaveCallback callback) {

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String path = String.format("campus/%s/cursus/%s/user_locations/%s", campusId, cursusId, userId);
            DatabaseReference userLocationRef = database.getReference(path);

            userLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Location location = snapshot.getValue(Location.class);
                        callback.onComplete(location);
                    } else {
                        // O snapshot não existe, retornamos null como na função original
                        callback.onComplete(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Ocorreu um erro ao buscar os dados
                    Log.e(TAG, "Erro ao buscar localização:", error.toException());
                    callback.onError(error.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar a operação do Firebase:", e);
            callback.onError(e);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
//        if (compositeDisposable.isDisposed())
//            compositeDisposable.dispose();
    }
}