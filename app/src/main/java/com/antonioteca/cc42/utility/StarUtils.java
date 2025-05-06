package com.antonioteca.cc42.utility;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.antonioteca.cc42.model.User;

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
}
