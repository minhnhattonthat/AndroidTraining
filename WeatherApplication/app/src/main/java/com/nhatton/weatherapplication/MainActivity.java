package com.nhatton.weatherapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static com.nhatton.weatherapplication.ListCityActivity.CITY_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.COORDINATE_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.NUMBER_OF_CITY;

public class MainActivity extends AppCompatActivity {
    public static final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final static String URL_ROOT = "http://api.apixu.com/v1/current.json?key=7f05b42fa8aa4c32b4364412162211&q=";
    private static final int TO_EDIT_LIST = 1;
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_COMPLETE = 3;
    private HttpHandler sh = new HttpHandler();

    private ArrayList<String> mSelectedCityList;
    private ArrayList<String> cloneSelectedCityList;
    private ArrayList<WeatherModel> mWeatherModelList;
    private SwipeRefreshLayout swipeContainer;
    private ListView mListView;
    private MainAdapter mMainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.main_today);
        long unixTime = System.currentTimeMillis();
        String date = df.format(new Date(unixTime));
        tv.setText(date);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorAccent);

        mWeatherModelList = new ArrayList<>();

        ArrayList<String> temp = new ArrayList<>();

        try {
            temp = (ArrayList<String>) readCachedFile(MainActivity.this, "selected_list");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (temp == null || temp.size() == 0) {
            mSelectedCityList = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_CITY; i++) {
                mSelectedCityList.add(null);
            }
        } else {
            mSelectedCityList = temp;
        }
        cloneSelectedCityList = mSelectedCityList;
        Collection<String> c = mSelectedCityList;
        if (Collections.frequency(c, null) == NUMBER_OF_CITY) {
            Intent getList = new Intent(MainActivity.this, ListCityActivity.class);
            startActivityForResult(getList, TO_EDIT_LIST);
        } else {
            mListView = (ListView) findViewById(R.id.main_list);
            mMainAdapter = new MainAdapter(MainActivity.this, mWeatherModelList);
            mListView.setAdapter(mMainAdapter);
            new Thread(new GetAllData()).start();
        }
    }

    class GetAllData implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            for (int i = 0; i < NUMBER_OF_CITY; i++) {
                if (parseToWeatherModel(i) != null) {
                    mWeatherModelList.add(parseToWeatherModel(i));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMainAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        WeatherModel parseToWeatherModel(int i) {
            if (cloneSelectedCityList.get(i) != null) {
                String url = URL_ROOT + cloneSelectedCityList.get(i);
                String jsonStr = sh.makeServiceCall(url);
                Log.e(TAG, "Response from url: " + jsonStr);
                if (jsonStr != null) {
                    try {
                        JSONObject weatherData = new JSONObject(jsonStr);

                        String cityName = getCityName(cloneSelectedCityList.get(i));

                        JSONObject current = weatherData.getJSONObject("current");
                        double tempC = current.getDouble("temp_c");

                        JSONObject condition = current.getJSONObject("condition");
                        String text = condition.getString("text");

                        return new WeatherModel(cityName, tempC, text);
                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            return null;
        }
    }

    private void refreshList() {
        mWeatherModelList = new ArrayList<>();
        cloneSelectedCityList = mSelectedCityList;
        new Thread(new GetAllData()).start();//TODO: switch to thread
        swipeContainer.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        try {
            createCachedFile(MainActivity.this, "selected_list", mSelectedCityList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_list) {
            Intent editList = new Intent(this, ListCityActivity.class);
            editList.putExtra("MAIN_LIST", mSelectedCityList);
            startActivityForResult(editList, TO_EDIT_LIST);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mListView = (ListView) findViewById(R.id.main_list);
        mMainAdapter = new MainAdapter(MainActivity.this, mWeatherModelList);
        mListView.setAdapter(mMainAdapter);
        if (requestCode == TO_EDIT_LIST && resultCode == RESULT_OK) {
            ArrayList<String> resultList = data.getStringArrayListExtra("RESULT_LIST");
            if (!resultList.equals(mSelectedCityList)) {
                for (int i = 0; i < NUMBER_OF_CITY; i++) {
                    if (Objects.equals(resultList.get(i), mSelectedCityList.get(i))) {
                        if (mSelectedCityList.get(i) != null) {
                            cloneSelectedCityList.set(i, null);
                        }
                    } else if (resultList.get(i) == null) {
                        removeWeatherModel(i);
                        mMainAdapter.notifyDataSetChanged();
                        cloneSelectedCityList.set(i, null);
                    } else {
                        cloneSelectedCityList.set(i, resultList.get(i));
                    }
                }
                mSelectedCityList = resultList;
                new Thread(new GetAllData()).start();//TODO: change  to using thread
            } else {
                mMainAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private static void createCachedFile(Context context, String fileName, ArrayList<String> content) throws IOException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(content);
        oos.close();
        fos.close();
    }

    public static Object readCachedFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }

    private String getCityName(String coordinate) {
        return Arrays.asList(CITY_LIST).get(Arrays.asList(COORDINATE_LIST).lastIndexOf(coordinate));
    }

    private void removeWeatherModel(int position) {
        for (int i = 0; i < mWeatherModelList.size(); i++) {
            if (Objects.equals(mWeatherModelList.get(i).getLocation(), CITY_LIST[position])) {
                mWeatherModelList.remove(mWeatherModelList.get(i));
            }
        }
    }
}