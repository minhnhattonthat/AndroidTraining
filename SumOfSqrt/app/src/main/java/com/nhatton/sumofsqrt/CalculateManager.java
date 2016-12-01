package com.nhatton.sumofsqrt;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Norvia on 01/12/2016.
 */

public class CalculateManager {

    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static CalculateManager sInstance;


    static{

        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        sInstance = new CalculateManager();
    }

    private CalculateManager(){
        LinkedBlockingQueue<Object> mDivideQueue = new LinkedBlockingQueue<>();
    }

}
