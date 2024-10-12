package com.antonioteca.cc42.dao.datastore;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataStoreManager {

    /*private static DataStoreManager instance;
    private final RxDataStore<Preferences> dataStore;

    // Chaves para armazenar os dados
    private static final Preferences.Key<String> USERNAME_KEY = PreferencesKeys.stringKey("username");
    private static final Preferences.Key<Integer> AGE_KEY = PreferencesKeys.intKey("age");

    // Construtor privado para o padrão Singleton
    private DataStoreManager(Context context) {
        // Inicializando o RxDataStore
        dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
    }

    // Método para obter a instância Singleton
    public static synchronized DataStoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataStoreManager(context);
        }
        return instance;
    }

    // Gravação (Escrita) no DataStore
    public Single<Preferences> saveUsername(String username) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(USERNAME_KEY, username);
            return Single.just(mutablePreferences);
        }).subscribeOn(Schedulers.io());
    }

    // Leitura do DataStore
    public Single<String> getUsername(Preferences.Key<String> key) {
        return dataStore.data().map(prefs -> {
                    return prefs.get(key) != null ? prefs.get(key) : null;
                })
                .firstOrError() // Pegue o primeiro (e único) valor emitido e finalize
                .subscribeOn(Schedulers.io());
    }

    // Leitura síncrona do DataStore
    public String getStringSync(Preferences.Key<String> key) {
        try {
            // Use blockingGet() para obter o valor de forma síncrona
            return dataStore.data()
                    .map(prefs -> prefs.get(key) != null ? prefs.get(key) : null)
                    .firstOrError() // Retorna o primeiro valor ou erro
                    .blockingGet(); // Bloqueia até que o valor esteja disponível
        } catch (Exception e) {
            // Tratar exceção caso ocorra algum erro durante a leitura
            e.printStackTrace();
            return null;
        }
    }


    // Atualização (Atualizar) no DataStore
    public Single<Preferences> updateAge(int age) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences prefs = prefsIn.toMutablePreferences();
            prefs.set(AGE_KEY, age);
            return Single.just(prefs);
        }).subscribeOn(Schedulers.io());
    }

    // Eliminação (Remover) do DataStore
    public Single<Preferences> clearData() {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences prefs = prefsIn.toMutablePreferences();
            prefs.remove(USERNAME_KEY);  // Remove username
            prefs.remove(AGE_KEY);       // Remove age
            return Single.just(prefs);
        }).subscribeOn(Schedulers.io());
    }*/
}