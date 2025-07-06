package com.antonioteca.cc42.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.TokenRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenViewModel extends ViewModel {

    private MutableLiveData<HttpStatus> httpStatusLiveData;
    private MutableLiveData<HttpException> httpExceptionMutableLiveData;

    private final TokenRepository tokenRepository;

    public TokenViewModel(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public LiveData<HttpStatus> getHttpSatus() {
        if (httpStatusLiveData == null)
            httpStatusLiveData = new MutableLiveData<>();
        return httpStatusLiveData;
    }

    public LiveData<HttpException> getHttpException() {
        if (httpExceptionMutableLiveData == null)
            httpExceptionMutableLiveData = new MutableLiveData<>();
        return httpExceptionMutableLiveData;
    }

    public void responseToken(Response<Token> response) {
        if (response.isSuccessful()) {
            Token token = response.body();
            if (token != null) {
                if (tokenRepository.saveAcessToken(token))
                    httpStatusLiveData.postValue(HttpStatus.OK);
                else
                    httpStatusLiveData.postValue(HttpStatus.NOT_FOUND);
            } else
                httpStatusLiveData.postValue(HttpStatus.NOT_FOUND);
        } else {
            HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
            httpStatusLiveData.postValue(httpStatus);
        }
    }

    private void failure(Throwable throwable, Context context) {
        HttpException httpException = HttpException.handleException(throwable, context);
        httpExceptionMutableLiveData.postValue(httpException);
    }

//    QUANDO O LOGIN FOR NO CLIENTE
//    public void getAccessTokenUser(String code, Context context) {
//
//        tokenRepository.getAccessTokenUser(code, context, new Callback<Token>() {
//            @Override
//            public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
//                responseToken(response);
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<Token> call, @NonNull Throwable throwable) {
//                failure(throwable, context);
//            }
//        });
//    }

    public void getRefreshTokenUser(String refreshToken, Context context) {
        tokenRepository.getRefreshTokenUser(refreshToken, context, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                responseToken(response);
            }

            @Override
            public void onFailure(@NonNull Call<Token> call, @NonNull Throwable throwable) {
                failure(throwable, context);
            }
        });
    }
}