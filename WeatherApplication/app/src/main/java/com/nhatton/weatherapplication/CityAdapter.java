package com.nhatton.weatherapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

import static com.nhatton.weatherapplication.ListCityActivity.CITY_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.COORDINATE_LIST;

class CityAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mSelectedCityList;
    public DataTransferInterface mDTF;

    CityAdapter(Context context, ArrayList<String> selectedList, DataTransferInterface dTF) {
        mSelectedCityList = selectedList;
        mDTF= dTF;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return CITY_LIST.length;
    }

    @Override
    public Object getItem(int i) {
        return CITY_LIST[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CityAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, parent, false);

            holder = new CityAdapter.ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.list_city_name);

            convertView.setTag(holder);
        } else {
            holder = (CityAdapter.ViewHolder) convertView.getTag();
        }
        holder.checkBox.setText(CITY_LIST[position]);

        if (mSelectedCityList.contains(CITY_LIST[position])) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setTag(position);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                int index = (int) compoundButton.getTag();
                String city_coordinate = COORDINATE_LIST[index];
                if (checked) {
                    mSelectedCityList.add(city_coordinate);
                } else {
                    mSelectedCityList.remove(city_coordinate);
                }
                mDTF.setValues(mSelectedCityList);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        CheckBox checkBox;
    }

}
