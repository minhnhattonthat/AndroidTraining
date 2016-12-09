package com.nhatton.sumofsqrt;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class SumOfSquareRoot {
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static int MAXIMUM_POOL_SIZE = 8;
    private static long KEEP_ALIVE_TIME = 1;
    private static TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_BLOCK = 4;
    private static long BLOCK_SIZE;
    private BlockingQueue mWorkingQueue = new ArrayBlockingQueue(NUMBER_OF_BLOCK);

    private long mInput;
    private long[] mInputPart = new long[NUMBER_OF_BLOCK];
    private double mOutput;
    private double[] mOutputPart = new double[NUMBER_OF_BLOCK];

    SumOfSquareRoot(long input) {
        mInput = input;
        BLOCK_SIZE = input / NUMBER_OF_BLOCK;
    }

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, mWorkingQueue);

    double execute() {
        for (int i = 0; i < NUMBER_OF_BLOCK; i++) {
            mInputPart[i] = mInput * (i + 1) / (NUMBER_OF_BLOCK);
        }

        for (int i = 0; i < NUMBER_OF_BLOCK; i++) {
            threadPoolExecutor.execute(part(i));
        }
        threadPoolExecutor.shutdown();
        try {
            while (!threadPoolExecutor.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                Log.i("Main", "Waiting to complete");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < NUMBER_OF_BLOCK; i++) {
            mOutput = mOutput + mOutputPart[i];
        }

        return mOutput;
    }

    private Runnable part(final int position) {
        return new Runnable() {
            @Override
            public void run() {
                for (long j = mInputPart[position] - BLOCK_SIZE; j < mInputPart[position]; j++) {
                    mOutputPart[position] = mOutputPart[position] + Math.sqrt(j);
                }
            }
        };
    }
}
