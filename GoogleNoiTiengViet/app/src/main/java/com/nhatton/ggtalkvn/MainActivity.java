package com.nhatton.ggtalkvn;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Sound> mSounds = null;
    private SoundAdapter mAdapter = null;
    static MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSounds = new ArrayList<>();

        Sound s = new Sound();

        s.setDescription("A hi hi");
        s.setSoundResourceId(R.raw.line_1);

        mSounds.add(s);

        s = new Sound();
        s.setDescription("Không có gì quý hơn độc lập tự do và hạnh phúc nữa a hi hi");
        s.setSoundResourceId(R.raw.line_2);

        mSounds.add(s);

        s = new Sound();
        s.setDescription("Chúng ta không thuộc về nhau");
        s.setSoundResourceId(R.raw.line_3);

        mSounds.add(s);
        mAdapter = new SoundAdapter(this, R.layout.list_row, mSounds);
        final Context mContext = this;
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            Context context= mContext;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Sound s = mSounds.get(position);

                mMediaPlayer = MediaPlayer.create(context,s.getSoundResourceId());

                mMediaPlayer.start();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_theme) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
