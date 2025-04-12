package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int cursusId;
    private String mealId;
    private int rating = 0;

    private Loading loading;
    private Context context;
    private int numberOfRatings = 0;
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
        reduceStarSize(binding.starRatingDone, 30, 30);
        NavController navController = Navigation.findNavController(view);
        DetailsMealFragmentArgs args = DetailsMealFragmentArgs.fromBundle(requireArguments());
        Meal meal = args.getDetailsMeal();
        mealId = meal.getId();
        cursusId = args.getCursusId();
        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), mealId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            String averageRating = (String) ratingValues.get(1); // média da avaliação total sem ser arrendodando ex: 4.5
                            HashMap<?, ?> ratingCounts = (HashMap<?, ?>) ratingValues.get(2); // Total de avaliação para cada estrela
                            numberOfRatings = (int) ratingValues.get(3); // Total de números de avaliações geral de uma refeição

                            // ratingValues.get(0): média da avaliação total arrendodando ex: 5
                            fillStars(binding.starRatingDone, (int) ratingValues.get(0), averageRating, false);

                            List<RatingProgressItem> ratingProgressItems = new ArrayList<>();
                            for (int i = 1; i <= ratingCounts.size(); i++) { // i: estrela
                                int ratingCount = (int) ratingCounts.get(i); // Total de avaliação para estrela
                                int percentage = (ratingCount * 100 / numberOfRatings);
                                ratingProgressItems.add(new RatingProgressItem(ratingCount, percentage));
                            }
                            RatingProgressAdapter adapter = new RatingProgressAdapter(ratingProgressItems);
                            binding.recyclerViewRating.setLayoutManager(new LinearLayoutManager(context));
                            binding.recyclerViewRating.setAdapter(adapter);
                            binding.numberOfRatings.setText(numberOfRatings + " Ratings");
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
    private void fillStars(StarRatingBinding starRatingBinding, int selectedRating, String ratingAverage, boolean isOnClick) {
        if (rating != selectedRating && !loading.isLoading) {
            rating = selectedRating;
            if (isOnClick) {
                resetStars(); // Reseta todas as estrelas
                loading.isLoading = true;
                binding.progressBarMeal.setVisibility(View.VISIBLE);
            }
            // Preenche as estrelas com base na avaliação selecionada
            if (selectedRating >= 1)
                starRatingBinding.star1.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 2)
                starRatingBinding.star2.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 3)
                starRatingBinding.star3.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 4)
                starRatingBinding.star4.setImageResource(R.drawable.baseline_filled_star_40);
            if (selectedRating >= 5)
                starRatingBinding.star5.setImageResource(R.drawable.baseline_filled_star_40);
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
            } else {
                starHalf(starRatingBinding, ratingAverage, selectedRating/*ratingAverageRounded*/);
            }
        }
    }

    private void starHalf(StarRatingBinding starRatingBinding, String ratingAverage, int ratingAverageRounded) {
        double average = Double.parseDouble(ratingAverage);

        // Preenche parcialmente a próxima estrela (opcional)
        double result = Math.abs(average - ratingAverageRounded);
        if (result > 0.0) {
            ImageView nextStar = null;
            if (ratingAverageRounded == 1) nextStar = starRatingBinding.star1;
            else if (ratingAverageRounded == 2) nextStar = starRatingBinding.star2;
            else if (ratingAverageRounded == 3) nextStar = starRatingBinding.star3;
            else if (ratingAverageRounded == 4) nextStar = starRatingBinding.star4;
            else if (ratingAverageRounded == 5) nextStar = starRatingBinding.star5;

            if (nextStar != null) {
                // Define uma imagem de estrela parcialmente preenchida (se disponível)
                nextStar.setImageResource(R.drawable.baseline_star_half_40); // Exemplo de ícone de meia estrela
            }
        }
    }

    private void resetStars() {
        binding.starRating.star1.setImageResource(R.drawable.baseline_star_border_40);
        binding.starRating.star2.setImageResource(R.drawable.baseline_star_border_40);
        binding.starRating.star3.setImageResource(R.drawable.baseline_star_border_40);
        binding.starRating.star4.setImageResource(R.drawable.baseline_star_border_40);
        binding.starRating.star5.setImageResource(R.drawable.baseline_star_border_40);
    }

    public void reduceStarSize(StarRatingBinding starRatingBinding, int newWidth, int newHeight) {
        ImageView[] stars = new ImageView[]{
                starRatingBinding.star1,
                starRatingBinding.star2,
                starRatingBinding.star3,
                starRatingBinding.star4,
                starRatingBinding.star5
        };

        for (ImageView star : stars) {
            ViewGroup.LayoutParams params = star.getLayoutParams();
            params.width = dpToPx(newWidth, context);
            params.height = dpToPx(newHeight, context);
            star.setLayoutParams(params);
        }
    }

    int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}