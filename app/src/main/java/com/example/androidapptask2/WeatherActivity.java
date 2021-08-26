package com.example.androidapptask2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class WeatherActivity extends AppCompatActivity {


  public static final String LOG_TAG = "WeatherActivity";
  EditText cityName;
  Button showResult;
  TextView centigradeTem,fahrenheitTemp,latitude,longitude,errorMsg;
  private String USGS_REQUEST_URL = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather);

    cityName = findViewById(R.id.editTextTextPersonName2);
    showResult = findViewById(R.id.button);
    centigradeTem = (TextView) findViewById(R.id.textView6);
    fahrenheitTemp = (TextView) findViewById(R.id.textView7);
    latitude = (TextView) findViewById(R.id.textView8);
    longitude = (TextView) findViewById(R.id.textView9);
    errorMsg = findViewById(R.id.errorMsg);
    Bundle extras = getIntent().getExtras();
    cityName.setText(extras.getString("district"));
    showResult.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        if (cityName.getText().length() > 0) {
          Toast.makeText(WeatherActivity.this, "Checking, please wait..", Toast.LENGTH_SHORT).show();
          centigradeTem.setText("Temperature in Centigrade : ");
          fahrenheitTemp.setText("Temperature in Fahrenheit : ");
          latitude.setText("Latitude: ");
          longitude.setText("Longitude: ");
          USGS_REQUEST_URL = "https://api.weatherapi.com/v1/current.json?key=35c9f92ac5bf4df0811144140212307&q=" + cityName.getText().toString() + "&aqi=no";
          WeatherAsyncTask task = new WeatherAsyncTask();
          task.execute();
        }
      }
    });
  }

  private void updateUi(EventWeather eventWeather) {
    if(eventWeather !=null) {
      errorMsg.setText("");
      centigradeTem.setText("Temperature in Centigrade : " + eventWeather.getCentigradeTem());
      fahrenheitTemp.setText("Temperature in Fahrenheit : " + eventWeather.getFahrenheitTemp());
      latitude.setText("Latitude: " + eventWeather.getLatitude());
      longitude.setText("Longitude: " + eventWeather.getLongitude());
    } else {
      Toast.makeText(this, "Please enter valid pincode...", Toast.LENGTH_SHORT).show();
    }

  }

  private class WeatherAsyncTask extends AsyncTask<URL, Void, EventWeather> {

    @Override
    protected EventWeather doInBackground(URL... urls) {
      String jsonResponse = "";
      URL url = createUrl(USGS_REQUEST_URL);
      try {
        jsonResponse = makeHttpRequest(url);
      } catch (IOException e) {
        Log.e(LOG_TAG, "Problem in making the HTTP request.", e);
      }
      EventWeather eventWeather = extractFeatureFromJson(jsonResponse);
      return eventWeather;
    }

    @Override
    protected void onPostExecute(EventWeather eventWeather) {
      if (eventWeather == null) {
        return;
      }
      updateUi(eventWeather);
    }

    private URL createUrl(String stringUrl) {

      URL url = null;
      try {
        url = new URL(stringUrl);
      } catch (MalformedURLException e) {
        Log.e(LOG_TAG, "Error with creating URL", e);
        return null;
      }
      return url;
    }

    private String makeHttpRequest(URL url) throws IOException {

      String jsonResponse = "";
      if (url == null) {
        return jsonResponse;
      }
      HttpURLConnection urlConnection = null;
      InputStream inputStream = null;
      try {
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.connect();
        if (urlConnection.getResponseCode() == 200) {
          inputStream = urlConnection.getInputStream();
          jsonResponse = readFromStream(inputStream);
        } else {
          errorMsg.setText("Please inter valid name...");
          Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
        }
      } catch (IOException e) {
        Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
      } finally {
        if (urlConnection != null) {
          urlConnection.disconnect();
        }
        if (inputStream != null) {
          inputStream.close();
        }
      }
      return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
      StringBuilder output = new StringBuilder();
      if (inputStream != null) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
          output.append(line);
          line = reader.readLine();
        }
      }
      return output.toString();
    }

    private EventWeather extractFeatureFromJson(String json) {
      if (TextUtils.isEmpty(json)) {
        return null;
      }

      try {
        JSONObject a = new JSONObject(json);
        JSONObject a1 = a.getJSONObject("location");
        JSONObject a2 = a.getJSONObject("current");
        String latitude = a1.getString("lat");
        String longitude = a1.getString("lon");
        String centigradeTem = a2.getString("temp_c");
        String fahrenheitTemp = a2.getString("temp_f");
        return new EventWeather(centigradeTem, fahrenheitTemp, latitude, longitude);
      } catch (JSONException e) {
        Log.e(LOG_TAG, "Problem parsing the JSON results", e);
      }
      return null;
    }
  }
}
