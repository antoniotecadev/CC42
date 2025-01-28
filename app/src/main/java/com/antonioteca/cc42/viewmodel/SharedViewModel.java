package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> markedAttendanceMutableLiveData = new MutableLiveData<>();

    public LiveData<Boolean> disabledRecyclerView() {
        return disabledMutableLiveData;
    }

    public LiveData<Long> markedAttendanceUser() {
        return markedAttendanceMutableLiveData;
    }

    public void setDisabledRecyclerView(boolean disabledRecyclerView) {
        disabledMutableLiveData.setValue(disabledRecyclerView);
    }

    public void setMarkAttendanceUser(Long userId) {
        markedAttendanceMutableLiveData.setValue(userId);
    }
}
