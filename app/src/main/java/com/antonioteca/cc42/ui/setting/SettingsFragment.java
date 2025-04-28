package com.antonioteca.cc42.ui.setting;

import static com.antonioteca.cc42.network.NetworkConstants.REQUEST_CODE_POST_NOTIFICATIONS;
import static com.antonioteca.cc42.utility.Util.setAppLanguage;
import static com.antonioteca.cc42.utility.Util.setRequestPermissionLauncherNotification;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Exception exception;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        User user = new User(requireContext());
        String campusId = String.valueOf(user.getCampusId());
        String cursusId = String.valueOf(user.getCursusId());
        String topic = "/topics/meals_" + campusId + "_" + cursusId;

        PreferenceCategory breakfastCategory = findPreference("breakfast");
        PreferenceCategory lunchCategory = findPreference("lunch");
        PreferenceCategory dinnerCategory = findPreference("dinner");

        ListPreference languageKey = findPreference("language_key");
        SwitchPreferenceCompat themeModeKey = findPreference("theme_mode_key");
        SwitchPreferenceCompat notificationKey = findPreference("notification_key");
        SwitchPreferenceCompat mealTimeRangeKey = findPreference("meal_time_range_key");

        ThemePreferences themePreferences = new ThemePreferences(requireContext());

        if (notificationKey != null) {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            notificationKey.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!notificationKey.isChecked()) {
                    messaging.subscribeToTopic(topic).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            exception = task.getException();
                            if (exception != null)
                                Util.showAlertDialogMessage(preference.getContext(), getLayoutInflater(), preference.getContext().getString(R.string.err), exception.getMessage(), "#E53935", null, null);
                        } else
                            setRequestPermissionLauncherNotification(preference.getContext(), REQUEST_CODE_POST_NOTIFICATIONS);
                    });
                } else {
                    messaging.unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
                        if (!task.isSuccessful())
                            if (exception != null) {
                                Util.showAlertDialogMessage(preference.getContext(), getLayoutInflater(), preference.getContext().getString(R.string.err), exception.getMessage(), "#E53935", null, null);
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
        if (themeModeKey != null) {
            // Define o estado inicial do Switch
            boolean isDarkMode = themePreferences.getThemeMode() == AppCompatDelegate.MODE_NIGHT_YES;
            themeModeKey.setChecked(isDarkMode);

            // Define o listener para mudanças
            themeModeKey.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean themeIsDarkMode = (boolean) newValue;

                // Salva a preferência do usuário
                int themeMode = themeIsDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
                themePreferences.setThemeMode(themeMode);

                // Aplica o tema
                AppCompatDelegate.setDefaultNightMode(themeMode);

                // Reinicia a Activity para aplicar o novo tema
                requireActivity().recreate();
                return true;
            });
        }

        if (mealTimeRangeKey != null && breakfastCategory != null && lunchCategory != null && dinnerCategory != null) {
            breakfastCategory.setVisible(mealTimeRangeKey.isChecked());
            lunchCategory.setVisible(mealTimeRangeKey.isChecked());
            dinnerCategory.setVisible(mealTimeRangeKey.isChecked());

            mealTimeRangeKey.setOnPreferenceChangeListener((preference, newValue) -> {
                breakfastCategory.setVisible((boolean) newValue);
                lunchCategory.setVisible((boolean) newValue);
                dinnerCategory.setVisible((boolean) newValue);
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