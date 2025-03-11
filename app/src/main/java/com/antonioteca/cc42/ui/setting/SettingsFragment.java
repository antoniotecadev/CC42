package com.antonioteca.cc42.ui.setting;

import static com.antonioteca.cc42.network.NetworkConstants.REQUEST_CODE_POST_NOTIFICATIONS;
import static com.antonioteca.cc42.utility.Util.setAppLanguage;
import static com.antonioteca.cc42.utility.Util.setRequestPermissionLauncherNotification;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Exception exception;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SwitchPreferenceCompat notificationKey = findPreference("notification_key");
        ListPreference languageKey = findPreference("language_key");

        if (notificationKey != null) {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            notificationKey.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!notificationKey.isChecked()) {
                    messaging.subscribeToTopic("/topics/meals").addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            exception = task.getException();
                            if (exception != null)
                                Util.showAlertDialogMessage(preference.getContext(), getLayoutInflater(), preference.getContext().getString(R.string.err), exception.getMessage(), "#E53935", null);
                        } else
                            setRequestPermissionLauncherNotification(preference.getContext(), REQUEST_CODE_POST_NOTIFICATIONS);
                    });
                } else {
                    messaging.unsubscribeFromTopic("/topics/meals").addOnCompleteListener(task -> {
                        if (!task.isSuccessful())
                            if (exception != null) {
                                Util.showAlertDialogMessage(preference.getContext(), getLayoutInflater(), preference.getContext().getString(R.string.err), exception.getMessage(), "#E53935", null);
                            }
                    });
                }
                return true;
            });
        }
        if (languageKey != null) {
            languageKey.setOnPreferenceChangeListener((preference, newValue) -> {
                // Obter o idioma selecionado
                String language = newValue.toString();
                // Alterar o idioma da aplicação
                setAppLanguage(language, getResources(), requireActivity(), true);
                return true;
            });
        }
        // Obtém as SharedPreferences
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        // Configurar o clique nas Preferences
        setupTimePicker("breakfast_start", "06:00");
        setupTimePicker("breakfast_end", "11:00");
        setupTimePicker("lunch_start", "12:00");
        setupTimePicker("lunch_end", "17:00");
        setupTimePicker("dinner_start", "18:00");
        setupTimePicker("dinner_end", "21:00");
    }

    private void setupTimePicker(String key, String defaultValue) {
        Preference preference = findPreference(key);
        if (preference != null) {
            // Define o valor inicial (se não existir)
            if (!sharedPreferences.contains(key)) {
                sharedPreferences.edit().putString(key, defaultValue).apply();
            }

            // Atualiza o summary com o valor atual
            preference.setSummary(sharedPreferences.getString(key, defaultValue));

            // Configura o clique na Preference
            preference.setOnPreferenceClickListener(pref -> {
                showTimePickerDialog(getContext(), key);
                return true;
            });
        }
    }

    private void showTimePickerDialog(Context context, String key) {
        // Obtém o horário atual salvo
        String currentTime = sharedPreferences.getString(key, "00:00");
        String[] timeParts = currentTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // Cria e exibe o TimePickerDialog
        TimePickerDialog dialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minuteOfHour) -> {
                    // Salva o novo horário nas SharedPreferences
                    Locale currentLocale = Locale.getDefault();
                    String newTime = String.format(currentLocale, "%02d:%02d", hourOfDay, minuteOfHour);
                    sharedPreferences.edit().putString(key, newTime).apply();

                    // Atualiza o summary da Preference
                    Preference preference = findPreference(key);
                    if (preference != null) {
                        preference.setSummary(newTime);
                    }
                },
                hour,
                minute,
                true // Usar formato 24 horas
        );
        dialog.show();
    }
}