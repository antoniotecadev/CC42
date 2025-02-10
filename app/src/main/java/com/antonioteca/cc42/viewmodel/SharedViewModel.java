package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> longMutableLiveData = new MutableLiveData<>();

    public LiveData<Boolean> disabledRecyclerView() {
        return disabledMutableLiveData;
    }

    public LiveData<Long> getUserIdLiveData() {
        return longMutableLiveData;
    }

    public void setDisabledRecyclerView(boolean disabledRecyclerView) {
        disabledMutableLiveData.setValue(disabledRecyclerView);
    }

    public void setUserIdLiveData(Long userId) {
        longMutableLiveData.setValue(userId);
    }
}
