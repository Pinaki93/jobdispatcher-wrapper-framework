package com.pinaki.jobdispatcher.jobdispatcherwthrxjava.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Provides methods to schedule recurring
 * or non recurring jobs
 */
public class JobScheduerFacade {

    private static final int MINUTE_IN_SECONDS = 60;

    public static void scheduleOneOffJob(Context context, Class<? extends CoreJobService> jobClazz, String tag, @Nullable Bundle extras, boolean shouldReplaceCurrent, @Constraint.JobConstraint int... constraints) {
        if (context == null || !isGooglePlayServicesAvailable(context))
            return;

        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job.Builder jobBuilder = buildJob(jobClazz, tag, extras, shouldReplaceCurrent, jobDispatcher);

        // add constraints if passed
        if (constraints != null && constraints.length != 0) {
            jobBuilder.setConstraints(constraints);
        }

        // it will try to trigger the job between 0s and one minute
        // this can be passed as a method parameter as well, if required
        jobBuilder.setTrigger(Trigger.executionWindow(0, MINUTE_IN_SECONDS));

        Job job = jobBuilder.build();

        jobDispatcher.mustSchedule(job);
    }

    public static void schedulePeriodicJob(Context context, Class<? extends CoreJobService> jobClazz, String tag, @Nullable Bundle extras, boolean shouldReplaceCurrent, int periodInSeconds, @Constraint.JobConstraint int... constraints) {
        if (context == null || !isGooglePlayServicesAvailable(context))
            return;

        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Job.Builder jobBuilder = buildJob(jobClazz, tag, extras, shouldReplaceCurrent, jobDispatcher);

        jobBuilder.setRecurring(true);

        if (constraints != null && constraints.length != 0) {
            jobBuilder.setConstraints(constraints);
        }

        // it will try to trigger the job between 0s and one minute
        // this can be passed as a method parameter as well, if required
        jobBuilder.setTrigger(Trigger.executionWindow(periodInSeconds, periodInSeconds + MINUTE_IN_SECONDS));

        Job job = jobBuilder.build();

        jobDispatcher.mustSchedule(job);
    }

    @NonNull
    private static Job.Builder buildJob(Class<? extends CoreJobService> jobClazz, String tag, @Nullable Bundle extras, boolean shouldReplaceCurrent, FirebaseJobDispatcher jobDispatcher) {
        Job.Builder builder = jobDispatcher.newJobBuilder()
                .setService(jobClazz)
                .setTag(tag)
                .setReplaceCurrent(shouldReplaceCurrent)
                .setLifetime(Lifetime.FOREVER) // can be passed as a method parameter, if required
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR);

        // add bundle if extras present
        if (extras != null && !extras.isEmpty())
            builder.setExtras(extras);

        return builder;
    }

    /**
     * Attempts to cancel an ongoing job with the given tag
     *
     * @return true if successful
     */
    public static boolean cancelJob(Context context, String tag) {
        if (context == null || !isGooglePlayServicesAvailable(context))
            return false;

        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        return FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS == jobDispatcher.cancel(tag);
    }

    private static boolean isGooglePlayServicesAvailable(Context context) {
        return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }
}
