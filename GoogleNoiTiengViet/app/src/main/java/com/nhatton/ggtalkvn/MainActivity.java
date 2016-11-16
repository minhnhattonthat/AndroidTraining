package com.nhatton.ggtalkvn;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    public ArrayList<Sound> mSounds = null;
    private SoundAdapter mAdapter = null;

    static MediaPlayer mMediaPlayer = null;
    public static TextToSpeech tts;
    public final static String localeAsString = "vi_VN";
    private int MY_DATA_CHECK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Text-to-speech");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        setContentView(R.layout.activity_main);

        //check for TTS resource available
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        tts = new TextToSpeech(MainActivity.this, this);

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

        dialog.hide();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TTSActivity.class);
                startActivity(intent);
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

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale(localeAsString));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                MainActivity.tts = new TextToSpeech(this, this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
}
