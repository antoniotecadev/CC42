package com.antonioteca.cc42.ui.meal;

import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.loadMeals;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MealFragment extends Fragment {

    private User user;
    private Context context;
    private FragmentMealBinding binding;

    private MealAdapter mealAdapter;
    private FirebaseDatabase firebaseDatabase;
    private List<Meal> mealList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
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
        binding.recyclerViewMeal.setHasFixedSize(true);
        binding.recyclerViewMeal.setLayoutManager(new LinearLayoutManager(context));

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.btnCreateMeal.setBackgroundColor(color);
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
        }
        binding.btnCreateMeal.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_nav_meal_to_dialogFragmentCreateMeal)
        );
        mealAdapter = new MealAdapter(context, mealList);
        binding.recyclerViewMeal.setAdapter(mealAdapter);
        loadMeals(firebaseDatabase, getLayoutInflater(), binding.progressBarMeal, context, String.valueOf(user.getCampusId()), mealList, mealAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}