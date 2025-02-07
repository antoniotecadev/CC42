package com.antonioteca.cc42.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.repository.CursuRepository;
import com.antonioteca.cc42.viewmodel.CursuViewModel;

public class CursuViewModelFactory implements ViewModelProvider.Factory {
    private final CursuRepository cursuRepository;

    public CursuViewModelFactory(CursuRepository cursuRepository) {
        this.cursuRepository = cursuRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CursuViewModel.class)) {
            return (T) new CursuViewModel(cursuRepository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
