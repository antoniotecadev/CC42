Fetcher<Pair<EventPageKey, String>, List<User>> fetcher = (key) -> {
    long eventId = key.first.eventId;
    int page = key.first.page;
    int size = key.first.size;
    String token = key.second;
    
    return getUsersEventApi()
        .getUsersEvent(eventId, "Bearer " + token, page, size)
        .execute()
        .body();
};

public class EventPageKey {
    public final long eventId;
    public final int page;
    public final int size;

    public EventPageKey(long eventId, int page, int size) {
        this.eventId = eventId;
        this.page = page;
        this.size = size;
    }

    // equals & hashCode são necessários para funcionar corretamente no cache do Store
}
SourceOfTruth<Pair<EventPageKey, String>, List<User>, List<User>> sourceOfTruth =
    new SourceOfTruth<>() {
        private final Map<Pair<EventPageKey, String>, List<User>> memoryCache = new HashMap<>();

        @NonNull
        @Override
        public ListenableFuture<Void> delete(@NonNull Pair<EventPageKey, String> key) {
            memoryCache.remove(key);
            return Futures.immediateVoidFuture();
        }

        @NonNull
        @Override
        public ListenableFuture<Void> deleteAll() {
            memoryCache.clear();
            return Futures.immediateVoidFuture();
        }

        @NonNull
        @Override
        public ListenableFuture<List<User>> read(@NonNull Pair<EventPageKey, String> key) {
            return Futures.immediateFuture(memoryCache.get(key));
        }

        @NonNull
        @Override
        public ListenableFuture<Void> write(@NonNull Pair<EventPageKey, String> key, @NonNull List<User> users) {
            memoryCache.put(key, users);
            return Futures.immediateVoidFuture();
        }
    };
Store<Pair<EventPageKey, String>, List<User>> userEventStore = StoreBuilder
    .from(fetcher)
    .persister(sourceOfTruth)
    .build();


public void loadUsersEventPaginatedStore(long eventId, int page, int pageSize, @NonNull Loading l, Callback<List<User>> callback) {
    if (token.isTokenExpired(token.getTokenExpirationTime())) {
        token.getRefreshTokenUserSave(context, (success) -> {
            if (success)
                callStore(eventId, page, pageSize, l, callback);
            else
                callback.onResponse(null, Response.success(new ArrayList<>()));
        });
    } else {
        callStore(eventId, page, pageSize, l, callback);
    }
}

private void callStore(long eventId, int page, int pageSize, Loading l, Callback<List<User>> callback) {
    EventPageKey key = new EventPageKey(eventId, page, pageSize);
    Pair<EventPageKey, String> storeKey = new Pair<>(key, token.getAccessToken());

    l.isLoading = true;
    userEventStore.get(storeKey)
        .addListener(() -> {
            try {
                List<User> users = userEventStore.get(storeKey).get();
                callback.onResponse(null, Response.success(users));
            } catch (Exception e) {
                callback.onFailure(null, e);
            } finally {
                l.isLoading = false;
            }
        }, Executors.newSingleThreadExecutor());
}

