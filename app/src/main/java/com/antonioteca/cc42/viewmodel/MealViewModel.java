package com.antonioteca.cc42.viewmodel;

import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.setupVisibility;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MealViewModel extends ViewModel {

    private DatabaseReference mealsRef;
    private ValueEventListener valueEventListener;

    private MutableLiveData<List<Meal>> mealListMutableLiveData;

    public LiveData<List<Meal>> getMealList(DatabaseReference mealsRef, FragmentMealBinding binding, Context context) {
        if (mealListMutableLiveData == null) {
            mealListMutableLiveData = new MutableLiveData<>();
            binding.progressBarMeal.setVisibility(View.VISIBLE);
            loadMeals(mealsRef, binding, context);
        }
        return mealListMutableLiveData;
    }

    public void loadMeals(DatabaseReference mealsRef, FragmentMealBinding binding, Context context) {
        this.mealsRef = mealsRef;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Meal> mealList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Meal meal = dataSnapshot.getValue(Meal.class);
                        mealList.add(meal);
                    }
                    Collections.reverse(mealList);
                    mealListMutableLiveData.setValue(mealList);
                    setupVisibility(binding, View.INVISIBLE, false, View.INVISIBLE, View.VISIBLE);
                } else {
                    String message = context.getString(R.string.meals_not_found);
                    Util.showAlertDialogBuild(context.getString(R.string.warning), message, context, () -> {
                        setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                        loadMeals(mealsRef, binding, context);
                    });
                    mealListMutableLiveData.setValue(mealList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
                String message = context.getString(R.string.error_load_data) + ": " + error.getMessage();
                Util.showAlertDialogBuild(context.getString(R.string.err), message, context, () -> {
                    setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                    loadMeals(mealsRef, binding, context);
                });
            }
        };
        mealsRef.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mealsRef != null && valueEventListener != null)
            mealsRef.removeEventListener(valueEventListener);
    }
}