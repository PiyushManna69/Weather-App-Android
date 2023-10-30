package com.infinisync.weather;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.infinisync.weather.databinding.ActivityMainBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static  final int REQUEST_LOCATION = 1;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.mainContainer.setVisibility(View.GONE);
        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        fetchLocation();
    }

    private void fetchLocation() {
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            OnGPS();
        }
        else
        {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double latitude=LocationGps.getLatitude();
                double longitude=LocationGps.getLongitude();

                updateWeatherInfo(latitude,longitude);
            }
            else if (LocationNetwork !=null)
            {
                double latitude=LocationGps.getLatitude();
                double longitude=LocationGps.getLongitude();

                updateWeatherInfo(latitude,longitude);
            }
            else if (LocationPassive !=null)
            {
                double latitude=LocationGps.getLatitude();
                double longitude=LocationGps.getLongitude();

                updateWeatherInfo(latitude,longitude);
            }
            else
            {
                handleError("Can't Get Your Location");
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.progressBar.setVisibility(View.GONE);
                binding.errorTxt.setVisibility(View.GONE);
                getLocation();
            } else {
                handleError("Permission denied. Please grant location permission.");
            }
        }
    }

    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    public void updateWeatherInfo(double latitude, double longitude) {
        Volley.newRequestQueue(this).add(new JsonObjectRequest(
                0, "https://api.openweathermap.org/data/2.5/weather?lat=22.9409&lon=88.1595&units=metric&appid=" + Constant.API_KEY, null,
                response -> {
                    try {
                        JSONObject mainObject = response.getJSONObject("main");
                        double temperature = mainObject.getDouble("temp");
                        int pressure = mainObject.getInt("pressure");
                        int humidity = mainObject.getInt("humidity");
                        double tempMax = mainObject.getDouble("temp_max");
                        double feelsLike = mainObject.getDouble("feels_like");
                        JSONArray weatherArray = response.getJSONArray("weather");
                        String weatherMain = weatherArray.getJSONObject(0).getString("main");
                        JSONObject sysObject = response.getJSONObject("sys");
                        String country = sysObject.getString("country");
                        long sunrise = sysObject.getLong("sunrise");
                        long sunset = sysObject.getLong("sunset");

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        Date sunriseDate = new Date(sunrise * 1000);
                        Date sunsetDate = new Date(sunset * 1000);
                        String formattedSunrise = sdf.format(sunriseDate);
                        String formattedSunset = sdf.format(sunsetDate);

                        int visibility = response.getInt("visibility");
                        String state = response.getString("name");

                        binding.progressBar.setVisibility(View.GONE);
                        binding.mainContainer.setVisibility(View.VISIBLE);
                        binding.temperature.setText(temperature + "°C");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
                        String formattedDate = dateFormat.format(new Date());
                        binding.dateDetails.setText(formattedDate);

                        binding.location.setText(state + ", " + country);
                        binding.weatherDescription.setText("Feels Like " + feelsLike + "°C - " + weatherMain);
                        binding.sunrise.setText(formattedSunrise + "AM");
                        binding.sunset.setText(formattedSunset + "PM");
                        binding.pressure.setText(pressure + "hPA");
                        binding.humidity.setText(humidity + "%");
                        binding.maxTemperature.setText(tempMax + "°C");
                        binding.visibility.setText(visibility + "m");
                    } catch (Exception e) {
                        handleError(e.getLocalizedMessage());
                    }
                },
                error -> handleError(getString(R.string.something_went_wrong_try_again_later))
        ));
    }

    private void handleError(String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
        binding.mainContainer.setVisibility(View.GONE);
        binding.errorTxt.setVisibility(View.VISIBLE);
        binding.errorTxt.setText(errorMessage);
    }
}