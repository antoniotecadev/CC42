package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.EventObserver;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<EventObserver<Long>> longMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<String>> faceIDMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> newMeal = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<Meal>> updatedMeal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> disabledMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventObserver<List<String>>> updatePathImage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> faceIDContinueCaptureMutableLiveData = new MutableLiveData<>();

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

    public LiveData<EventObserver<Long>> getUserIdLiveData() {
        return longMutableLiveData;
    }

    public void setUserIdLiveData(Long userId) {
        longMutableLiveData.setValue(new EventObserver<>(userId));
    }

    public LiveData<EventObserver<String>> getUserFaceIdLiveData() {
        return faceIDMutableLiveData;
    }

    public void setUserFaceIdLiveData(String userId) {
        faceIDMutableLiveData.setValue(new EventObserver<>(userId));
    }

    public LiveData<Boolean> getUserFaceIdContinueCaptureLiveData() {
        return faceIDContinueCaptureMutableLiveData;
    }

    public void setUserFaceIdContinueCaptureLiveData(Boolean continueCaptureFaceID) {
        faceIDContinueCaptureMutableLiveData.setValue(continueCaptureFaceID);
    }
}
