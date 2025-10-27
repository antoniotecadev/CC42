package com.antonioteca.cc42.ui.location;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.antonioteca.cc42.R;

public class LocationReminderWorker extends Worker {

    public static final String NOTIFICATION_CHANNEL_ID = "location_reminder_channel";

    public LocationReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Pega a hora para qual este worker foi agendado (passado como dado de entrada)
        int hour = getInputData().getInt("hour", -1);

        // Exibe a notificação local
        sendNotification(hour);

        // Retorna sucesso para indicar que a tarefa foi concluída
        return Result.success();
    }

    private void sendNotification(int hour) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cria o canal de notificação (necessário para Android 8.0 Oreo e superior)
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Lembretes de Localização",
                    NotificationManager.IMPORTANCE_DEFAULT // Sem som por padrão
            );
            channel.setDescription("Notificações para lembrar de actualizar a localização.");
            notificationManager.createNotificationChannel(channel);
        }

        // Constrói a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.reminderTitle))
                .setContentText(context.getString(R.string.reminderBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // A notificação desaparece ao ser tocada

        // ID único para a notificação (para que uma substitua a outra na barra de status)
        int notificationId = 1000;
        notificationManager.notify(notificationId, builder.build());
    }
}
