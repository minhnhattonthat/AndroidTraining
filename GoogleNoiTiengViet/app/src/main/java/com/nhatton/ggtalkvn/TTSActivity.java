package com.nhatton.ggtalkvn;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class TTSActivity extends AppCompatActivity {

    private Button btnSpeak;
    private EditText txtText;

    private SeekBar pitchBar;
    private TextView pitchValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tts);

        btnSpeak = (Button) findViewById(R.id.input_button);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CharSequence sentence = txtText.getText().toString();
                MainActivity.tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
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
                Float floatVal = .5f * i + 0.5f;
                pitchValue.setText(String.valueOf(floatVal));
                MainActivity.tts.setPitch(floatVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
    }
}
