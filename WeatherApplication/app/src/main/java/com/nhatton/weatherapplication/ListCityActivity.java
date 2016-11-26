package com.nhatton.weatherapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;


public class ListCityActivity extends AppCompatActivity implements DataTransferInterface {
    public final static String[] CITY_LIST = {"Hanoi", "Ho Chi Minh City", "Da Lat", "Nha Trang", "Pleiku", "Bien Hoa"};
    public final static String[] COORDINATE_LIST =
            {"21.0278,101.6867", "10.8231,106.6297", "11.9404,108.4583", "12.2388,109.1967", "13.9718,108.0151", "10.9574,106.8427"};
    public final static int NUMBER_OF_CITY = CITY_LIST.length;
    private ListView mListView;
    private ArrayList<String> mSelectedCityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_list);

        mSelectedCityList = getIntent().getStringArrayListExtra("MAIN_LIST");

        if (mSelectedCityList == null) {
            mSelectedCityList = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_CITY; i++){
                mSelectedCityList.add(null);
            }
        }

        mListView = (ListView) findViewById(R.id.city_list);
        mListView.setAdapter(new CityAdapter(ListCityActivity.this, mSelectedCityList, this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.setAdapter(new CityAdapter(ListCityActivity.this, mSelectedCityList, this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backMain = new Intent();
                backMain.putStringArrayListExtra("RESULT_LIST", mSelectedCityList);
                setResult(RESULT_OK, backMain);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setValues(ArrayList<String> al) {
        mSelectedCityList = al;
    }
}
