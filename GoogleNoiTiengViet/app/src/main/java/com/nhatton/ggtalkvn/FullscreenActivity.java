package com.nhatton.ggtalkvn;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.nhatton.ggtalkvn.MainActivity.mMediaPlayer;

public class FullscreenActivity extends Activity {

    private View mContentView;

    private String message = "";

    private int soundId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        message = extras.getString("TEXT_TO_FULLSCREEN");

        TextView textView = (TextView) findViewById(R.id.fullscreen_text);

        textView.setText(message);

        soundId = extras.getInt("SOUND_TO_FULLSCREEN");

        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer = MediaPlayer.create(FullscreenActivity.this, soundId);
                mMediaPlayer.start();
            }
        });

    }
}
