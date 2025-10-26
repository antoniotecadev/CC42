package com.antonioteca.cc42.model;

import android.content.Context;

import com.antonioteca.cc42.R;

import java.time.Instant;
import java.time.Duration;

public class ReliabilityCalculator {

    public static ReliabilityResult getReliability(Context context, long lastUpdatedMillis) {
        Instant now = Instant.now();
        Instant lastUpdated = Instant.ofEpochMilli(lastUpdatedMillis);
        Duration diff = Duration.between(lastUpdated, now);

        long diffMinutes = diff.toMinutes();
        long diffHours = diff.toHours();
        long diffDays = diff.toDays();

        // Menos de 5 minutos: Muito Confiável
        if (diffMinutes < 5) {
            return new ReliabilityResult(
                    context.getString(R.string.veryReliable),
                    "#27ae60", // Verde
                    100,
                    context.getString(R.string.updatedRecently)
            );
        }
        // 5-30 minutos: Confiável
        else if (diffMinutes < 30) {
            return new ReliabilityResult(
                    context.getString(R.string.reliable),
                    "#2ecc71", // Verde claro
                    80,
                    context.getString(R.string.updatedMinutesAgo, diffMinutes)
            );
        }
        // 30 minutos - 2 horas: Incerto
        else if (diffHours < 2) {
            String message = (diffHours == 1)
                    ? context.getString(R.string.updatedHoursAgo, 1)
                    : context.getString(R.string.updatedMinutesAgo, diffMinutes);

            return new ReliabilityResult(
                    context.getString(R.string.uncertain),
                    "#f39c12", // Laranja
                    50,
                    message
            );
        }
        // Mais de 2 horas: Não Confiável
        else {
            String message = (diffDays > 0)
                    ? context.getString(R.string.updatedDaysAgo, diffDays)
                    : context.getString(R.string.updatedHoursAgo, diffHours);
            return new ReliabilityResult(
                    context.getString(R.string.unreliable),
                    "#e74c3c", // Vermelho
                    20,
                    message
            );
        }
    }

    public static String getTimeAgo(Context context, long lastUpdatedMillis) {
        Instant now = Instant.now();
        Instant lastUpdated = Instant.ofEpochMilli(lastUpdatedMillis);
        Duration diff = Duration.between(lastUpdated, now);

        long diffMinutes = diff.toMinutes();
        if (diffMinutes < 1) {
            return context.getString(R.string.updatedRecently);
        }
        if (diffMinutes < 60) {
            return context.getString(R.string.updatedMinutesAgo, diffMinutes);
        }

        long diffHours = diff.toHours();
        if (diffHours < 24) {
            return context.getString(R.string.updatedHoursAgo, diffHours);
        }

        long diffDays = diff.toDays();
        return context.getString(R.string.updatedDaysAgo, diffDays);
    }
}
