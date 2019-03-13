package com.zhopa.alina.weatherviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private List<Weather> weatherList = new ArrayList<>();
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherListView = findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText locationEditText = findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());

                if (url != null) {
                    dismissKeyboard(locationEditText);
                    GetWeatherTask task = new GetWeatherTask();
                    task.execute(url);
                } else
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url,
                            Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void dismissKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
    }

    private URL createURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            String stringUrl = baseUrl + URLEncoder.encode(city, "UTF-8")
                    + "&units=metric&cnt=25&APPID=" + apiKey; // imperial - F, metric - C, standard - K, cnt - q.days
            return new URL(stringUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))){
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (Exception e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.read_error,
                                Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                } else Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error,
                        Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.connect_error,
                        Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            convertJSONtoArrayList(jsonObject);
            weatherArrayAdapter.notifyDataSetChanged();
            weatherListView.smoothScrollToPosition(0);
        }

        private void convertJSONtoArrayList(JSONObject forecast) {
            weatherList.clear();

            try {
                if (forecast != null) {
                    JSONArray list = forecast.getJSONArray("list");
                    System.out.println(forecast.toString());
                    handleDailyTemperatures(list);

                    for (int i=0; i<list.length(); ++i) {
                        JSONObject day = list.getJSONObject(i);
                        JSONObject temperatures = day.getJSONObject("main");
                        JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
                        //System.out.println("list ["+i+"] day = "+day.getLong("dt")
                        //        +". "+day.getString("dt_txt"));

                        weatherList.add(new Weather(
                                day.getLong("dt"),
                                temperatures.getDouble("temp_min"),
                                temperatures.getDouble("temp_max"),
                                temperatures.getDouble("humidity"),
                                weather.getString("description"),
                                getResources().getString(R.string.icon_url_placeholder, weather.getString("icon"))
                        ));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private HashMap<Integer, Double[]> handleDailyTemperatures(JSONArray temps) {
            int today = (int) (Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() / 86400000);
            // min max bounds in millis
            HashMap<Long, Double> tempsMap = new HashMap<>(); // TODO check out docs for SparseArrays
            HashMap<Integer, Double[]> dailyTemps = new HashMap<>();

            System.out.println("time in millis: "+Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
            for (int i=0; i<temps.length(); ++i) {
                try {
                    JSONObject hour = temps.getJSONObject(i);
                    double tempDouble = hour.getJSONObject("main").getDouble("temp");
                    tempsMap.put(hour.getLong("dt"), tempDouble);
                    System.out.println("tempsMap["+hour.getLong("dt")+"] = "+tempDouble);
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.print(" at list for i="+i);
                }
            }

            for (int i=1; i<=5; i++) {
                double minTemp = 0.0, maxTemp = 0.0;
                long dayStart = (today+i)*86400000;
                long dayEnd = dayStart + 86399999;
                System.out.println("today = "+today+"dayStart = "+dayStart+". dayEnd = "+dayEnd);

                for (long key : tempsMap.keySet()) {
                    if (key>=dayStart && key<=dayEnd) {
                        double test = tempsMap.get(key);
                        minTemp = (test < minTemp ? test : minTemp);
                        maxTemp = (test > maxTemp ? test : maxTemp);
                    }
                }
                dailyTemps.put(i, new Double[] {minTemp, maxTemp});
            }

            for (int d=1; d<=dailyTemps.keySet().size(); d++) {
                System.out.println("dailyTemps["+d+"] = "+Arrays.toString(dailyTemps.get(d)));
            }

            return dailyTemps;
        }
    }
}
