package com.antonioteca.cc42.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.ui.meal.RatingProgressAdapter;
import com.antonioteca.cc42.ui.meal.RatingProgressItem;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class StarUtils {

    public static void starHalf(StarRatingBinding starRatingBinding, double ratingAverage, int ratingAverageRounded) {
        // Preenche parcialmente a próxima estrela (opcional)
        double result = Math.abs(ratingAverage - ratingAverageRounded);
        if (result > 0.0) {
            ImageView nextStar = null;
            boolean isEquals = (int) ratingAverage == ratingAverageRounded;
            if (isEquals)
                ratingAverageRounded += 1;
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

    public static void resetStars(StarRatingBinding starRatingBinding) {
        starRatingBinding.star1.setImageResource(R.drawable.baseline_star_border_40);
        starRatingBinding.star2.setImageResource(R.drawable.baseline_star_border_40);
        starRatingBinding.star3.setImageResource(R.drawable.baseline_star_border_40);
        starRatingBinding.star4.setImageResource(R.drawable.baseline_star_border_40);
        starRatingBinding.star5.setImageResource(R.drawable.baseline_star_border_40);
    }

    public static void setColorCoalitionStar(StarRatingBinding starRatingBinding, User user) {
        int color;
        String colorCoalition = user.coalition.getColor();
        color = Color.parseColor(Objects.requireNonNullElse(colorCoalition, "#FF039BE5"));
        starRatingBinding.star1.setColorFilter(color);
        starRatingBinding.star2.setColorFilter(color);
        starRatingBinding.star3.setColorFilter(color);
        starRatingBinding.star4.setColorFilter(color);
        starRatingBinding.star5.setColorFilter(color);
    }

    public static void reduceStarSize(Context context, StarRatingBinding starRatingBinding, int newWidth, int newHeight) {
        ImageView[] stars = new ImageView[]{
                starRatingBinding.star1,
                starRatingBinding.star2,
                starRatingBinding.star3,
                starRatingBinding.star4,
                starRatingBinding.star5
        };

        for (ImageView star : stars) {
            Util.setWidthHeightImageView(context, newWidth, newHeight, star);
        }
    }

    public static void selectedRating(StarRatingBinding starRatingBinding, int selectedRating) {
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
    }

    // Método para lidar com o clique nas estrelas
    public static int fillStars(StarRatingBinding starRatingBinding,
                                int selectedRating,
                                Double ratingAverage,
                                boolean isOnClick,
                                Context context,
                                Loading loading,
                                long userId,
                                int campusId,
                                int cursusId,
                                String type,
                                String typeId,
                                int rating,
                                FirebaseDatabase firebaseDatabase,
                                ProgressBar progressBar,
                                MealViewModel mealViewModel) {
        if (rating != selectedRating && !loading.isLoading) {
            if (ratingAverage == null)
                rating = selectedRating;
            if (isOnClick) {
                StarUtils.resetStars(starRatingBinding); // Reseta todas as estrelas
                loading.isLoading = true;
                progressBar.setVisibility(View.VISIBLE);
            }
            StarUtils.selectedRating(starRatingBinding, selectedRating);
            if (isOnClick) {
                mealViewModel.rate(
                        context,
                        firebaseDatabase,
                        loading,
                        progressBar,
                        String.valueOf(campusId),
                        String.valueOf(cursusId),
                        type,
                        typeId,
                        String.valueOf(userId),
                        selectedRating
                );
            } else if (ratingAverage != null) {
                StarUtils.starHalf(starRatingBinding, ratingAverage, selectedRating/*ratingAverageRounded*/);
            }
        }
        return (rating);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    public static HashMap<?, ?> getRate(
            Context context,
            long userId,
            String userLogin,
            @NonNull List<Object> ratingValues,
            StarRatingBinding starRatingDone,
            StarRatingBinding starRating,
            TextView textViewTapToRate,
            TextView textViewNumberOfRatings,
            TextView textViewAverageRating,
            RecyclerView recyclerViewRating,
            Loading loading,
            int campusId,
            int cursusId,
            String type,
            String typeId,
            int rating,
            FirebaseDatabase firebaseDatabase,
            ProgressBar progressBar,
            MealViewModel mealViewModel
    ) {
        String averageRating = ratingValues.get(1).toString().replace(",", "."); // média da avaliação total sem ser arrendodando ex: 4.5
        HashMap<?, ?> ratingCounts = (HashMap<?, ?>) ratingValues.get(2); // Total de avaliação para cada estrela
        int numberOfRatings = (int) ratingValues.get(3); // Total de números de avaliações geral de uma refeição
        HashMap<?, ?> ratingValuesUsers = (HashMap<?, ?>) ratingValues.get(4); // Avaliações de cada usuário
        Integer ratingValueUser = (Integer) ratingValuesUsers.get(String.valueOf(userId)); // Avaliação do usuário actual

        // ratingValues.get(0): média da avaliação total arrendodando ex: 5
        fillStars(starRatingDone, (int) ratingValues.get(0), Double.valueOf(averageRating), false, context, loading, userId, campusId, cursusId, type, typeId, rating, firebaseDatabase, progressBar, mealViewModel);
        if (ratingValueUser != null) {
            textViewTapToRate.setTextColor(context.getResources().getColor(R.color.green));
            fillStars(starRating,
                    ratingValueUser,
                    null,
                    false,
                    context,
                    loading,
                    userId,
                    campusId,
                    cursusId,
                    type,
                    typeId,
                    rating,
                    firebaseDatabase,
                    progressBar,
                    mealViewModel);
            textViewTapToRate.setText(userLogin);
            starRating.star1.setClickable(false);
            starRating.star2.setClickable(false);
            starRating.star3.setClickable(false);
            starRating.star4.setClickable(false);
            starRating.star5.setClickable(false);
        }

        List<RatingProgressItem> ratingProgressItems = new ArrayList<>();
        for (int i = 1; i <= ratingCounts.size(); i++) { // i: estrela
            int ratingCount = (int) ratingCounts.get(i); // Total de avaliação para estrela
            int percentage = (ratingCount * 100 / numberOfRatings);
            ratingProgressItems.add(new RatingProgressItem(ratingCount, percentage));
        }
        RatingProgressAdapter adapter = new RatingProgressAdapter(ratingProgressItems);
        recyclerViewRating.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewRating.setAdapter(adapter);
        textViewNumberOfRatings.setText(numberOfRatings + " " + context.getString(R.string.ratings));
        textViewAverageRating.setText(averageRating);
        return ratingValuesUsers;
    }
}