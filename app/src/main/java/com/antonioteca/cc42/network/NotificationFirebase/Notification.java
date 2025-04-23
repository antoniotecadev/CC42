package com.antonioteca.cc42.network.NotificationFirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.utility.Util;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Notification {

    public static void sendNotificationForTopic(Context context, LayoutInflater layoutInflater, Meal meal, int campusId, int cursusId) throws IOException {
        AccessTokenGenerator.getAccessToken(context, accessToken -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            FCMService service = retrofit.create(FCMService.class);

            FCMessage.Notification notification = new FCMessage.Notification(meal.getType(), meal.getName(), meal.getPathImage());
            FCMessage.Data data = new FCMessage.Data(meal.getId(), meal.getCreatedBy(), meal.getCreatedDate(), String.valueOf(meal.getQuantity()), String.valueOf(cursusId), "DetailsMealFragment", meal.getDescription(), notification);
            FCMessage.Message message = new FCMessage.Message("meals_" + campusId + "_" + cursusId, notification, data);
            FCMessage fcmMessage = new FCMessage(message);

            Call<Void> call = service.sendMessage("Bearer " + accessToken, fcmMessage);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (!response.isSuccessful()) {
                        HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpStatus.getDescription(), "#E53935", null);
                    } else
                        Toast.makeText(context, R.string.notification_sent, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                    HttpException httpException = HttpException.handleException(throwable, context);
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpException.getDescription(), "#E53935", null);
                }
            });
        });
    }
}