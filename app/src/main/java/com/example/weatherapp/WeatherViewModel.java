package com.example.weatherapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherViewModel extends ViewModel {

    private final MutableLiveData<WeatherUiState> _uiState = new MutableLiveData<>();
    public LiveData<WeatherUiState> uiState = _uiState;

    private final String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
    private final String apiKey = "04462fbfd0639b3e9fb85758a32647a6";

    public void fetchWeather(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            _uiState.setValue(new WeatherUiState(false, null, "Prosim vpiši veljavno mesto"));
            return;
        }

        new Thread(() -> {
            _uiState.postValue(new WeatherUiState(true, null, null));
            try {
                String urlString = baseUrl + "?q=" + cityName + "&appid=" + apiKey;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String weatherInfo = parseWeatherResponse(response.toString());
                    _uiState.postValue(new WeatherUiState(false, weatherInfo, null));
                } else {
                    _uiState.postValue(new WeatherUiState(false, null, "Napaka pri iskanju podatkov o vremenu"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                _uiState.postValue(new WeatherUiState(false, null, "Napaka: " + e.getMessage()));
            }
        }).start();
    }

    private String parseWeatherResponse(String response) throws Exception {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
        String main = weatherObject.getString("main");
        String description = weatherObject.getString("description");
        JSONObject mainObject = jsonObject.getJSONObject("main");

        double temp = mainObject.getDouble("temp");
        double feelsLike = mainObject.getDouble("feels_like");
        double tempMin = mainObject.getDouble("temp_min");
        double tempMax = mainObject.getDouble("temp_max");

        return String.format(
                "Glavno : %s\nOpis : %s\nTemperatura : %s\nČuti se kot : %s\nMinimalno : %s\nMaksimalno : %s",
                main,
                description,
                kelvinToCelsius(temp),
                kelvinToCelsius(feelsLike),
                kelvinToCelsius(tempMin),
                kelvinToCelsius(tempMax)
        );
    }

    private String kelvinToCelsius(double kelvin) {
        return String.format("%.2f°C", kelvin - 273.15);
    }

    public static class WeatherUiState {
        public boolean isLoading;
        public String weatherInfo;
        public String error;

        public WeatherUiState(boolean isLoading, String weatherInfo, String error) {
            this.isLoading = isLoading;
            this.weatherInfo = weatherInfo;
            this.error = error;
        }
    }
}
