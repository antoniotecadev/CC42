package com.antonioteca.cc42.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

import androidx.preference.PreferenceManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.Calendar;

public class MealsUtils {

    public static String getMealType(Context context) {
        // Lê os horários das refeições
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String breakfastStart = preferences.getString("breakfast_start", "06:00");
        String breakfastEnd = preferences.getString("breakfast_end", "11:00");
        String lunchStart = preferences.getString("lunch_start", "12:00");
        String lunchEnd = preferences.getString("lunch_end", "17:00");
        String dinnerStart = preferences.getString("dinner_start", "18:00");
        String dinnerEnd = preferences.getString("dinner_end", "21:00");
        // Converte os horários para minutos
        int breakfastStartMin = convertTimeToMinute(breakfastStart);
        int breakfastEndMin = convertTimeToMinute(breakfastEnd);
        int lunchStartMin = convertTimeToMinute(lunchStart);
        int lunchEndMin = convertTimeToMinute(lunchEnd);
        int dinnerStartMin = convertTimeToMinute(dinnerStart);
        int dinnerEndMin = convertTimeToMinute(dinnerEnd);

        // Obtém a hora atual
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Calcular o tempo actual em minutos
        int currentTime = time * 60 + minute;

        // Determina o tipo de refeição
        if (currentTime >= breakfastStartMin && currentTime < breakfastEndMin) {
            return context.getString(R.string.breakfast);
        } else if (currentTime >= lunchStartMin && currentTime < lunchEndMin) {
            return context.getString(R.string.lunch);
        } else if (currentTime >= dinnerStartMin && currentTime < dinnerEndMin) {
            return context.getString(R.string.dinner);
        } else {
            return context.getString(R.string.outside_meal_times);
        }
    }

    // Método para converter "HH:MM" para minutos
    private static int convertTimeToMinute(String hora) {
        String[] partes = hora.split(":");
        int time = Integer.parseInt(partes[0]);
        int minute = Integer.parseInt(partes[1]);
        return time * 60 + minute;
    }

    public static void loadingImageMeal(Context context, String imageUrl, ImageView imageView, boolean isDetails) {
        Glide.with(context)
                .load(imageUrl)
                .transform(isDetails ? new RoundedCorners(30) : new CircleCrop())
                .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_60))
                .into(imageView);
    }

    public static void setupVisibility(FragmentMealBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBarMeal.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewNotFoundMeals.setVisibility(viewT);
        binding.recyclerViewMeal.setVisibility(viewR);
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
}