package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.EventObserver;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> newMeal = new MutableLiveData<>();
    private final MutableLiveData<Long> longMutableLiveData = new MutableLiveData<>();

    public void setNewMeal(Meal meal) {
        newMeal.setValue(new EventObserver<>(meal));
    }

    public MutableLiveData<EventObserver<Meal>> getNewMealLiveData() {
        return newMeal;
    }

    public void setDisabledRecyclerView(boolean disabledRecyclerView) {
        disabledMutableLiveData.setValue(disabledRecyclerView);
    }

    public LiveData<Boolean> disabledRecyclerView() {
        return disabledMutableLiveData;
    }

    public LiveData<Long> getUserIdLiveData() {
        return longMutableLiveData;
    }

    public void setUserIdLiveData(Long userId) {
        longMutableLiveData.setValue(userId);
    }
}
