package com.pinaki.jobdispatcher.jobdispatcherwthrxjava.core

import android.os.Bundle
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.firebase.jobdispatcher.RetryStrategy
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

public abstract class CoreJobService : JobService() {

    private val compositeDisposable = CompositeDisposable()

    /**
     * This is the entry point for our job and all our job related code should be written here
     * @param extras Bundle of extras, if specified while scheduling the job
     * @return true if the job doesn't need to be rescheduled, otherwise false
     */
    abstract fun onPerformJob(extras: Bundle?): Boolean

    /**
     * Called to determine whether the job needs to be rescheduled
     * when the scheduling engine has decided to interrupt the execution of a running job
     * @return true if the job should be retried. The retry mechanism can either be
     * @{@link RetryStrategy#DEFAULT_LINEAR} or @{@link RetryStrategy#DEFAULT_EXPONENTIAL}
     */
    abstract fun shouldRetryIfInterrupted(): Boolean

    /**
     * The entry point to your Job. Implementations should offload work to another thread of execution
     * as soon as possible because this runs on the main thread. If work was offloaded, call {@link
     * JobService#jobFinished(JobParameters, boolean)} to notify the scheduling service that the work
     * is completed.
     *
     * <p>If a job with the same service and tag was rescheduled during execution {@link
     * JobService#onStopJob(JobParameters)} will be called and the wakelock will be released. Please
     * make sure that all reschedule requests happen at the end of the job.
     *
     * @return {@code true} if there is more work remaining in the worker thread, {@code false} if the
     *     job was completed.
     */
    override fun onStartJob(job: JobParameters?): Boolean {
        val d = Single
                .fromCallable {
                    return@fromCallable onPerformJob(job?.extras)
                }.subscribeOn(getOperationalScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onJobDone(job, !it)
                }, {
                    onJobDone(job, shouldRetryOnException())
                })

        compositeDisposable.add(d)

        // since the work is still happening, we will retrun true
        return true
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job, most
     * likely because the runtime constraints associated with the job are no longer satisfied. The job
     * must stop execution.
     *
     * @return true if the job should be retried
     * @see com.firebase.jobdispatcher.JobInvocation.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    override fun onStopJob(job: JobParameters?): Boolean {
        disposeSubscriptions()
        return shouldRetryIfInterrupted()
    }

    /**
     * Specifies the scheduler on which our job must run. Our jobs run on @{@link Schedulers#io()} but
     * child classes can override this method to specify a different scheduler
     */
    open fun getOperationalScheduler(): Scheduler {
        return Schedulers.io()
    }

    /**
     * Specified whether a reschedule is needed if an exception is caught
     * during the execution of our job. Default is true but child classes can
     * override this method to specify if a reschedule is not required
     */
    open fun shouldRetryOnException(): Boolean {
        return true
    }

    private fun onJobDone(job: JobParameters?, shouldRetryIfInterrupted: Boolean) {
        disposeSubscriptions()

        if (job != null)
            jobFinished(job, shouldRetryIfInterrupted)
    }

    private fun disposeSubscriptions() {
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }
}