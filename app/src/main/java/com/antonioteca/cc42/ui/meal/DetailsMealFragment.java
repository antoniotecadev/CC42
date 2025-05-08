package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int rating = 0;
    private Loading loading;
    private Context context;
    private HashMap<?, ?> ratingValuesUsers;
    private MealViewModel mealViewModel;
    private FirebaseDatabase firebaseDatabase;
    private FragmentDetailsMealBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        loading = new Loading();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsMealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StarUtils.setColorCoalitionStar(binding.starRating, user);
        StarUtils.reduceStarSize(context, binding.starRatingDone, 30, 30);
        NavController navController = Navigation.findNavController(view);
        DetailsMealFragmentArgs args = DetailsMealFragmentArgs.fromBundle(requireArguments());
        Meal meal = args.getDetailsMeal();
        String type = "meals";
        long userId = user.getUid();
        String mealId = meal.getId();
        int campusId = user.getCampusId();
        int cursusId = args.getCursusId();

        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, binding.progressBarMeal, String.valueOf(user.getCampusId()), String.valueOf(cursusId), type, mealId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            if (ratingValues.isEmpty()) {
                                binding.textViewTapToRate.setTextColor(context.getResources().getColor(R.color.red));
                                binding.textViewTapToRate.setText(R.string.text_unsigned);
                            } else
                                ratingValuesUsers = StarUtils.getRate(
                                        context,
                                        userId,
                                        user.getLogin(),
                                        ratingValues,
                                        binding.starRatingDone,
                                        binding.starRating,
                                        binding.textViewTapToRate,
                                        binding.numberOfRatings,
                                        binding.averageRating,
                                        binding.recyclerViewRating,
                                        loading,
                                        campusId,
                                        cursusId,
                                        type,
                                        mealId,
                                        rating,
                                        firebaseDatabase,
                                        binding.progressBarMeal,
                                        mealViewModel);
                            binding.progressBarMeal.setVisibility(View.INVISIBLE);
                        });

        if (cursusId == 0) {
            binding.fabGenerateQrCode.setVisibility(View.GONE);
            binding.fabOpenSubscriptionList.setVisibility(View.GONE);
        }

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(meal.getType() + " (" + meal.getQuantity() + ")");
        }

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
        }

        binding.textViewType.setText(meal.getType());
        binding.textViewName.setText(meal.getName());
        binding.textViewDescription.setText(meal.getDescription());
        binding.textViewDate.setText(meal.getCreatedDate());
        MealsUtils.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal, true);
        // Configura os cliques das estrelas
        binding.starRating.star1.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 1, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star2.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 2, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star3.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 3, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star4.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 4, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star5.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 5, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));

        binding.fabGenerateQrCode.setOnClickListener(v -> {
            try {
                rating = 0; // Para poder mostrar a classificação, ao voltar <-
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToQrCodeFragment actionDetailsMealFragmentToQrCodeFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToQrCodeFragment("meal" + meal.getId() + "#" + user.getUid(), meal.getName(), Objects.requireNonNullElse(meal.getDescription(), ""), user.getCampusId(), cursusId);
                navController.navigate(actionDetailsMealFragmentToQrCodeFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabOpenSubscriptionList.setOnClickListener(v -> {
            try {
                rating = 0; // Para poder mostrar a classificação, ao voltar <-
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToSubscriptionListFragment actionDetailsMealFragmentToSubscriptionListFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToSubscriptionListFragment(meal, cursusId).setRatingValuesUsers(ratingValuesUsers);
                navController.navigate(actionDetailsMealFragmentToSubscriptionListFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}