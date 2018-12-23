package com.shortstack.hackertracker

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.github.stkent.amplify.feedback.DefaultEmailFeedbackCollector
import com.github.stkent.amplify.feedback.GooglePlayStoreFeedbackCollector
import com.github.stkent.amplify.tracking.Amplify
import com.orhanobut.logger.Logger
import com.shortstack.hackertracker.di.AppComponent
import com.shortstack.hackertracker.di.DaggerAppComponent
import com.shortstack.hackertracker.di.modules.*
import io.fabric.sdk.android.Fabric


class App : MultiDexApplication() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        application = this

        initFabric()
        initLogger()
        initFeedback()

        component = DaggerAppComponent.builder()
                .sharedPreferencesModule(SharedPreferencesModule())
                .databaseModule(DatabaseModule())
                .gsonModule(GsonModule())
                .notificationsModule(NotificationsModule())
                .dispatcherModule(DispatcherModule())
                .timerModule(TimerModule())
                .contextModule(ContextModule(this))
                .build()
    }

    private fun initFeedback() {
        Amplify.initSharedInstance(this)
                .setPositiveFeedbackCollectors(GooglePlayStoreFeedbackCollector())
                .setCriticalFeedbackCollectors(DefaultEmailFeedbackCollector(Constants.FEEDBACK_EMAIL))
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

    companion object {

        lateinit var application: App

    }
}
