package com.nhatton.weatherapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.nhatton.weatherapplication.ListCityActivity.CITY_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.COORDINATE_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.NUMBER_OF_CITY;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity {
    public static final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

    private static final int TO_EDIT_LIST = 1;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String URL_ROOT =
            "http://api.apixu.com/v1/current.json?key=880f16ebddb0426498065011161412&q=";
    private static final int NUMBER_OF_CORE_THREAD = 30;

    private HttpHandler sh = new HttpHandler();

    private BlockingQueue workQueue = new ArrayBlockingQueue(30);
    private ThreadPoolExecutor threadPoolExecutor;
    private Thread[] fetchDataAt = new Thread[30];

    private Handler handler = new Handler();

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

        //Set time of today
        TextView tv = (TextView) findViewById(R.id.main_today);
        long unixTime = System.currentTimeMillis();
        String date = df.format(new Date(unixTime));
        tv.setText(date);

        //Set swipe to refresh list
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorAccent);

        //Set mWeatherModelList with 30 null items
        mWeatherModelList = new ArrayList<>();

        //Recall cache of mSelectedCityList. If null or have no items, set list of 30 null items
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

        //Create a clone of mSelectedCityList to add checked items and remove unchecked items
        cloneSelectedCityList = mSelectedCityList;

        //If mSelectedCityList don't have any item, go to list to choose, else get data
        Collection<String> c = mSelectedCityList;
        if (Collections.frequency(c, null) == NUMBER_OF_CITY) {
            Intent getList = new Intent(MainActivity.this, ListCityActivity.class);
            startActivityForResult(getList, TO_EDIT_LIST);
        } else {
            mListView = (ListView) findViewById(R.id.main_list);
            mMainAdapter = new MainAdapter(MainActivity.this, mWeatherModelList);
            mListView.setAdapter(mMainAdapter);
            fetchData();
        }
    }

    private void fetchData() {
        final long startTime = currentTimeMillis();
        threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORE_THREAD, 30, 1, TimeUnit.SECONDS, workQueue);

        for (int i = 0; i < NUMBER_OF_CITY; i++) {
            final int position = i;
            fetchDataAt[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (cloneSelectedCityList.get(position) != null) {
                        String url = URL_ROOT + cloneSelectedCityList.get(position);
                        String jsonStr = sh.makeServiceCall(url);
                        Log.e(TAG, "Response from url: " + jsonStr);
                        if (jsonStr != null) {
                            try {
                                JSONObject weatherData = new JSONObject(jsonStr);

                                String cityName = getCityName(cloneSelectedCityList.get(position));

                                JSONObject current = weatherData.getJSONObject("current");
                                double tempC = current.getDouble("temp_c");

                                JSONObject condition = current.getJSONObject("condition");
                                String text = condition.getString("text");

                                mWeatherModelList.add(new WeatherModel(cityName, tempC, text));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMainAdapter.notifyDataSetChanged();
                                    }
                                });
                            } catch (final JSONException e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Json parsing error: "
                                                + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            Log.i(String.valueOf(NUMBER_OF_CORE_THREAD)+": FINISH THREAD " + String.valueOf(position),
                                    String.valueOf(currentTimeMillis() - startTime));
                        } else {
                            Log.e(TAG, "Couldn't get json from server.");
                        }
                    }
                }
            });
            threadPoolExecutor.execute(fetchDataAt[i]);
        }
    }

    private void refreshList() {
        cloneSelectedCityList = mSelectedCityList;
        fetchData();//TODO: switch to thread
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
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_list) {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
            }
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
        //Compare between the list before and after activity for result
        if (requestCode == TO_EDIT_LIST && resultCode == RESULT_OK) {
            ArrayList<String> resultList = data.getStringArrayListExtra("RESULT_LIST");
            if (!resultList.equals(mSelectedCityList)) {
                for (int i = 0; i < NUMBER_OF_CITY; i++) {
                    if (Objects.equals(resultList.get(i), mSelectedCityList.get(i))) {
                        if (mSelectedCityList.get(i) != null) {
                            cloneSelectedCityList.set(i, null);
                        }
                    } else if (resultList.get(i) == null) {
                        cloneSelectedCityList.set(i, null);
                        removeWeatherModel(i);
                    } else {
                        cloneSelectedCityList.set(i, resultList.get(i));
                    }
                }
                mSelectedCityList = resultList;
                fetchData();
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

    private static void createCachedFile(Context context, String fileName, ArrayList<String> content)
            throws IOException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(content);
        oos.close();
        fos.close();
    }

    private static Object readCachedFile(Context context, String fileName)
            throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return ois.readObject();
    }

    private String getCityName(String coordinate) {
        return Arrays.asList(CITY_LIST).get(Arrays.asList(COORDINATE_LIST).lastIndexOf(coordinate));
    }

    private void removeWeatherModel(int position) {
        for (int i = 0; i < mWeatherModelList.size(); i++) {
            if (Objects.equals(mWeatherModelList.get(i).getLocation(), CITY_LIST[position])) {//TODO: fix removing
                mWeatherModelList.remove(mWeatherModelList.get(i));
            }
        }
    }

    private String getTime(long millis) {
        return String.format(Locale.getDefault(), "%d:%d.%d",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes
                                (TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds
                                (TimeUnit.MILLISECONDS.toMinutes(millis)),
                TimeUnit.MILLISECONDS.toMillis(millis) -
                        TimeUnit.SECONDS.toMillis
                                (TimeUnit.MILLISECONDS.toSeconds(millis)));
    }
}
