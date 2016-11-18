package com.nhatton.ggtalkvn;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import static com.nhatton.ggtalkvn.Collection.mMediaPlayer;
import static com.nhatton.ggtalkvn.TTSActivity.tts;

public class FullscreenActivity extends Activity {

    private View mContentView;

    private String message = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        message = extras.getString("TEXT_TO_FULLSCREEN");

        TextView textView = (TextView) findViewById(R.id.fullscreen_text);

        textView.setText(message);

        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

    }
}
