package com.example.unitconvertor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Begin the fragment transaction to show the preferences screen
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from XML
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // Listen for changes to the 'dark_mode' preference
            Preference darkModeSwitch = findPreference("dark_mode");
            if (darkModeSwitch != null) {
                darkModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save the new theme preference
                    boolean darkMode = (Boolean) newValue;
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .edit()
                            .putBoolean("dark_mode", darkMode)
                            .apply();

                    // Apply the new theme only once
                    applyTheme();
                    return true;
                });
            }
        }

        // Apply theme dynamically when the preference is changed
        private void applyTheme() {
            boolean darkMode = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getBoolean("dark_mode", false);  // Default is false (Light Mode)

            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);  // Dark Mode
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);   // Light Mode
            }
        }
    }
}






