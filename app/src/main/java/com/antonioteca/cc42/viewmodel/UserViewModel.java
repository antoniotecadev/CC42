package com.antonioteca.cc42.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.UserRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A camada que interage com o Repositório para buscar dados e preparar essas informações para a View.
 * O ViewModel observa os dados e fornece-os para a View.
 */

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;

    private MutableLiveData<User> userMutableLiveData;
    private MutableLiveData<List<User>> userListMutableLiveData;
    private MutableLiveData<HttpStatus> httpStatusMutableLiveData;
    private MutableLiveData<HttpException> httpExceptionMutableLiveData;

    public UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public LiveData<User> getUser() {
        if (userMutableLiveData == null)
            userMutableLiveData = new MutableLiveData<>();
        return userMutableLiveData;
    }

    public LiveData<List<User>> getUsersEventLiveData(long eventId, Context context) {
        if (userListMutableLiveData == null) {
            userListMutableLiveData = new MutableLiveData<>();
            getUsersEvent(eventId, context);
        }
        return userListMutableLiveData;
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

    public boolean saveUser(User user) {
        return userRepository.saveUser(user);
    }

    public void getUser(Context context) {
        userRepository.getUser(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    userRepository.getCoalition(user.uid, new Callback<List<Coalition>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Coalition>> call, @NonNull Response<List<Coalition>> response) {
                            List<Coalition> coalitions = response.body();
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

    public void getUsersEvent(long eventId, Context context) {
        userRepository.getUsersEvent(eventId, new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful())
                    userListMutableLiveData.postValue(response.body());
                else {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    httpStatusMutableLiveData.postValue(httpStatus);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                httpExceptionMutableLiveData.postValue(httpException);
            }
        });
    }
}
