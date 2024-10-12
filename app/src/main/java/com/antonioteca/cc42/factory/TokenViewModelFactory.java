package com.antonioteca.cc42.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.viewmodel.TokenViewModel;

public class TokenViewModelFactory implements ViewModelProvider.Factory {
    private final TokenRepository tokenRepository;

    public TokenViewModelFactory(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TokenViewModel.class)) {
            return (T) new TokenViewModel(tokenRepository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
