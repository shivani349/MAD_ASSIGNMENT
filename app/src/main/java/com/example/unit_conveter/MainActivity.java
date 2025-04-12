package com.example.unit_conveter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText input;
    Spinner fromUnit, toUnit;
    Button convertBtn;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        input = findViewById(R.id.input);
        fromUnit = findViewById(R.id.fromUnit);
        toUnit = findViewById(R.id.toUnit);
        convertBtn = findViewById(R.id.convertBtn);
        result = findViewById(R.id.result);

        convertBtn.setOnClickListener(v -> {
            String valueStr = input.getText().toString();

            if (valueStr.isEmpty()) {
                result.setText(getString(R.string.enter_value_warning));
                return;
            }

            double value = Double.parseDouble(valueStr);
            String from = fromUnit.getSelectedItem().toString();
            String to = toUnit.getSelectedItem().toString();

            double conversionResult = convert(value, from, to);

            String formattedResult = String.format(Locale.getDefault(), "%.4f", conversionResult);
            String resultMessage = getString(R.string.result_format, valueStr, from, formattedResult, to);
            result.setText(resultMessage);
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
        }

        return 0;
    }
}