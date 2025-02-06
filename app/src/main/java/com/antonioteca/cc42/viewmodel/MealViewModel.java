package com.antonioteca.cc42.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.model.Meal;

import java.util.List;

public class MealViewModel extends ViewModel {
    private MutableLiveData<List<Meal>> mealListMutableLiveData;

    public LiveData<List<Meal>> getMealList() {
        if (mealListMutableLiveData == null)
            mealListMutableLiveData = new MutableLiveData<>();
        return mealListMutableLiveData;
    }

    public void setMealList(List<Meal> meals) {
        mealListMutableLiveData.setValue(meals);
    }
}
