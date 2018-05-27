package com.shortstack.hackertracker

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import com.github.stkent.amplify.tracking.Amplify
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.di.DaggerMyComponent
import com.shortstack.hackertracker.di.MyComponent
import com.shortstack.hackertracker.di.modules.*
import com.shortstack.hackertracker.models.Conference
import com.shortstack.hackertracker.network.task.SyncJob
import com.shortstack.hackertracker.utils.SharedPreferencesUtil
import io.fabric.sdk.android.Fabric
import java.util.*


class App : Application() {

    lateinit var myComponent: MyComponent

    // Storage
    @Deprecated(message = "Use DI")
    val storage: SharedPreferencesUtil by lazy { SharedPreferencesUtil(applicationContext) }
    @Deprecated("use DI")
    private val dispatcher: FirebaseJobDispatcher by lazy { FirebaseJobDispatcher(GooglePlayDriver(applicationContext)) }


    // TODO: Remove, this is just for measuring launch time.
    var timeToLaunch: Long = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()

        application = this

        initFabric()
        initLogger()
        initFeedback()

        myComponent = DaggerMyComponent.builder()
                .sharedPreferencesModule(SharedPreferencesModule())
                .databaseModule(DatabaseModule())
                .gsonModule(GsonModule())
                .analyticsModule(AnalyticsModule())
                .notificationsModule(NotificationsModule())
                .dispatcherModule(DispatcherModule())
                .contextModule(ContextModule(this))
                .build()

        // TODO: Remove, this is only for debugging.
        Logger.d("Time to complete onCreate " + (System.currentTimeMillis() - timeToLaunch))
    }

    fun updateTheme(con: Conference?) {
        val theme = when (con?.index) {
            1 -> R.style.AppTheme_Hackwest
            2 -> R.style.AppTheme_Toorcon
            3 -> R.style.AppTheme_BsidesOrl
            else -> R.style.AppTheme
        }
        setTheme(theme)
    }

    fun scheduleSync() {

        cancelSync()

        var value = storage.syncInterval

        if (value == 0) {
            cancelSync()
            return
        }

        value *= Constants.TIME_SECONDS_IN_HOUR

        val job = dispatcher.newJobBuilder()
                .setService(SyncJob::class.java)
                .setTag(SyncJob.TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(value, value + Constants.TIME_SECONDS_IN_HOUR))
                .build()

        dispatcher.mustSchedule(job)
    }

    private fun cancelSync() {
        dispatcher.cancel(SyncJob.TAG)
    }

    private fun initFeedback() {
        Amplify.initSharedInstance(this)
                .setFeedbackEmailAddress(Constants.FEEDBACK_EMAIL)
                .applyAllDefaultRules()
                .setLastUpdateTimeCooldownDays(1)
    }

    private fun initLogger() {
        Logger.init().methodCount(1).hideThreadInfo()
    }

    private fun initFabric() {
        if (!BuildConfig.DEBUG)
            Fabric.with(this, Crashlytics())
    }

    @Deprecated("", replaceWith = ReplaceWith("BusProvider.bus.register(any)"))
    fun registerBusListener(any: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Deprecated("", replaceWith = ReplaceWith("BusProvider.bus.unregister(any)"))
    fun unregisterBusListener(any: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    @Deprecated("", replaceWith = ReplaceWith("BusProvider.bus.post(any)"))
    fun postBusEvent(any: Any) {

    }

    companion object {
        @Deprecated("Do not use", replaceWith = ReplaceWith("Date().now()"))
        fun getCurrentDate(): Date {
            return Date().now()
        }

        lateinit var application: App
    }
}
