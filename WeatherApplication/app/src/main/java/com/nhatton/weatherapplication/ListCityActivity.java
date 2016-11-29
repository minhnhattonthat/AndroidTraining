package com.nhatton.weatherapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;


public class ListCityActivity extends AppCompatActivity {
    public final static String[] CITY_LIST = {"Bac Lieu", "Bac Ninh", "Ben Tre", "Bien Hoa",
            "Buon Ma Thuot", "Can Tho", "Da Lat", "Da Nang", "Ha Long", "Hai Duong", "Hai Phong",
            "Hanoi", "Ho Chi Minh City", "Hue", "Long Xuyen", "My Tho", "Nam Dinh", "Nha Trang",
            "Pleiku", "Phan Thiet", "Phu Quoc", "Quang Ngai", "Quy Nhon", "Rach Gia", "Thai Binh",
            "Thai Nguyen", "Thanh Hoa", "Thu Dau Mot", "Vinh", "Vung Tau"};
    public final static String[] COORDINATE_LIST = {"9.2573,105.7558", "21.1214,106.1111",
            "10.1082,106.4406", "10.9574,106.8427", "12.6847,108.0509", "10.0324,105.7841",
            "11.9404,108.4583", "16.0544,108.2022", "20.8733,107.0897", "20.9373,106.3146",
            "20.8449,106.6881", "10.8231,106.6297", "16.4498,107.5624", "10.3728,105.4258",
            "10.3522,106.3669", "20.4388,106.1621", "12.2388,109.1967", "13.9718,108.0151",
            "10.9805,108.2615", "10.2899,103.9840", "15.1205,108.7923", "13.7763,109.2233",
            "10.0264,105.1056", "20.4463,106.3366", "21.5672,105.8252", "20.1410,105.3094",
            "11.0003,106.6489", "18.3800,105.5600", "20.9655,107.0731", "21.0278,101.6867"};
    public final static int NUMBER_OF_CITY = CITY_LIST.length;

    private ArrayList<String> mSelectedCityList;
    private ListView mListView;
    private ListCityAdapter mListCityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getStringArrayListExtra("MAIN_LIST") == null) {
            mSelectedCityList = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_CITY; i++) {
                mSelectedCityList.add(null);
            }
        } else {
            mSelectedCityList = getIntent().getStringArrayListExtra("MAIN_LIST");
        }

        mListView = (ListView) findViewById(R.id.city_list);
        mListCityAdapter = new ListCityAdapter(ListCityActivity.this, mSelectedCityList);
        mListView.setAdapter(mListCityAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long index) {
                CheckBox box = (CheckBox) view.findViewById(R.id.list_city_name);
                if (mSelectedCityList.get((int) index) == null) {
                    mSelectedCityList.set((int) index, COORDINATE_LIST[(int) index]);
                    box.setChecked(true);
                } else {
                    mSelectedCityList.set((int) index, null);
                    box.setChecked(false);
                }
            }
        });
        mListView.setOnItemSelectedListener(null);
        registerForContextMenu(mListView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_select_all_list:
                for(int i = 0; i<NUMBER_OF_CITY; i++){
                    mSelectedCityList.set(i,COORDINATE_LIST[i]);
                    View rowView = mListView.getAdapter().getView(i,null,mListView);
                    CheckBox box = (CheckBox) rowView.findViewById(R.id.list_city_name);
                    box.setChecked(true);
                }
                mListCityAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_select_none_list:
                for(int i = 0; i<NUMBER_OF_CITY; i++){
                    mSelectedCityList.set(i,null);
                    View rowView = mListView.getAdapter().getView(i,null,mListView);
                    CheckBox box = (CheckBox) rowView.findViewById(R.id.list_city_name);
                    box.setChecked(false);
                }
                mListCityAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent backMain = new Intent();
        backMain.putStringArrayListExtra("RESULT_LIST", mSelectedCityList);
        setResult(RESULT_OK, backMain);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }
}