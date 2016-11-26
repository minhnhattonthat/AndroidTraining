package com.nhatton.weatherapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class MainAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<WeatherModel> mWeatherModelList;
    private LayoutInflater mInflater;

    MainAdapter(Context context, ArrayList<WeatherModel> weatherModelArrayList) {
        mContext = context;
        mWeatherModelList = weatherModelArrayList;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mWeatherModelList.size();
    }

    @Override
    public Object getItem(int i) {
        return mWeatherModelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MainAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_row, parent, false);

            holder = new MainAdapter.ViewHolder();
            holder.cityName = (TextView) convertView.findViewById(R.id.main_city_name);
            holder.tempC = (TextView) convertView.findViewById(R.id.main_temp_c);
            holder.iconWeather = (ImageView) convertView.findViewById(R.id.main_weather_icon);
            holder.weatherCondition = (TextView) convertView.findViewById(R.id.main_weather_condition);

            convertView.setTag(holder);
        } else {
            holder = (MainAdapter.ViewHolder) convertView.getTag();
        }
        WeatherModel element = mWeatherModelList.get(position);
        holder.cityName.setText(element.getLocation());
        holder.tempC.setText(element.getValueTempC());
        holder.iconWeather.setImageBitmap(element.getIcon());
        holder.weatherCondition.setText(element.getWeatherCondidion());

        return convertView;
    }

    private class ViewHolder {
        TextView cityName;
        TextView tempC;
        ImageView iconWeather;
        TextView weatherCondition;
    }
}
