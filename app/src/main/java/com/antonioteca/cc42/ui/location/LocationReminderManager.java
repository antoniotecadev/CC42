package com.antonioteca.cc42.ui.location;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 📍 LOCAL LOCATION REMINDER MANAGER
 * <p>
 * Gerencia o agendamento de notificações locais para lembrar o estudante
 * de actualizar sua localização, usando o WorkManager do Android.
 */
public class LocationReminderManager {

    private static final String TAG = "LocationReminderManager";
    public static final String LOCATION_REMINDER_WORK_TAG = "location-reminder-work";
    private static final int[] REMINDER_HOURS = {10 /* 10, 12, 14, 16, 18, 20 */ };

    /**
     * Agenda notificações diárias para lembrar de actualizar a localização.
     * Usa o WorkManager para garantir a execução.
     */
    public static void scheduleLocationReminders(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);

        // Cancela trabalhos antigos para evitar duplicatas
        cancelLocationReminders(context);

        for (int hour : REMINDER_HOURS) {
            // Calcula o atraso inicial até a próxima ocorrência da 'hour'
            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.set(Calendar.HOUR_OF_DAY, hour);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);

            if (now.after(target)) {
                target.add(Calendar.DAY_OF_YEAR, 1); // Se já passou, agenda para amanhã
            }

            long initialDelay = target.getTimeInMillis() - now.getTimeInMillis();

            // Dados para passar ao Worker (saber qual hora o disparou)
            Data inputData = new Data.Builder().putInt("hour", hour).build();

            // Cria uma requisição periódica que se repete a cada 24 horas
            PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                    LocationReminderWorker.class, 24, TimeUnit.HOURS)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(LOCATION_REMINDER_WORK_TAG) // Tag para agrupar
                    .build();

            // Nome único para cada trabalho, para evitar duplicatas
            String uniqueWorkName = "location_reminder_" + hour;
            workManager.enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.UPDATE, reminderRequest);

            Log.d(TAG, "Lembrete agendado para " + hour + ":00 (Trabalho: " + uniqueWorkName + ")");
        }
        Log.d(TAG, REMINDER_HOURS.length + " lembretes de localização agendados com sucesso!");
    }

    /**
     * Cancela todos os lembretes de localização agendados.
     */
    public static void cancelLocationReminders(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(LOCATION_REMINDER_WORK_TAG);
        Log.d(TAG, "🗑️ Lembretes de localização cancelados.");
    }

    /**
     * Agenda um lembrete único (one-time) após X horas.
     *
     * @param hoursFromNow Quantas horas a partir de agora.
     */
    public static void scheduleOneTimeReminder(Context context, int hoursFromNow) {
        if (hoursFromNow <= 0) return;

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(LocationReminderWorker.class)
                .setInitialDelay(hoursFromNow, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "one_time_reminder", ExistingWorkPolicy.REPLACE, oneTimeRequest);

        Log.d(TAG, "⏰ Lembrete único agendado para daqui " + hoursFromNow + "h");
    }

    /**
     * Verifica se os lembretes estão agendados e, se não, os reagenda.
     */
    public static void rescheduleIfNeeded(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.getWorkInfosByTagLiveData(LOCATION_REMINDER_WORK_TAG).observeForever(workInfos -> {
            boolean isScheduled = workInfos.stream().anyMatch(info ->
                    !info.getState().isFinished()
            );

            if (!isScheduled) {
                Log.d(TAG, "🔄 Reagendando lembretes de localização...");
                scheduleLocationReminders(context);
            } else {
                Log.d(TAG, "✅ " + workInfos.size() + " lembretes já estão agendados.");
            }
        });
    }
}

// Para agendar tudo pela primeira vez ou reagendar
//LocationReminderManager.scheduleLocationReminders(getApplicationContext());

// Para cancelar todos os lembretes (ex: no logout do usuário)
//        LocationReminderManager.cancelLocationReminders(getApplicationContext());

// Para agendar um lembrete único após 2 horas
//        LocationReminderManager.scheduleOneTimeReminder(getApplicationContext(), 2);

// Para verificar e reagendar se necessário (bom para chamar na inicialização do app)
//        LocationReminderManager.rescheduleIfNeeded(getApplicationContext());
