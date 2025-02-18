package com.antonioteca.cc42.ui.setting;

import static com.antonioteca.cc42.utility.Util.setAppLanguage;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Exception exception;

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
                        }
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
                setAppLanguage(language, getResources(), getActivity(), true);
                return true;
            });
        }
    }
}