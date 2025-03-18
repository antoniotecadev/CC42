package com.antonioteca.cc42.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.dao.daoapi.DaoApiUser;
import com.antonioteca.cc42.dao.daoapi.PaginationLinks;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Subscription;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.RetrofitClientApi;
import com.antonioteca.cc42.utility.Loading;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Classe que gerencia a lógica de negócios e a comunicação entre os dados (Model) e o ViewModel
 * O repositório será responsável por realizar as chamadas de API
 */

public class UserRepository {

    private final User user;
    private final Token token;
    private final Context context;
    private final DaoApiUser daoApiUser;

    //private final AppDataBase appDataBase;

    public UserRepository(Context context) {
        this.context = context;
        user = new User(context);
        token = new Token(context);
        /*WeakReference<Context> weakReference = new WeakReference<>(context);
        appDataBase = AppDataBase.getInstance(weakReference.get());*/
        daoApiUser = RetrofitClientApi.getApiService().create(DaoApiUser.class);
    }

    /*public Completable insert(LocalAttendanceList user) {
        return appDataBase.userDao().insert(user);
    }

    public Single<List<LocalAttendanceList>> userAlreadyLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId,
            long userId) {
        return appDataBase.userDao().userAlreadyLocalAttendanceList(campusId, cursusId, eventId, userId);
    }

    public Single<List<Long>> geIdsUserLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId) {
        return appDataBase.userDao().geIdsUserLocalAttendanceList(campusId, cursusId, eventId);
    }

    public Completable deleteLocalAttendanceList(
            int campusId,
            int cursusId,
            long eventId) {
        return appDataBase.userDao().deleteLocalAttendanceList(campusId, cursusId, eventId);
    }*/

    public boolean saveUser(User user) {
        return this.user.saveUser(user, user.coalition);
    }

    public void getUser(Callback<User> callback) {
        Call<User> userCall = daoApiUser.getUser("Bearer " + token.getAccessToken());
        userCall.enqueue(callback);
    }

    public void getCoalition(long userId, Callback<List<Coalition>> callback) {
        Call<List<Coalition>> coalitionCall = daoApiUser.getCoalition(userId, "Bearer " + token.getAccessToken());
        coalitionCall.enqueue(callback);
    }

    public void loadUsersEventPaginated(long eventId, @NonNull Loading l, Callback<List<User>> callback) {
        if (token.isTokenExpired(token.getTokenExpirationTime())) {
            token.getRefreshTokenUserSave(context, (success) -> {
                if (success)
                    extractedLoadUsersEventPaginated(eventId, l, callback);
                else
                    callback.onResponse(null, Response.success(new ArrayList<>()));
            });
        } else
            extractedLoadUsersEventPaginated(eventId, l, callback);
    }

    private void extractedLoadUsersEventPaginated(long eventId, @NonNull Loading l, Callback<List<User>> callback) {
        String accessToken = "Bearer " + token.getAccessToken();

        daoApiUser.getUsersEvent(eventId, accessToken, l.currentPage, 50).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                    // Verificar se existe uma próxima página
                    PaginationLinks links = extractPaginationLinks(response.headers());
                    if (links == null) {
                        l.hasNextPage = false;
                    } else {
                        l.hasNextPage = links.getNext() != null;
                        if (l.hasNextPage) {
                            l.currentPage++;
                        }
                    }
                } else {
                    l.hasNextPage = false;
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable throwable) {
                l.hasNextPage = false;
                callback.onFailure(call, throwable);
            }
        });
    }

    public void loadUserSubscriptionPaginated(int cursusId, @NonNull Loading l, Callback<List<Subscription>> callback) {
        if (token.isTokenExpired(token.getTokenExpirationTime())) {
            token.getRefreshTokenUserSave(context, (success) -> {
                if (success)
                    extractedLoadUserSubscriptionPaginated(cursusId, l, callback);
                else
                    callback.onResponse(null, Response.success(new ArrayList<>()));
            });
        } else
            extractedLoadUserSubscriptionPaginated(cursusId, l, callback);
    }

    private void extractedLoadUserSubscriptionPaginated(int cursusId, @NonNull Loading l, Callback<List<Subscription>> callback) {
        String accessToken = "Bearer " + token.getAccessToken();

        daoApiUser.getUsersSubscription(accessToken, cursusId, user.getCampusId(), true, l.currentPage, 50).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Subscription>> call, @NonNull Response<List<Subscription>> response) {

                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                    // Verificar se existe uma próxima página
                    PaginationLinks links = extractPaginationLinks(response.headers());
                    if (links == null) {
                        l.hasNextPage = false;
                    } else {
                        l.hasNextPage = links.getNext() != null;
                        if (l.hasNextPage) {
                            l.currentPage++;
                        }
                    }
                } else {
                    l.hasNextPage = false;
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Subscription>> call, @NonNull Throwable throwable) {
                l.hasNextPage = false;
                callback.onFailure(call, throwable);
            }
        });
    }

    // Link : <https://api.intra.42.fr/v2/messages?page=1>; rel="first", <https://api.intra.42.fr/v2/messages?page=1>; rel="prev", <https://api.intra.42.fr/v2/messages?page=586>; rel="last", <https://api.intra.42.fr/v2/messages?page=3>; rel="next"
    public PaginationLinks extractPaginationLinks(Headers headers) {
        PaginationLinks links = new PaginationLinks();
        String linkHeader = headers.get("Link");

        // Regex para extrair o link com o "rel" correto
        Pattern pattern = Pattern.compile("<(.*?)>; rel=\"(.*?)\"");
        Matcher matcher;
        if (linkHeader != null) {
            matcher = pattern.matcher(linkHeader);
        } else
            return null;

        while (matcher.find()) {
            String url = matcher.group(1); // Link
            String rel = matcher.group(2); // Tipo de relação (next, prev, first, last)

            // Identificar o link de acordo com o tipo de relação
            if ("next".equals(rel)) {
                links.setNext(url);
            } else if ("prev".equals(rel)) {
                links.setPrev(url);
            } else if ("first".equals(rel)) {
                links.setFirst(url);
            } else if ("last".equals(rel)) {
                links.setLast(url);
            }
        }
        return links;
    }
}
