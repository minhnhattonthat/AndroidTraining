package com.nhatton.weatherapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static com.nhatton.weatherapplication.ListCityActivity.CITY_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.COORDINATE_LIST;
import static com.nhatton.weatherapplication.ListCityActivity.NUMBER_OF_CITY;

public class MainActivity extends AppCompatActivity {
    public static final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private final static String URL_ROOT = "http://api.apixu.com/v1/current.json?key=7f05b42fa8aa4c32b4364412162211&q=";
    private static final int GET_RESULT = 1;
    private static final int EDIT_LIST = 2;

    private ProgressDialog pDialog;
    private ArrayList<String> mSelectedCityList;
    private ArrayList<WeatherModel> mWeatherModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.main_today);
        long unixTime = System.currentTimeMillis();
        String date = df.format(new Date(unixTime));
        tv.setText(date);

        mSelectedCityList = new ArrayList<>();
        mWeatherModelList = new ArrayList<>();
        try {
            mSelectedCityList = (ArrayList<String>) readCachedFile(MainActivity.this, "selected_list");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Collection<String> c = (Collection) mSelectedCityList;
        if (mSelectedCityList.isEmpty() || Collections.frequency(c, null) == NUMBER_OF_CITY) {
            Intent getList = new Intent(MainActivity.this, ListCityActivity.class);
            startActivityForResult(getList, GET_RESULT);
        }else{
            new GetWeatherData().execute();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_list) {
            Intent editList = new Intent(this, ListCityActivity.class);
            editList.putExtra("MAIN_LIST", mSelectedCityList);
            startActivityForResult(editList, EDIT_LIST);
        }
        return super.onOptionsItemSelected(item);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_RESULT || requestCode == EDIT_LIST) {
            if (resultCode == RESULT_OK) {
                if(data.getStringArrayListExtra("RESULT_LIST")!= mSelectedCityList){
                    mSelectedCityList = data.getStringArrayListExtra("RESULT_LIST");
                    mWeatherModelList = new ArrayList<>();
                    new GetWeatherData().execute();
                }
                else{
                    ListView listView = (ListView) findViewById(R.id.main_list);
                    listView.setAdapter(new MainAdapter(MainActivity.this, mWeatherModelList));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class GetWeatherData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();

            for (int i = 0; i < NUMBER_OF_CITY; i++) {
                if (mSelectedCityList.get(i) != null) {
                    String url = URL_ROOT + mSelectedCityList.get(i);

                    String jsonStr = sh.makeServiceCall(url);

                    Log.e("MainActivity", "Response from url: " + jsonStr);

                    if (jsonStr != null) {
                        try {
                            JSONObject weatherData = new JSONObject(jsonStr);

                            String cityName = getCityName(mSelectedCityList.get(i));

                            JSONObject current = weatherData.getJSONObject("current");
                            double tempC = current.getDouble("temp_c");

                            JSONObject condition = current.getJSONObject("condition");
                            String text = condition.getString("text");

                            String icon_url = "http:" + condition.getString("icon");
                            Bitmap icon = null;
                            try {
                                icon = BitmapFactory.
                                        decodeStream((InputStream) new URL(icon_url).getContent());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mWeatherModelList.add(new WeatherModel(cityName, tempC, text, icon));
                        } catch (final JSONException e) {
                            Log.e("MainActivity", "Json parsing error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            });

                        }
                    } else {
                        Log.e("MainActivity", "Couldn't get json from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server. Check LogCat for possible errors!",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            ListView listView = (ListView) findViewById(R.id.main_list);
            listView.setAdapter(new MainAdapter(MainActivity.this, mWeatherModelList));
            registerForContextMenu(listView);
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
}