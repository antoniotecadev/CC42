package com.antonioteca.cc42.network.NotificationFirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daoapi.DaoApiMeal;
import com.antonioteca.cc42.dao.daoapi.DaoApiUser;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.Message;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.network.NotificationExpo.ExpoNotificationPayload;
import com.antonioteca.cc42.network.NotificationExpo.NotificationSender;
import com.antonioteca.cc42.utility.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Notification {

    public static void sendFCMNotification(Context context, LayoutInflater layoutInflater, Meal meal, Message messageStaff, String campusId, String cursusId, String topic, String condition) throws IOException {

        FCMessage.Notification notification;
        FCMessage.Data data;
        FCMessage.Message message;

        if (meal != null) {
            notification = new FCMessage.Notification(meal.getType(), meal.getName(), meal.getPathImage());
            data = new FCMessage.Data(meal.getId(), meal.getCreatedBy(), meal.getCreatedDate(), String.valueOf(meal.getQuantityNotReceived()), cursusId, "DetailsMealFragment", meal.getDescription(), notification);
            message = new FCMessage.Message(topic, condition, notification, data);
        } else {
            notification = new FCMessage.Notification(messageStaff.getTitle(), messageStaff.getMessage(), null);
            data = null;
            message = new FCMessage.Message(topic, condition, notification, null);
        }
        FCMessage fcmMessage = new FCMessage(message);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://check-cadet.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DaoApiMeal daoApiMeal = retrofit.create(DaoApiMeal.class);

        daoApiMeal.sendFCMNotification(fcmMessage).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpStatus.getDescription(), "#E53935", null, null);
                } else {
                    if (meal != null) {
                        sendNotificationToIphone(meal, null, campusId, cursusId, data);
                    } else {
                        sendNotificationToIphone(null, messageStaff, campusId, cursusId, data);
                    }
                    if (condition != null)
                        Toast.makeText(context, R.string.notification_sent, Toast.LENGTH_LONG).show();
                    else
                        Util.showAlertDialogBuild(context.getString(R.string.notification_sent), meal.getType(), context, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                HttpException httpException = HttpException.handleException(throwable, context);
                Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpException.getDescription(), "#E53935", null, null);
            }
        });
    }

    private static void sendNotificationToIphone(Meal meal, Message messageStaff, String campusId, String cursusId, FCMessage.Data data) {
        ExpoNotificationPayload payload;
        Map<String, Object> dataExtra = new HashMap<>();
        dataExtra.put("data", data);
        NotificationSender notificationSender = new NotificationSender();
        if (meal != null)
            payload = new ExpoNotificationPayload(meal.getType(), meal.getName(), dataExtra, meal.getPathImage().trim());
        else
            payload = new ExpoNotificationPayload(messageStaff.getTitle(), messageStaff.getMessage(), dataExtra, null);
        notificationSender.sendExpoNotificationToGroup(campusId, cursusId, payload);
    }

    public static void sendFCMNotificationToUser(Context context, ProgressBar progressBar, String selectedStudentDisplayName, String pushToken, String title, String body, Map<String, Object> data, String urlImageUser) {

        FCMessage.Notification notification = new FCMessage.Notification(title, body, urlImageUser);
        FCMessage.Message message = new FCMessage.Message(pushToken, notification, data);

        FCMessage fcmMessage = new FCMessage(message);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://check-cadet.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DaoApiUser daoApiUser = retrofit.create(DaoApiUser.class);

        daoApiUser.sendFCMNotification(fcmMessage).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Util.showAlertDialogBuild(context.getString(R.string.shareLocationTitle), context.getString(R.string.shareLocationSuccess, selectedStudentDisplayName), context, null);
                } else {
                    Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.shareLocationError) + ": " + response.code() + " - " + response.message(), context, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                progressBar.setVisibility(View.GONE);
                Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.shareLocationError) + ": " + throwable.getMessage(), context, null);
            }
        });
    }

//      QUANDO FOR PARA ENVIAR APARTIR DO CLIENTE - NÃO É SEGURO UTILIZAR - APENAS PARA TESTES
//    public static void sendNotificationForTopic(Context context, LayoutInflater layoutInflater, Meal meal, int cursusId, String topic, String condition) throws IOException {
//        AccessTokenGenerator.getAccessToken(context, accessToken -> {
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://fcm.googleapis.com/")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            FCMService service = retrofit.create(FCMService.class);
//
//            FCMessage.Notification notification = new FCMessage.Notification(meal.getType(), meal.getName(), meal.getPathImage());
//            FCMessage.Data data = new FCMessage.Data(meal.getId(), meal.getCreatedBy(), meal.getCreatedDate(), String.valueOf(meal.getQuantity()), String.valueOf(cursusId), "DetailsMealFragment", meal.getDescription(), notification);
//            FCMessage.Message message = new FCMessage.Message(topic, condition, notification, data);
//            FCMessage fcmMessage = new FCMessage(message);
//
//            Call<Void> call = service.sendMessage("Bearer " + accessToken, fcmMessage);
//            call.enqueue(new Callback<>() {
//                @Override
//                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                    if (!response.isSuccessful()) {
//                        HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
//                        Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpStatus.getDescription(), "#E53935", null, null);
//                    } else
//                        Toast.makeText(context, R.string.notification_sent, Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
//                    HttpException httpException = HttpException.handleException(throwable, context);
//                    Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.err), "Notification: " + httpException.getDescription(), "#E53935", null, null);
//                }
//            });
//        });
//    }
}