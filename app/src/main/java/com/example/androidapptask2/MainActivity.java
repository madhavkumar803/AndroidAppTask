package com.example.androidapptask2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
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

public class MainActivity extends AppCompatActivity {

  public static final String LOG_TAG = MainActivity.class.getSimpleName();

  private static String url = "https://api.postalpincode.in/pincode/";
  private static  String  USGS_REQUEST_URL;

  EditText pincode;
  Button checkButton,btnRegister;
  TextView titleTextView,dateTextView,errorLog;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

     pincode = findViewById(R.id.edtTextPinCode);
     checkButton = findViewById(R.id.btnCheckPin);
     titleTextView = (TextView) findViewById(R.id.textView4);
     dateTextView = (TextView) findViewById(R.id.textView5);
     btnRegister = findViewById(R.id.register);
     errorLog = findViewById(R.id.errorLog);

    checkButton.setOnClickListener(new View.OnClickListener() {
      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
      public void onClick(View v) {
        if(pincode.getText().length()==6) {
          Toast.makeText(MainActivity.this, "Checking, please wait..", Toast.LENGTH_LONG).show();
          checkButton.setClickable(true);
          USGS_REQUEST_URL = url+pincode.getText().toString();
        EventAsyncTask task = new EventAsyncTask();
        task.execute();
        }
      }
    });
  }

  private void updateUi(Event event) {

    if(event.getState() != null) {
      titleTextView.setText("District : "+event.getDistic());
      dateTextView.setText("State : "+event.getState());
      btnRegister.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
          if(errorLog.getText().length()==0) {
            Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
            intent.putExtra("district",event.getDistic());
            startActivity(intent);
          } else {
            Toast.makeText(MainActivity.this, "Please enter valid pincode...", Toast.LENGTH_SHORT).show();
          }
        }
      });
    }
  }

  private class EventAsyncTask extends AsyncTask<URL, Void, Event> {

    @Override
    protected Event doInBackground(URL... urls) {
      String jsonResponse = "";
      URL url = createUrl(USGS_REQUEST_URL);

      try {
        jsonResponse = makeHttpRequest(url);
      } catch (IOException e) {
        Log.e(LOG_TAG, "Problem in making the HTTP request.", e);
      }
      Event event = extractFeatureFromJson(jsonResponse);
      return event;
    }

    @Override
    protected void onPostExecute(Event event) {
      if (event == null) {
        return;
      }
      updateUi(event);
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
        errorLog.setText("");
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

    private Event extractFeatureFromJson(String json) {
      if (TextUtils.isEmpty(json)) {
        return null;
      }

      try {
        JSONArray baseJsonResponse = new JSONArray(json);

        JSONObject featureArray = baseJsonResponse.getJSONObject(0);

        if (featureArray.length() > 0) {
          JSONArray firstFeature = featureArray.getJSONArray("PostOffice");
          JSONObject properties = firstFeature.getJSONObject(0);

          String district = properties.getString("District");
          String state = properties.getString("State");

          return new Event(district, state);
        }
      } catch (JSONException e) {
        errorLog.setText("Please enter valid pincode...");
        Log.e(LOG_TAG, "Problem parsing the JSON results", e);
      }
      return null;
    }
  }
}