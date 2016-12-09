package com.nhatton.sumofsqrt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Long.parseLong;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity {
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static int MAXIMUM_POOL_SIZE = 8;
    private static long KEEP_ALIVE_TIME = 1;
    private static TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue workQueue = new ArrayBlockingQueue(3);
    private ThreadPoolExecutor threadPoolExecutor;

    private long startThreeThreads;
    private long inputThreeThreads;
    private double outputFirstPart;
    private double outputSecondPart;
    private double outputFinalPart;
    private long firstRange;
    private long secondRange;
    private double outputThreeThreads;
    private EditText editTextThreeThreads;
    private TextView timeResultThreeThreads;
    private TextView resultViewThreeThreads;

    private long startOneThread;
    private long inputOneThread;
    private double outputOneThread;
    private EditText editTextOneThread;
    private TextView timeResultOneThread;
    private TextView resultViewOneThread;


    private long startManyThreads;
    private long inputManyThreads;
    private double outputManyThreads;
    private EditText editTextManyThreads;
    private TextView timeResultManyThreads;
    private TextView resultViewManyThreads;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, workQueue);

        editTextThreeThreads = (EditText) findViewById(R.id.input);
        resultViewThreeThreads = (TextView) findViewById(R.id.output);
        timeResultThreeThreads = (TextView) findViewById(R.id.time);

        Button buttonThreeThreads = (Button) findViewById(R.id.calculate);
        buttonThreeThreads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, workQueue);

                startThreeThreads = currentTimeMillis();

                inputThreeThreads = parseLong(editTextThreeThreads.getText().toString());
                firstRange = inputThreeThreads / 3;
                secondRange = firstRange * 2;

                outputThreeThreads = 0;
                outputFirstPart = 0;
                outputSecondPart = 0;
                outputFinalPart = 0;

                new Thread(resultThreeThreads).start();
            }
        });

        editTextOneThread = (EditText) findViewById(R.id.input_2);
        resultViewOneThread = (TextView) findViewById(R.id.output_2);
        timeResultOneThread = (TextView) findViewById(R.id.time_2);

        Button buttonOneThread = (Button) findViewById(R.id.calculate_2);
        buttonOneThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOneThread = currentTimeMillis();

                inputOneThread = parseLong(editTextOneThread.getText().toString());

                outputOneThread = 0;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        oneThread.run();
                    }
                }).start();
            }
        });

        editTextManyThreads = (EditText) findViewById(R.id.input_3);
        resultViewManyThreads = (TextView) findViewById(R.id.output_3);
        timeResultManyThreads = (TextView) findViewById(R.id.time_3);

        Button buttonManyThreads = (Button) findViewById(R.id.calculate_3);
        buttonManyThreads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManyThreads = currentTimeMillis();
                inputManyThreads = parseLong(editTextManyThreads.getText().toString());
                final SumOfSquareRoot sumOfSquareRoot = new SumOfSquareRoot(inputManyThreads);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        outputManyThreads = sumOfSquareRoot.execute();
                        final long millis = currentTimeMillis() - startManyThreads;
                        Log.i("FinishManyThreads", getTime(millis));
                        resultViewManyThreads.post(new Runnable() {
                            @Override
                            public void run() {
                                resultViewManyThreads.setText(String.valueOf(outputManyThreads));
                                timeResultManyThreads.setText(getTime(millis));
                            }
                        });
                    }
                }).start();
            }
        });

    }

    Runnable resultThreeThreads = new Runnable() {
        @Override
        public void run() {
            threadPoolExecutor.execute(firstPart);
            threadPoolExecutor.execute(secondPart);
            threadPoolExecutor.execute(finalPart);
            threadPoolExecutor.shutdown();
            try {
                while (!threadPoolExecutor.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                    Log.i("Main", "Waiting to complete");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outputThreeThreads = outputFirstPart + outputSecondPart + outputFinalPart;

            final long millis = currentTimeMillis() - startThreeThreads;

            Log.i("FinishThreeThreads", getTime(millis));
            resultViewThreeThreads.post(new Runnable() {
                @Override
                public void run() {
                    resultViewThreeThreads.setText(String.valueOf(outputThreeThreads));
                    timeResultThreeThreads.setText(getTime(millis));
                    Log.i("First part", String.valueOf(outputFirstPart));
                    Log.i("Second part", String.valueOf(outputSecondPart));
                    Log.i("Final part", String.valueOf(outputFinalPart));
                }
            });
        }
    };

    Runnable firstPart = new Runnable() {
        @Override
        public void run() {
            for (long i = 1; i <= firstRange; i++) {
                outputFirstPart = outputFirstPart + Math.sqrt(i);
            }
            final long millis = currentTimeMillis() - startThreeThreads;
            Log.i("FinishFirstThread", getTime(millis));
        }
    };

    Runnable secondPart = new Runnable() {
        @Override
        public void run() {
            for (long m = firstRange + 1; m <= secondRange; m++) {
                outputSecondPart = outputSecondPart + Math.sqrt(m);
            }
            final long millis = currentTimeMillis() - startThreeThreads;
            Log.i("FinishSecondThread", getTime(millis));
        }
    };

    Runnable finalPart = new Runnable() {
        @Override
        public void run() {
            for (long n = secondRange + 1; n <= inputThreeThreads; n++) {
                outputFinalPart = outputFinalPart + Math.sqrt(n);
            }
            final long millis = currentTimeMillis() - startThreeThreads;
            Log.i("FinishFinalThread", getTime(millis));
        }
    };

    Runnable oneThread = new Runnable() {
        @Override
        public void run() {
            for (long k = 1; k <= inputOneThread; k++) {
                outputOneThread = outputOneThread + Math.sqrt(k);
            }
            final long millis = currentTimeMillis() - startOneThread;
            Log.i("FinishOneThread", getTime(millis));
            resultViewOneThread.post(new Runnable() {
                @Override
                public void run() {
                    resultViewOneThread.setText(String.valueOf(outputOneThread));
                    timeResultOneThread.setText(getTime(millis));
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread.getAllStackTraces().clear();
    }

    private String getTime(long millis) {
        return String.format(Locale.getDefault(), "%d min %d sec %dms",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes
                                (TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds
                                (TimeUnit.MILLISECONDS.toMinutes(millis)),
                TimeUnit.MILLISECONDS.toMillis(millis) -
                        TimeUnit.SECONDS.toMillis
                                (TimeUnit.MILLISECONDS.toSeconds(millis)));
    }
}