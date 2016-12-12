package com.nhatton.smallmediaplayer;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
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

    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final String TAG = "MediaPlayer";
    private static final String MEDIA_PATH = Environment.
            getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getAbsolutePath();

    private ListView mListView;
    private TextView mCurrentTime;
    private TextView mEndTime;
    private SeekBar mSeekBar;
    private MenuItem mPlayButton;

    private int LAST_SONG_POSITION = -1;
    private String mSongPath = "";

    private MediaSession mMediaSession;
    private MediaPlayer mMediaPlayer;
    private final Handler mMediaHandler = new Handler();

    private ArrayList<HashMap<String, String>> mSongList = new ArrayList<>();

    private FilenameFilter songFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            return (s.endsWith(".mp3") || s.endsWith(".MP3") || s.endsWith(".wav")
                    || s.endsWith(".WAV"));
        }
    };

    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer.isPlaying()) {
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition() / 1000);
                mMediaHandler.postDelayed(this, PROGRESS_UPDATE_INTERNAL);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        mListView = (ListView) findViewById(R.id.list_music);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mCurrentTime = (TextView) findViewById(R.id.start_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        mSongList = getSongList();
        mListView.setAdapter(new SimpleAdapter(this, mSongList, R.layout.playlist_item,
                new String[]{"songTitle"}, new int[]{R.id.song_title}));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mListView.setItemChecked(position, true);
                mSongPath = mSongList.get(position).get("songPath");

                if (LAST_SONG_POSITION != position) {
                    LAST_SONG_POSITION = position;
                    playAudioFromStart(mSongPath);
                } else if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
                invalidateOptionsMenu();
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                LAST_SONG_POSITION++;
                mSongPath = mSongList.get(LAST_SONG_POSITION).get("songPath");
                playAudioFromStart(mSongPath);
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentTime.setText(DateUtils.formatElapsedTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mMediaHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaHandler.removeCallbacks(mUpdateTimeTask);
                mMediaPlayer.seekTo(seekBar.getProgress() * 1000);
                updateProgressBar();
            }
        });

        mMediaSession = new MediaSession(this, this.getLocalClassName());
        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_FAST_FORWARD | PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PLAYING, 0, 1, SystemClock.elapsedRealtime())
                .build();
        mMediaSession.setPlaybackState(state);

        Intent intent = new Intent(this, RemoteControlReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mMediaSession.setMediaButtonReceiver(pi);
        mMediaSession.setActive(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        mPlayButton = menu.findItem(R.id.button_play);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mPlayButton = menu.findItem(R.id.button_play);
        if (mMediaPlayer.isPlaying()) {
            mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
        } else {
            mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
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
                    mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
                } else {
                    mMediaPlayer.start();
                    if (!mMediaHandler.post(mUpdateTimeTask)) {
                        updateProgressBar();
                    }
                    mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
                }
                break;
            case R.id.button_next:
                mListView.setItemChecked(LAST_SONG_POSITION, false);
                LAST_SONG_POSITION++;
                mSongPath = mSongList.get(LAST_SONG_POSITION).get("songPath");
                playAudioFromStart(mSongPath);
                break;
            case R.id.button_previous:
                if (LAST_SONG_POSITION < 0) {
                    LAST_SONG_POSITION = 0;
                } else {
                    mListView.setItemChecked(LAST_SONG_POSITION, false);
                    LAST_SONG_POSITION--;
                }
                mSongPath = mSongList.get(LAST_SONG_POSITION).get("songPath");
                playAudioFromStart(mSongPath);

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
                mSongList.add(song);
            }
        }

        return mSongList;
    }

    private void playAudioFromStart(String songPath) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(songPath);
            mMediaPlayer.prepare();
            mEndTime.setText(DateUtils.formatElapsedTime(mMediaPlayer.getDuration() / 1000));
            mMediaPlayer.start();
            mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
            mListView.setItemChecked(LAST_SONG_POSITION, true);
            mSeekBar.setProgress(0);
            mSeekBar.setMax(mMediaPlayer.getDuration() / 1000);
            updateProgressBar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaPlayer != null) {
            updateProgressBar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            mMediaHandler.postDelayed(mUpdateTimeTask, PROGRESS_UPDATE_INITIAL_INTERVAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaHandler.removeCallbacks(mUpdateTimeTask);

        if (mMediaSession != null) {
            mMediaSession.release();
        }

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    public void updateProgressBar() {
        if (mMediaPlayer.isPlaying()) {
            mMediaHandler.postDelayed(mUpdateTimeTask, PROGRESS_UPDATE_INITIAL_INTERVAL);
        }
    }

}
