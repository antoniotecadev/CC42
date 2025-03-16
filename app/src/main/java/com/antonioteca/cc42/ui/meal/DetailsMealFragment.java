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
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int cursusId;
    private String mealId;
    private int rating = 0;
    private Loading loading;
    private Context context;
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
        NavController navController = Navigation.findNavController(view);
        DetailsMealFragmentArgs args = DetailsMealFragmentArgs.fromBundle(requireArguments());
        Meal meal = args.getDetailsMeal();
        mealId = meal.getId();
        cursusId = args.getCursusId();
        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), mealId)
                .observe(getViewLifecycleOwner(), ratingValues -> fillStars((int) ratingValues.get(0), false));
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
        binding.textViewDate.setText(meal.getCreatedDate());
        MealsUtils.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal, true);
        // Configura os cliques das estrelas
        binding.star1.setOnClickListener(v -> fillStars(1, true));
        binding.star2.setOnClickListener(v -> fillStars(2, true));
        binding.star3.setOnClickListener(v -> fillStars(3, true));
        binding.star4.setOnClickListener(v -> fillStars(4, true));
        binding.star5.setOnClickListener(v -> fillStars(5, true));

        binding.fabGenerateQrCode.setOnClickListener(v -> {
            try {
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToQrCodeFragment actionDetailsMealFragmentToQrCodeFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToQrCodeFragment("meal" + meal.getId(), meal.getName(), "description");
                navController.navigate(actionDetailsMealFragmentToQrCodeFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabOpenSubscriptionList.setOnClickListener(v -> {
            try {
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToSubscriptionListFragment actionDetailsMealFragmentToSubscriptionListFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToSubscriptionListFragment(meal, cursusId);
                navController.navigate(actionDetailsMealFragmentToSubscriptionListFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para lidar com o clique nas estrelas
    private void fillStars(int selectedRating, boolean isOnClick) {
        if (rating != selectedRating && !loading.isLoading) {
            rating = selectedRating;
            if (isOnClick) {
                resetStars(); // Reseta todas as estrelas
                loading.isLoading = true;
                binding.progressBarMeal.setVisibility(View.VISIBLE);
            }
            // Preenche as estrelas com base na avaliação selecionada
            if (selectedRating >= 1)
                binding.star1.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 2)
                binding.star2.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 3)
                binding.star3.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 4)
                binding.star4.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 5)
                binding.star5.setImageResource(R.drawable.baseline_filled_star_40);
            if (isOnClick) {
                mealViewModel.rateMeal(
                        context,
                        firebaseDatabase,
                        loading,
                        binding.progressBarMeal,
                        String.valueOf(user.getCampusId()),
                        String.valueOf(cursusId), mealId,
                        String.valueOf(user.getUid()),
                        selectedRating);
            }
        }
    }

    private void resetStars() {
        binding.star1.setImageResource(R.drawable.baseline_border_star_40);
        binding.star2.setImageResource(R.drawable.baseline_border_star_40);
        binding.star3.setImageResource(R.drawable.baseline_border_star_40);
        binding.star4.setImageResource(R.drawable.baseline_border_star_40);
        binding.star5.setImageResource(R.drawable.baseline_border_star_40);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}