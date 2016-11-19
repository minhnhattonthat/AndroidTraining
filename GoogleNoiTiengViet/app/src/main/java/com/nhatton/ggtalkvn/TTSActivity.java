package com.nhatton.ggtalkvn;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class TTSActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private SoundDbAdapter mDbHelper;

    private Button btnSpeak;
    private EditText txtText;

    private SeekBar pitchBar;
    private TextView pitchValue;

    private int MY_DATA_CHECK_CODE = 0;

    public static TextToSpeech tts;
    public final static String localeAsString = "vi_VN";
    private final static int smoothness = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tts);

        //check for TTS resource available
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        btnSpeak = (Button) findViewById(R.id.input_button);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                CharSequence sentence = txtText.getText().toString();

                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        txtText = (EditText) findViewById(R.id.input_text);
        txtText.requestFocus();

        pitchValue = (TextView) findViewById(R.id.pitch_value);
        pitchValue.setText("1.0");

        pitchBar = (SeekBar) findViewById(R.id.pitch_bar);
        pitchBar.setOnSeekBarChangeListener(new SeekBar.
                OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Float floatVal = .25f * i + 0.5f;
                pitchValue.setText(String.valueOf(floatVal));
                tts.setPitch(floatVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });


        SeekBar volControl = (SeekBar) findViewById(R.id.vol_bar);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volControl.setMax(maxVolume * smoothness);
        volControl.setProgress(curVolume * smoothness);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress / smoothness, 0);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TTSActivity.this, Collection.class);
                startActivity(intent);
            }
        });

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
                tts = new TextToSpeech(this, this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
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
        if (id == R.id.action_save) {

            mDbHelper = new SoundDbAdapter(this);

            mDbHelper.open();

            String description = txtText.getText().toString();

            long t = mDbHelper.createSound(description);

            if(t > -1){
                Toast.makeText(this, R.string.toast_saved,Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

}
