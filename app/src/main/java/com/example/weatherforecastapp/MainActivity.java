package com.example.weatherforecastapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText cityInput;
    private Button fetchButton;
    private ProgressBar progressBar;
    private TextView forecastText;

    private final String API_KEY = "f9db59a3eac8649d5af62191a84881ba"; // Reemplaza con tu API key
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        fetchButton = findViewById(R.id.fetchButton);
        progressBar = findViewById(R.id.progressBar);
        forecastText = findViewById(R.id.forecastText);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString();
                if (!city.isEmpty()) {
                    new FetchWeatherTask().execute(city);
                } else {
                    Toast.makeText(MainActivity.this, "Ingresa una ciudad", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            forecastText.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                Log.d("WeatherApp", "Código de respuesta: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    Log.e("WeatherApp", "Error en la solicitud: " + responseCode);
                    return null;
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "Excepción: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                Log.d("WeatherApp", "Respuesta JSON: " + result);
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray forecastList = jsonResponse.getJSONArray("list");
                    StringBuilder forecastData = new StringBuilder();

                    for (int i = 0; i < forecastList.length(); i++) {
                        JSONObject forecast = forecastList.getJSONObject(i);
                        String date = forecast.getString("dt_txt");
                        JSONObject main = forecast.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        JSONArray weatherArray = forecast.getJSONArray("weather");
                        String description = weatherArray.getJSONObject(0).getString("description");

                        forecastData.append("Fecha: ").append(date)
                                .append("\nTemperatura: ").append(temp).append("°C")
                                .append("\nDescripción: ").append(description)
                                .append("\n\n");
                    }

                    forecastText.setText(forecastData.toString());
                } catch (Exception e) {
                    Log.e("WeatherApp", "Error al procesar JSON: " + e.getMessage());
                    forecastText.setText("Error al procesar los datos");
                }
            } else {
                forecastText.setText("Error al obtener el pronóstico");
            }
        }
    }
}