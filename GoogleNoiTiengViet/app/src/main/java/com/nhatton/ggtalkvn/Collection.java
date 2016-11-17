package com.nhatton.ggtalkvn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class Collection extends AppCompatActivity {

    public ArrayList<Sound> mSounds = null;

    private SoundAdapter mAdapter = null;

    static MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_collection);

        mSounds = new ArrayList<>();

        Sound s = new Sound();
        s.setDescription("A hi hi");
        s.setSoundResourceId(R.raw.line_1);
        mSounds.add(s);

        s = new Sound();
        s.setDescription("Không có gì quý hơn độc lập tự do");
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

        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Context context = mContext;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sound s = mSounds.get(position);

                mMediaPlayer = MediaPlayer.create(context, s.getSoundResourceId());

                mMediaPlayer.start();
            }
        });


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;
        menu.add(0, info.position, 0, R.string.enter_fullscreen);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent fs_intent = new Intent(this, FullscreenActivity.class);
        String description = mSounds.get(item.getItemId()).getDescription();
        int soundId = mSounds.get(item.getItemId()).getSoundResourceId();
        Bundle extras = new Bundle();

        extras.putString("TEXT_TO_FULLSCREEN", description);
        extras.putInt("SOUND_TO_FULLSCREEN", soundId);

        fs_intent.putExtras(extras);
        startActivity(fs_intent);
        return super.onContextItemSelected(item);
    }

}
