package com.antonioteca.cc42.ui.meal;

import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.setupVisibility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Cursu;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MealListFragment extends Fragment {


    private User user;
    private Context context;
    private MealAdapter mealAdapter;
    private MealViewModel mealViewModel;
    private FragmentMealBinding binding;
    private FirebaseDatabase firebaseDatabase;

    private DatabaseReference mealsRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MealListFragmentArgs args = MealListFragmentArgs.fromBundle(getArguments());
        Cursu cursu = args.getCursu();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(String.valueOf(cursu.getName()));
        }

        mealsRef = firebaseDatabase.getReference("campus").child(String.valueOf(user.getCampusId()))
                .child("cursus")
                .child(String.valueOf(cursu.getId()))
                .child("meals");

        binding.recyclerViewMeal.setHasFixedSize(true);
        binding.recyclerViewMeal.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.VISIBLE);
            mealViewModel.loadMeals(mealsRef, binding, context);
        });

        mealAdapter = new MealAdapter(context,
                new ArrayList<>(),
                firebaseDatabase,
                getLayoutInflater(),
                user.getCampusId(),
                cursu.getId());
        binding.recyclerViewMeal.setAdapter(mealAdapter);

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.btnCreateMeal.setBackgroundColor(color);
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
        }
        binding.btnCreateMeal.setOnClickListener(v -> {
            MealListFragmentDirections.ActionNavMealToDialogFragmentCreateMeal actionNavMealToDialogFragmentCreateMeal =
                    MealListFragmentDirections.actionNavMealToDialogFragmentCreateMeal(true, cursu.getId());
            Navigation.findNavController(v).navigate(actionNavMealToDialogFragmentCreateMeal);
        });
        mealViewModel.getMealList(mealsRef, binding, context).observe(getViewLifecycleOwner(), meals -> {
            if (!meals.isEmpty() && meals.get(0) != null)
                mealAdapter.updateMealList(meals);
            else
                setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}