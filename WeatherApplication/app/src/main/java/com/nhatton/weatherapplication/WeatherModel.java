package com.nhatton.weatherapplication;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

class WeatherModel implements Parcelable{
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

    WeatherModel(String cityName, double tempC, String weatherCondition) {
        mCityName = cityName;
        mTempC = tempC;
        mWeatherCondition = weatherCondition;
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mCityName);
        out.writeDouble(mTempC);
        out.writeString(mWeatherCondition);
        out.writeParcelable(mIcon,flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private WeatherModel(Parcel in) {
        mCityName = in.readString();
        mTempC = in.readDouble();
        mWeatherCondition = in.readString();
        mIcon = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<WeatherModel> CREATOR = new Creator<WeatherModel>() {
        @Override
        public WeatherModel createFromParcel(Parcel in) {
            return new WeatherModel(in);
        }

        @Override
        public WeatherModel[] newArray(int size) {
            return new WeatherModel[size];
        }
    };

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
