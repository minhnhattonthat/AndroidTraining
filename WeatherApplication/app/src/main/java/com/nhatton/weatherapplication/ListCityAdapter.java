package com.nhatton.weatherapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

import static com.nhatton.weatherapplication.ListCityActivity.CITY_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.COORDINATE_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.NUMBER_OF_CITY;

class ListCityAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> mSelectedCityList;

    ListCityAdapter(Context context, ArrayList<String> selectedList) {
        mSelectedCityList = selectedList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return NUMBER_OF_CITY;
    }

    @Override
    public Object getItem(int i) {
        return COORDINATE_LIST[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListCityAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, parent, false);
            holder = new ListCityAdapter.ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.list_city_name);
            convertView.setTag(holder);
        } else {
            holder = (ListCityAdapter.ViewHolder) convertView.getTag();
        }
        holder.checkBox.setText(CITY_LIST[position]);
        if (mSelectedCityList.contains(COORDINATE_LIST[position])) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setTag(position);
        return convertView;
    }

    private class ViewHolder {
        CheckBox checkBox;
    }
}
