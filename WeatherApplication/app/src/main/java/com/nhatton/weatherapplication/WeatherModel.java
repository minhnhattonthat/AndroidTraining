package com.nhatton.weatherapplication;

import android.graphics.Bitmap;

class WeatherModel {
    private String mCityName = "";
    private double mTempC;
    private String mWeatherCondition = "";
    private Bitmap mIcon;

    WeatherModel(String cityName, double tempC, String weatherCondition, Bitmap icon) {
        mCityName = cityName;
        mTempC = tempC;
        mWeatherCondition = weatherCondition;
        mIcon = icon;
    }

    String getLocation() {
        return mCityName;
    }

    String getWeatherCondidion() {
        return mWeatherCondition;
    }

    Bitmap getIcon() {
        return mIcon;
    }

    String getValueTempC() {
        return String.valueOf(Math.round(mTempC)) + "°C";
    }

}
