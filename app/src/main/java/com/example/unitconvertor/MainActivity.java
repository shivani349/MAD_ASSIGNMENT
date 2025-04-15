package com.example.unitconvertor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    EditText input;
    Spinner fromUnit, toUnit;
    TextView result;
    Button convertBtn, settingsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();  // Apply theme only once when activity is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] units = {"Feet", "Inches", "Meters", "Centimeters", "Yards"};

        input = findViewById(R.id.input);
        fromUnit = findViewById(R.id.fromUnit);
        toUnit = findViewById(R.id.toUnit);
        convertBtn = findViewById(R.id.convertBtn);
        result = findViewById(R.id.result);
        settingsBtn = findViewById(R.id.settingsBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromUnit.setAdapter(adapter);
        toUnit.setAdapter(adapter);

        convertBtn.setOnClickListener(v -> {
            String inputText = input.getText().toString();
            if (inputText.isEmpty()) {
                Toast.makeText(this, R.string.please_enter_value, Toast.LENGTH_SHORT).show();
                return;
            }
            double value = Double.parseDouble(inputText);
            double convertedValue = convert(value, fromUnit.getSelectedItem().toString(), toUnit.getSelectedItem().toString());
            result.setText("Result: " + convertedValue);
        });

        settingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    public double convert(double value, String from, String to) {
        double meters = 0;

        switch (from) {
            case "Feet": meters = value * 0.3048; break;
            case "Inches": meters = value * 0.0254; break;
            case "Centimeters": meters = value * 0.01; break;
            case "Meters": meters = value; break;
            case "Yards": meters = value * 0.9144; break;
        }

        switch (to) {
            case "Feet": return meters / 0.3048;
            case "Inches": return meters / 0.0254;
            case "Centimeters": return meters / 0.01;
            case "Meters": return meters;
            case "Yards": return meters / 0.9144;
            default: return meters;
        }
    }

    private void applyTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = preferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}



