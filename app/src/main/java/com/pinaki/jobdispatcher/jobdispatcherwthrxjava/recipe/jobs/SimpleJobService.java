package com.pinaki.jobdispatcher.jobdispatcherwthrxjava.recipe.jobs;

import android.os.Bundle;
import android.util.Log;

import com.pinaki.jobdispatcher.jobdispatcherwthrxjava.core.CoreJobService;

import org.jetbrains.annotations.Nullable;

public class SimpleJobService extends CoreJobService {
    private static final String TAG = "SimpleJobService";

    private static final int MINUTE_IN_MILLISECONDS = 1000;

    @Override
    public boolean onPerformJob(@Nullable Bundle extras) {
        // some extensive job can be performed here
        // for the sake of simplicity we can assume a task running for 10s, with update being posted per second
        try {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(MINUTE_IN_MILLISECONDS);
                Log.d(TAG, String.format("job update status >>> time up: %d", i));
            }

            Log.d(TAG, "job update status >>> job completed successfully");

        } catch (InterruptedException ignore) {
            // alternatively, we can make the method throw an exception
            // and as shouldRetryOnException returns true, our job will be rescheduled
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldRetryIfInterrupted() {
        return true;
    }
}
