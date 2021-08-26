package com.example.androidapptask2;

public class EventWeather {
  String centigradeTem;
  String fahrenheitTemp;
  String latitude;
  String longitude;

  public EventWeather(String centigradeTem, String fahrenheitTemp, String latitude, String longitude) {
    this.centigradeTem = centigradeTem;
    this.fahrenheitTemp = fahrenheitTemp;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public String getCentigradeTem() {
    return centigradeTem;
  }

  public String getFahrenheitTemp() {
    return fahrenheitTemp;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }
}
