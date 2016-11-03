package com.example.bioapplication;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class MainActivity extends Activity {

    private static final String[] genderList = new String[]{
            "Male", "Female", "N/A"
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                genderList);
        AutoCompleteTextView genderTextView = (AutoCompleteTextView)
                findViewById(R.id.gender);
        genderTextView.setAdapter(genderAdapter);
    }
}