package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WeatherViewModel weatherViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        binding.search.setOnClickListener(v -> {
            hideKeyboard();
            String cityName = binding.city.getText().toString().trim();
            weatherViewModel.fetchWeather(cityName);
        });

        observeUiState();
    }

    private void observeUiState() {
        weatherViewModel.uiState.observe(this, state -> {
            binding.progressBar.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);
            binding.weather.setVisibility(state.weatherInfo != null ? View.VISIBLE : View.GONE);

            if (state.weatherInfo != null) {
                binding.weather.setText(state.weatherInfo);
            }

            if (state.error != null) {
                Toast.makeText(this, state.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        binding.city.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.city.getWindowToken(), 0);
        }
    }
}
