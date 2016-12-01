package com.nhatton.sumofsqrt;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity {
    private static final int FIRST_PART_DONE = 1;
    private static final int SECOND_PART_DONE = 2;
    private static final int FINAL_PART_DONE = 3;
    private static final int FINISH_ONE_THREAD = 4;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private ThreadPoolExecutor threadPoolExecutor;
    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable runnable) {

        }
    };
    private EditText editText;
    private EditText editText2;
    private BigInteger input;
    private BigInteger inputOneThread;
    private BigInteger input1;
    private BigInteger input2;
    private BigDecimal outputOneThread;
    private BigDecimal output;
    private BigDecimal output1;
    private BigDecimal output2;
    private BigDecimal output3;
    private BigInteger i;
    private TextView timeResult;
    private TextView timeResult2;
    private TextView textView;
    private TextView textView2;
    private long start;
    private long startOneThread;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            result.run();
        }
    };
    Handler handlerOneThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            resultOneThread.run();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.input);
        textView = (TextView) findViewById(R.id.output);
        timeResult = (TextView) findViewById(R.id.time);
        Button button = (Button) findViewById(R.id.calculate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = currentTimeMillis();
                input = new BigInteger(editText.getText().toString());
                input1 = input.divide(BigInteger.valueOf(3));
                input2 = input1.multiply(BigInteger.valueOf(3));
                output = BigDecimal.ZERO;
                output1 = BigDecimal.ZERO;
                output2 = BigDecimal.ZERO;
                output3 = BigDecimal.ZERO;
                new Thread(firstPart).start();
                new Thread(secondPart).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        finalPart.run();
                        try {
                            Thread.sleep(7000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        new Thread(result).start();
                    }
                }).start();
            }
        });
        editText2 = (EditText) findViewById(R.id.input_2);
        textView2 = (TextView) findViewById(R.id.output_2);
        timeResult2 = (TextView) findViewById(R.id.time_2);
        Button button2 = (Button) findViewById(R.id.calculate_2);
        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startOneThread = currentTimeMillis();
                inputOneThread = new BigInteger(editText2.getText().toString());
                outputOneThread = BigDecimal.ZERO;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        oneThread.run();
                        new Thread(resultOneThread).start();
                    }
                }).start();
            }
        });
    }

    Runnable result = new Runnable() {
        @Override
        public void run() {
            output = output1.add(output2).add(output3);
            final long millis = currentTimeMillis() - start;
            Log.i("Finish", String.format("%d min %d sec %dms",
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes
                                    (TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds
                                    (TimeUnit.MILLISECONDS.toMinutes(millis)),
                    TimeUnit.MILLISECONDS.toMillis(millis) -
                            TimeUnit.SECONDS.toMillis
                                    (TimeUnit.MILLISECONDS.toSeconds(millis))));
            textView.post(new Runnable() {
                @Override
                public void run() {
                    String result = output.toString();
                    textView.setText(result);
                    timeResult.setText(String.valueOf(millis));
                }
            });
        }
    };

    Runnable firstPart =  new Runnable() {
        @Override
        public void run() {
            for (i = BigInteger.ONE; i.compareTo(input1) <= 0; i = i.add(BigInteger.ONE)) {
                BigDecimal squareRoot = BigDecimal.valueOf(Math.sqrt(i.doubleValue()));
                output1 = output1.add(squareRoot);
            }
        }
    };

    Runnable secondPart = new Runnable() {
        @Override
        public void run() {
            for (i = input1.add(BigInteger.ONE); i.compareTo(input2) <= 0; i = i.add(BigInteger.ONE)) {
                BigDecimal squareRoot = BigDecimal.valueOf(Math.sqrt(i.doubleValue()));
                output2 = output2.add(squareRoot);
            }
        }
    };

    Runnable finalPart = new Runnable() {
        @Override
        public void run() {
            for (i = input2.add(BigInteger.ONE); i.compareTo(input) <= 0; i = i.add(BigInteger.ONE)) {
                BigDecimal squareRoot = BigDecimal.valueOf(Math.sqrt(i.doubleValue()));
                output3 = output2.add(squareRoot);
            }
        }
    };

    Runnable oneThread = new Runnable() {
        @Override
        public void run() {
            for (i = BigInteger.ONE; i.compareTo(inputOneThread) <= 0; i = i.add(BigInteger.ONE)) {
                BigDecimal squareRoot = BigDecimal.valueOf(Math.sqrt(i.doubleValue()));
                outputOneThread = outputOneThread.add(squareRoot);
            }
        }
    };

    Runnable resultOneThread = new Runnable() {
        @Override
        public void run() {
            final long millis = currentTimeMillis() - startOneThread;
            Log.i("Finish", String.format("%d min %d sec %dms",
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes
                                    (TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds
                                    (TimeUnit.MILLISECONDS.toMinutes(millis)),
                    TimeUnit.MILLISECONDS.toMillis(millis) -
                            TimeUnit.SECONDS.toMillis
                                    (TimeUnit.MILLISECONDS.toSeconds(millis))));
            textView2.post(new Runnable() {
                @Override
                public void run() {
                    String result = outputOneThread.toString();
                    textView2.setText(result);
                    timeResult2.setText(String.valueOf(millis));
                }
            });
        }
    };
}