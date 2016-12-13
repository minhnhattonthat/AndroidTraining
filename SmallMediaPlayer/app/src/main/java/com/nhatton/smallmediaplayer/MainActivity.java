package com.nhatton.smallmediaplayer;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.os.Environment.DIRECTORY_MUSIC;
import static java.lang.Math.random;

public class MainActivity extends AppCompatActivity {

    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }

    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final String TAG = "MediaPlayer";
    private static final String MEDIA_PATH = Environment.
            getExternalStoragePublicDirectory(DIRECTORY_MUSIC).getAbsolutePath();

    private ListView mListView;
    private TextView mCurrentTime;
    private TextView mEndTime;
    private TextView mDialogText;
    private TextSwitcher mSongTitleText;
    private SeekBar mSeekBar;
    private MenuItem mPlayButton;

    private int LAST_SONG_POSITION = -1;
    private boolean isShuffle = false;
    private boolean mShowing;
    private boolean mReady;
    private char mPrevLetter = Character.MIN_VALUE;

    private WindowManager mWindowManager;
    private RemoveWindow mRemoveWindow = new RemoveWindow();
    private MediaSession mMediaSession;
    private MediaPlayer mMediaPlayer;
    private final Handler mWindowHandler = new Handler();
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
        mSongTitleText = (TextSwitcher) findViewById(R.id.song_title_bar);

        final LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView currentView = (TextView) inflate.inflate(R.layout.title_bar, null);
        TextView newView = (TextView) inflate.inflate(R.layout.title_bar, null);
        mSongTitleText.addView(currentView, vlp);
        mSongTitleText.addView(newView, vlp);

        mSongTitleText.setCurrentText(getText(R.string.song_title_default));
        mSongTitleText.setInAnimation(in);
        mSongTitleText.setOutAnimation(out);

        mSongList = getSongList();
        mListView.setAdapter(new SimpleAdapter(this, mSongList, R.layout.playlist_item,
                new String[]{"songTitle"}, new int[]{R.id.song_title}));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (LAST_SONG_POSITION != position) {
                    LAST_SONG_POSITION = position;
                    mListView.setItemChecked(position, true);
                    playAudioFromStart(mSongList.get(LAST_SONG_POSITION));
                } else if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
                invalidateOptionsMenu();
            }
        });

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (mReady) {
                    char firstLetter = mSongList.get(firstVisibleItem).get("songTitle").charAt(0);
                    if (Character.isLowerCase(firstLetter)) {
                        firstLetter = Character.toUpperCase(firstLetter);
                    }
                    if (!mShowing && firstLetter != mPrevLetter) {

                        mShowing = true;
                        mDialogText.setVisibility(View.VISIBLE);
                    }
                    mDialogText.setText(((Character) firstLetter).toString());
                    mWindowHandler.removeCallbacks(mRemoveWindow);
                    mWindowHandler.postDelayed(mRemoveWindow, 1500);
                    mPrevLetter = firstLetter;
                }
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isShuffle) {
                    Random random = new Random();
                    LAST_SONG_POSITION = random.nextInt(mSongList.size() - 1);
                } else {
                    LAST_SONG_POSITION++;
                }
                playAudioFromStart(mSongList.get(LAST_SONG_POSITION));
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

        mWindowHandler.post(new Runnable() {
            public void run() {
                mReady = true;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }
        });
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
            case R.id.action_shuffle:
                if (item.isChecked()) {
                    item.setChecked(false);
                    isShuffle = false;
                } else {
                    item.setChecked(true);
                    isShuffle = true;
                }
                break;
            case R.id.button_play:
                if (LAST_SONG_POSITION < 0) {
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
                if (isShuffle) {
                    Random random = new Random();
                    LAST_SONG_POSITION = random.nextInt(mSongList.size() - 1);
                } else {
                    LAST_SONG_POSITION++;
                }
                playAudioFromStart(mSongList.get(LAST_SONG_POSITION));
                break;
            case R.id.button_previous:
                if (LAST_SONG_POSITION < 0) {
                    LAST_SONG_POSITION = 0;
                } else {
                    mListView.setItemChecked(LAST_SONG_POSITION, false);
                    if (isShuffle) {
                        Random random = new Random();
                        LAST_SONG_POSITION = random.nextInt(mSongList.size() - 1);
                    } else {
                        LAST_SONG_POSITION++;
                    }
                }
                playAudioFromStart(mSongList.get(LAST_SONG_POSITION));

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<HashMap<String, String>> getSongList() {
        ArrayList<HashMap<String, String>> songList = new ArrayList<>();
        File[] directory = (new File(MEDIA_PATH)).listFiles(songFilter);

        if (directory.length > 0) {
            final Collator vnSort = Collator.getInstance(new Locale("vi_VN"));
            Arrays.sort(directory, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    String s1 = o1.getName();
                    String s2 = o2.getName();
                    return vnSort.compare(s1.toLowerCase(), s2.toLowerCase());
                }
            });
            for (File file : directory) {
                HashMap<String, String> song = new HashMap<>();
                song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("songPath", file.getPath());
                songList.add(song);
            }
        }

        return songList;
    }

    private void playAudioFromStart(HashMap<String, String> song) {
        String songPath = song.get("songPath");
        String songTitle = song.get("songTitle");
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(songPath);
            mMediaPlayer.prepare();

            mSongTitleText.setText(songTitle);
            mEndTime.setText(DateUtils.formatElapsedTime(mMediaPlayer.getDuration() / 1000));
            mPlayButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
            mListView.setItemChecked(LAST_SONG_POSITION, true);
            mSeekBar.setProgress(0);
            mSeekBar.setMax(mMediaPlayer.getDuration() / 1000);

            mMediaPlayer.start();

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
        mReady = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWindowHandler.removeCallbacks(mRemoveWindow);
        mWindowManager.removeView(mDialogText);
        mReady = false;
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

    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

}
