package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.EventObserver;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Long> longMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> newMeal = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> updatedMeal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<List<String>>> updatePathImage = new MutableLiveData<>();

    public MutableLiveData<EventObserver<Meal>> getUpdatedMealLiveData() {
        return updatedMeal;
    }

    public void setUpdatedMeal(Meal meal) {
        updatedMeal.setValue(new EventObserver<>(meal));
    }

    public MutableLiveData<EventObserver<Meal>> getNewMealLiveData() {
        return newMeal;
    }

    public void setNewMeal(Meal meal) {
        newMeal.setValue(new EventObserver<>(meal));
    }

    public LiveData<EventObserver<List<String>>> getPathImageLiveData() {
        return updatePathImage;
    }

    public void setUpdatedPathImage(List<String> imageUrl) {
        updatePathImage.setValue(new EventObserver<>(imageUrl));
    }

    public LiveData<Boolean> disabledRecyclerView() {
        return disabledMutableLiveData;
    }

    public void setDisabledRecyclerView(boolean disabledRecyclerView) {
        disabledMutableLiveData.setValue(disabledRecyclerView);
    }

    public LiveData<Long> getUserIdLiveData() {
        return longMutableLiveData;
    }

    public void setUserIdLiveData(Long userId) {
        longMutableLiveData.setValue(userId);
    }
}
