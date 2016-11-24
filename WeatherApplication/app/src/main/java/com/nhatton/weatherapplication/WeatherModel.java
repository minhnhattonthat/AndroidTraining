package com.nhatton.weatherapplication;

import java.util.Date;

class WeatherModel {
    private String mCityName = "";
    private double mTempC;
    private String mWeatherCondition = "";
    private Date mDate;
    private String mIconUrl;

    WeatherModel(String cityName, double tempC, String weatherCondition, String iconUrl) {
        mCityName = cityName;
        mTempC = tempC;
        mWeatherCondition = weatherCondition;
        mIconUrl = iconUrl;
    }

    String getLocation() {
        return mCityName;
    }

    public void setLocation(String cityName) {
        mCityName = cityName;
    }

    public String getWeatherCondidion() {
        return mWeatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        mWeatherCondition = weatherCondition;
    }

    double getTempC() {
        return mTempC;
    }

    public void setTempC(int tempC) {
        mTempC = tempC;
    }

    Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }
}
