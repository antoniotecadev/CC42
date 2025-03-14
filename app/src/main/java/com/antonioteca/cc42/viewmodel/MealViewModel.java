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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MealViewModel extends ViewModel {

    private DatabaseReference mealsRef;
    private ValueEventListener valueEventListener;

    private MutableLiveData<List<Meal>> mealListMutableLiveData;

    public LiveData<List<Meal>> getMealList(Context context, FragmentMealBinding binding, DatabaseReference mealsRef, String startAtKey) {
        if (mealListMutableLiveData == null) {
            mealListMutableLiveData = new MutableLiveData<>();
            binding.progressBarMeal.setVisibility(View.VISIBLE);
            loadMeals(context, binding, mealsRef, startAtKey);
        }
        return mealListMutableLiveData;
    }

    public void loadMeals(Context context, FragmentMealBinding binding, DatabaseReference mealsRef, String startAtKey) {
        this.mealsRef = mealsRef;
        Query query = mealsRef.orderByKey();
        if (startAtKey != null) {
            query = query.endBefore(startAtKey).limitToLast(15);
        } else {
            query = query.limitToLast(15);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Meal> mealList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Meal meal = dataSnapshot.getValue(Meal.class);
                        mealList.add(meal);
                    }
                    Collections.reverse(mealList);
                    setupVisibility(binding, View.INVISIBLE, false, View.INVISIBLE, View.VISIBLE);
                } else if (mealListMutableLiveData.getValue() == null) {
                    setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
                    String message = context.getString(R.string.meals_not_found);
                    Util.showAlertDialogBuild(context.getString(R.string.warning), message, context, () -> {
                        setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                        loadMeals(context, binding, mealsRef, startAtKey);
                    });
                }
                mealListMutableLiveData.setValue(mealList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (mealListMutableLiveData.getValue() == null) {
                    String message = context.getString(R.string.error_load_data) + ": " + error.getMessage();
                    Util.showAlertDialogBuild(context.getString(R.string.err), message, context, () -> {
                        setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.INVISIBLE);
                        loadMeals(context, binding, mealsRef, startAtKey);
                    });
                }
                mealListMutableLiveData.setValue(new ArrayList<>());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mealsRef != null && valueEventListener != null)
            mealsRef.removeEventListener(valueEventListener);
    }
}