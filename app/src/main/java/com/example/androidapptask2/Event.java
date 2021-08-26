package com.example.androidapptask2;


public class Event {

  /** Title of the earthquake event */
  public  String Distic;
  public  String State;
  public Event() {}
  public Event(String distic, String state) {
    Distic = distic;
    State = state;
  }

  public String getDistic() {
    return Distic;
  }

  public String getState() {
    return State;
  }
}