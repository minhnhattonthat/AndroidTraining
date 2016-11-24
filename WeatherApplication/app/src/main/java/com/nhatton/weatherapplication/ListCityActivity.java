package com.nhatton.weatherapplication;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class ListCityActivity extends AppCompatActivity implements DataTransferInterface {
    public final static String[] CITY_LIST = {"Hanoi", "Ho Chi Minh City", "Da Lat", "Nha Trang"};
    public final static String[] COORDINATE_LIST =
            {"21.0278,101.6867", "10.8231,101.6867", "11.9404,101.6867", "12.2388,101.6867"};

    private ListView mListView;
    private ArrayList<String> mSelectedCityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_list);
        if (getIntent().getStringArrayListExtra("MAIN_LIST") != null) {
            mSelectedCityList = getIntent().getStringArrayListExtra("MAIN_LIST");
        }else{
            mSelectedCityList = new ArrayList<>();
        }

        mListView = (ListView) findViewById(R.id.city_list);
        mListView.setAdapter(new CityAdapter(ListCityActivity.this, mSelectedCityList,this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.setAdapter(new CityAdapter(ListCityActivity.this, mSelectedCityList,this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backMain = new Intent(this,MainActivity.class);
                    backMain.putStringArrayListExtra("RESULT_LIST", mSelectedCityList);
                startActivity(backMain);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setValues(ArrayList<String> al) {
        mSelectedCityList = al;
    }
}
