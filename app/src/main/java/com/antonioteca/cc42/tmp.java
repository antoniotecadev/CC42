import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class UserEventCache(private val apiService: YourApiService) {

    // Chave de cache composta por eventId + página
    data class UserEventKey(val eventId: Long, val pageNumber: Int, val pageSize: Int)

    private val store: Store<UserEventKey, List<User>> = StoreBuilder.from(
        fetcher = { key: UserEventKey ->
            apiService.getUsersEvent(key.eventId, "Bearer ${TokenManager.getAccessToken()}", key.pageNumber, key.pageSize)
                .execute()
                .body() ?: emptyList()
        },
        sourceOfTruth = SourceOfTruth.Companion.from(
            reader = { key -> /* Ler do banco de dados local se aplicável */ },
            writer = { key, users -> /* Escrever no banco de dados local se aplicável */ },
            delete = { key -> /* Deletar do banco de dados local se aplicável */ },
            deleteAll = { /* Deletar tudo do banco de dados local se aplicável */ }
        )
    ).cachePolicy(
        MemoryPolicy.builder()
            .setExpireAfterWrite(10) // 10 minutos
            .setExpireAfterTimeUnit(TimeUnit.MINUTES)
            .build()
    ).build()

    suspend fun getUsers(eventId: Long, pageNumber: Int, pageSize: Int): List<User> {
        val key = UserEventKey(eventId, pageNumber, pageSize)
        return store.get(key)
    }

    fun streamUsers(eventId: Long, pageNumber: Int, pageSize: Int): Flow<List<User>> {
        val key = UserEventKey(eventId, pageNumber, pageSize)
        return store.stream(key)
    }

    suspend fun clearCache(eventId: Long, pageNumber: Int, pageSize: Int) {
        val key = UserEventKey(eventId, pageNumber, pageSize)
        store.clear(key)
    }

    suspend fun clearAllCache() {
        store.clearAll()
    }
}



public class UserRepository {
    private final UserEventCache userEventCache;
    private final YourApiService apiService;
    private final Context context;
    private final TokenManager token;

    public UserRepository(Context context, YourApiService apiService, TokenManager token) {
        this.context = context;
        this.apiService = apiService;
        this.token = token;
        this.userEventCache = new UserEventCache(apiService);
    }

    public void loadUsersEventPaginated(long eventId, @NonNull Loading l, Callback<List<User>> callback) {
        if (token.isTokenExpired(token.getTokenExpirationTime())) {
            token.getRefreshTokenUserSave(context, (success) -> {
                if (success)
                    loadUsersWithCache(eventId, l, callback);
                else
                    callback.onResponse(null, Response.success(new ArrayList<>()));
            });
        } else {
            loadUsersWithCache(eventId, l, callback);
        }
    }

    private void loadUsersWithCache(long eventId, @NonNull Loading l, Callback<List<User>> callback) {
        // Defina seus valores padrão de paginação ou obtenha do repositório
        int pageNumber = 1;
        int pageSize = 20;

        // Usando coroutines para chamar o cache (pode adaptar para Callback se necessário)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                List<User> users = userEventCache.getUsers(eventId, pageNumber, pageSize);
                callback.onResponse(null, Response.success(users));
            } catch (e: Exception) {
                // Em caso de falha no cache, chama a API diretamente
                Call<List<User>> call = apiService.getUsersEvent(
                    eventId,
                    "Bearer " + token.getAccessToken(),
                    pageNumber,
                    pageSize
                );
                call.enqueue(callback);
            }
        }
    }

    // Método para forçar atualização do cache
    public void refreshUsersEventCache(long eventId, int pageNumber, int pageSize) {
        CoroutineScope(Dispatchers.IO).launch {
            userEventCache.clearCache(eventId, pageNumber, pageSize);
        }
    }
}

public void getUsersEvent(long eventId, Loading l, Context context) {
    l.isLoading = true;
    userRepository.loadUsersEventPaginated(eventId, l, new Callback<>() {
        @Override
        public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
            if (response.isSuccessful()) {
                userListMutableLiveData.postValue(response.body());
            } else {
                HttpStatus httpStatus = HttpStatus.handleResponse(response.code());
                httpStatusMutableLiveDataEvent.postValue(new EventObserver<>(httpStatus));
            }
            l.isLoading = false;
        }

        @Override
        public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable throwable) {
            HttpException httpException = HttpException.handleException(throwable, context);
            httpExceptionMutableLiveDataEvent.postValue(new EventObserver<>(httpException));
            l.isLoading = false;
        }
    });
}

// Método para forçar atualização
public void refreshUsersEvent(long eventId) {
    userRepository.refreshUsersEventCache(eventId, 1, 20); // assumindo página 1 e size 20
    getUsersEvent(eventId, new Loading(), context);
}


