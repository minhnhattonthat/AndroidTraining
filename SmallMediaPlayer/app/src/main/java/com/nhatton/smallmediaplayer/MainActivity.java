package com.nhatton.smallmediaplayer;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MediaPlayer";
    private final String MEDIA_PATH = Environment.
            getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getAbsolutePath();
    private int LAST_SONG_POSITION = -1;

    private MediaPlayer mMediaPlayer;
    private MenuItem playButton;
    private String songPath = "";
    private int CURRENT_TIME_PLAYED = 0;
    private ListView listView;

    private ArrayList<HashMap<String, String>> songList = new ArrayList<>();
    private FilenameFilter songFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return (s.endsWith(".mp3") || s.endsWith(".MP3") || s.endsWith(".wav") || s.endsWith(".WAV"));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar mToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolBar);

        mMediaPlayer = new MediaPlayer();

        songList = getSongList();

        listView = (ListView) findViewById(R.id.list_music);
        listView.setAdapter(new SimpleAdapter(this, songList, R.layout.playlist_item,
                new String[]{"songTitle"}, new int[]{R.id.song_title}));
        listView.setTextFilterEnabled(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(selectSongListener);
    }

    private AdapterView.OnItemClickListener selectSongListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            listView.setItemChecked(position, true);
            songPath = songList.get(position).get("songPath");

            if(LAST_SONG_POSITION != position) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                try {
                    mMediaPlayer.setDataSource(songPath);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                LAST_SONG_POSITION = position;
            }else if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                CURRENT_TIME_PLAYED = mMediaPlayer.getCurrentPosition();
            }else{
                mMediaPlayer.seekTo(CURRENT_TIME_PLAYED);
                mMediaPlayer.start();
            }
            invalidateOptionsMenu();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        playButton = menu.findItem(R.id.button_play);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        playButton = menu.findItem(R.id.button_play);
        if(mMediaPlayer.isPlaying()){
            playButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
        }else{
            playButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_play:
                if (Objects.equals(songPath, "")) {
                    Toast.makeText(this, "Please choose a song", Toast.LENGTH_LONG).show();
                } else if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    CURRENT_TIME_PLAYED = mMediaPlayer.getCurrentPosition();
                    playButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
                } else {
                    mMediaPlayer.seekTo(CURRENT_TIME_PLAYED);
                    mMediaPlayer.start();
                    playButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<HashMap<String, String>> getSongList() {
        File directory = new File(MEDIA_PATH);

        if (directory.listFiles(songFilter).length > 0) {
            for (File file : directory.listFiles(songFilter)) {
                HashMap<String, String> song = new HashMap<>();
                song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("songPath", file.getPath());
                songList.add(song);
            }
        }

        return songList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }
}
