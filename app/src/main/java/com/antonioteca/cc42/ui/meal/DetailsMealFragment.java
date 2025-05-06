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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsMealBinding;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int cursusId;
    private String mealId;
    private int rating = 0;
    private Loading loading;
    private Context context;
    private int numberOfRatings = 0;
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
        mealId = meal.getId();
        cursusId = args.getCursusId();

        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), "meals", mealId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            String averageRating = ratingValues.get(1).toString().replace(",", "."); // média da avaliação total sem ser arrendodando ex: 4.5
                            HashMap<?, ?> ratingCounts = (HashMap<?, ?>) ratingValues.get(2); // Total de avaliação para cada estrela
                            numberOfRatings = (int) ratingValues.get(3); // Total de números de avaliações geral de uma refeição
                            ratingValuesUsers = (HashMap<?, ?>) ratingValues.get(4); // Avaliações de cada usuário
                            //bundle.putSerializable("ratingValuesUsers", ratingValuesUsers);
                            Integer ratingValueUser = (Integer) ratingValuesUsers.get(String.valueOf(user.getUid())); // Avaliação do usuário actual

                            // ratingValues.get(0): média da avaliação total arrendodando ex: 5
                            fillStars(binding.starRatingDone, (int) ratingValues.get(0), Double.valueOf(averageRating), false);
                            if (ratingValueUser != null) {
                                binding.textViewTapToRate.setTextColor(getResources().getColor(R.color.green));
                                fillStars(binding.starRating, ratingValueUser, null, false);
                                binding.textViewTapToRate.setText(user.getLogin());
                                binding.starRating.star1.setClickable(false);
                                binding.starRating.star2.setClickable(false);
                                binding.starRating.star3.setClickable(false);
                                binding.starRating.star4.setClickable(false);
                                binding.starRating.star5.setClickable(false);
                            }

                            List<RatingProgressItem> ratingProgressItems = new ArrayList<>();
                            for (int i = 1; i <= ratingCounts.size(); i++) { // i: estrela
                                int ratingCount = (int) ratingCounts.get(i); // Total de avaliação para estrela
                                int percentage = (ratingCount * 100 / numberOfRatings);
                                ratingProgressItems.add(new RatingProgressItem(ratingCount, percentage));
                            }
                            RatingProgressAdapter adapter = new RatingProgressAdapter(ratingProgressItems);
                            binding.recyclerViewRating.setLayoutManager(new LinearLayoutManager(context));
                            binding.recyclerViewRating.setAdapter(adapter);
                            binding.numberOfRatings.setText(numberOfRatings + " " + getString(R.string.ratings));
                            binding.averageRating.setText(averageRating);
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
        binding.starRating.star1.setOnClickListener(v -> fillStars(binding.starRating, 1, null, true));
        binding.starRating.star2.setOnClickListener(v -> fillStars(binding.starRating, 2, null, true));
        binding.starRating.star3.setOnClickListener(v -> fillStars(binding.starRating, 3, null, true));
        binding.starRating.star4.setOnClickListener(v -> fillStars(binding.starRating, 4, null, true));
        binding.starRating.star5.setOnClickListener(v -> fillStars(binding.starRating, 5, null, true));

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

    // Método para lidar com o clique nas estrelas
    private void fillStars(StarRatingBinding starRatingBinding, int selectedRating, Double ratingAverage, boolean isOnClick) {
        if (rating != selectedRating && !loading.isLoading) {
            if (ratingAverage == null)
                rating = selectedRating;
            if (isOnClick) {
                StarUtils.resetStars(starRatingBinding); // Reseta todas as estrelas
                loading.isLoading = true;
                binding.progressBarMeal.setVisibility(View.VISIBLE);
            }
            StarUtils.selectedRating(starRatingBinding, selectedRating);
            if (isOnClick) {
                mealViewModel.rate(
                        context,
                        firebaseDatabase,
                        loading,
                        binding.progressBarMeal,
                        String.valueOf(user.getCampusId()),
                        String.valueOf(cursusId),
                        mealId,
                        String.valueOf(user.getUid()),
                        selectedRating,
                        "meals");
            } else if (ratingAverage != null) {
                StarUtils.starHalf(starRatingBinding, ratingAverage, selectedRating/*ratingAverageRounded*/);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}