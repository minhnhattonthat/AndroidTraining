package com.nhatton.smallmediaplayer;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MediaPlayer";
    private static final String MEDIA_PATH = Environment.
            getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getAbsolutePath();
    private int LAST_SONG_POSITION = -1;
    private MediaPlayer mMediaPlayer;
    private MenuItem playButton;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView endTime;
    private String mSongPath = "";
    private ListView listView;

    private ArrayList<HashMap<String, String>> songList = new ArrayList<>();
    private FilenameFilter songFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return (s.endsWith(".mp3") || s.endsWith(".MP3") || s.endsWith(".wav")
                    || s.endsWith(".WAV"));
        }
    };

    private Handler musicHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar mToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolBar);

        songList = getSongList();

        listView = (ListView) findViewById(R.id.list_music);
        listView.setAdapter(new SimpleAdapter(this, songList, R.layout.playlist_item,
                new String[]{"songTitle"}, new int[]{R.id.song_title}));
        listView.setTextFilterEnabled(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(selectSongListener);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                LAST_SONG_POSITION++;
                mSongPath = songList.get(LAST_SONG_POSITION).get("songPath");
                playAudio(mSongPath);
            }
        });

        currentTime = (TextView) findViewById(R.id.start_time);
        currentTime.setText(R.string.default_start_time);
        endTime = (TextView) findViewById(R.id.end_time);
        endTime.setText(R.string.default_end_time);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (LAST_SONG_POSITION >= 0 && fromUser) {
                    currentTime.setText(getTextFromDuration(progress * 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mMediaPlayer.pause();
                musicHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicHandler.removeCallbacks(mUpdateTimeTask);
                mMediaPlayer.seekTo(seekBar.getProgress() * 1000);
                mMediaPlayer.start();
                updateProgressBar();
            }
        });

    }

    private AdapterView.OnItemClickListener selectSongListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    listView.setItemChecked(position, true);
                    mSongPath = songList.get(position).get("songPath");

                    if (LAST_SONG_POSITION != position) {
                        mMediaPlayer.stop();
                        LAST_SONG_POSITION = position;
                        playAudio(mSongPath);
                    } else if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    } else {
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
        if (mMediaPlayer.isPlaying()) {
            playButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
        } else {
            playButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_play:
                if (Objects.equals(mSongPath, "")) {
                    Toast.makeText(this, "Please choose a song", Toast.LENGTH_LONG).show();
                } else if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    playButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
                } else {
                    mMediaPlayer.start();
                    playButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
                }
                break;
            case R.id.button_next:
                listView.setItemChecked(LAST_SONG_POSITION, false);
                LAST_SONG_POSITION++;
                mSongPath = songList.get(LAST_SONG_POSITION).get("songPath");
                playAudio(mSongPath);
                break;
            case R.id.button_previous:
                if (LAST_SONG_POSITION < 0) {
                    LAST_SONG_POSITION = 0;
                } else {
                    listView.setItemChecked(LAST_SONG_POSITION, false);
                    LAST_SONG_POSITION--;
                }
                mSongPath = songList.get(LAST_SONG_POSITION).get("songPath");
                playAudio(mSongPath);

                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private boolean playAudio(String songPath) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(songPath);
            mMediaPlayer.prepare();
            endTime.setText(getTextFromDuration(mMediaPlayer.getDuration()));
            mMediaPlayer.start();
            playButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
            listView.setItemChecked(LAST_SONG_POSITION, true);
            seekBar.setProgress(0);
            seekBar.setMax(mMediaPlayer.getDuration() / 1000);
            updateProgressBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicHandler.removeCallbacks(mUpdateTimeTask);

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    public void updateProgressBar() {
        if (mMediaPlayer.isPlaying()) {
            musicHandler.postDelayed(mUpdateTimeTask, 100);
        }
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer.isPlaying()) {
                int currentPosition = mMediaPlayer.getCurrentPosition();
                currentTime.setText(getTextFromDuration(currentPosition));
                seekBar.setProgress(mMediaPlayer.getCurrentPosition() / 1000);
                musicHandler.postDelayed(this, 100);
            }
        }
    };

    private String getTextFromDuration(int millis) {

        return String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds
                        (TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
