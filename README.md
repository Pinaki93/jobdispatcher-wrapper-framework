# Using Firebase Job Dispatcher for scheduling Jobs

This project demonstrates the use of [Firebase JobDispatcher](https://github.com/firebase/firebase-jobdispatcher-android "Firebase Job Scheduler Github page") to schedule recurring and one off jobs for your application. 

### Core Components
The Core Components used are `CoreJobService` and `JobScheduerFacade` which help us implement a wrapper around the `JobDispatcher` framework.

#### CoreJobService
This class is an abstract class and each class implementing this class will represent one `Job` that needs to be scheduled. The method `onPerformJob()` is where the `Job` begins. This job shall be performed on the `Scheduler` provided by the `getOperationalScheduler()` method. By default the scheduler is `io` but a child class can override this method to provide a different Scheduler like `Schedulers.computation()` or `AndroidSchedulers.mainThread()`. This will ensure that the `Job` is performed on the required Scheduler. The other abstract method `shouldRetryIfInterrupted()` should return a `boolean` representing whether the job should be rescheduled if an exception occurs. 

#### JobScheduerFacade

This class exposes three methods to schedule and cancel jobs, `scheduleOneOffJob`, `schedulePeriodicJob` and `cancelJob`. Each `job` must have a unique identifier or `tag` that will help us cancel it, if required.

### Recipe

This package contains `MainActivity` which contains a playground to play with one such job created, named `SimpleJobService` which performs an operation that lasts for 10 seconds. This mocks a real work REST API call, for instance, which might have progress and result. If an exception arises during the execution of the job, we can either make the method throw an exception or catch the exception and return `true` or `false` denoting if the job should be rescheduled. We are using the same service for our `recurring` and `one off` tasks so we have used an interface `TagProvider` to differentiate between the tags.