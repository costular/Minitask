package com.costular.atomtasks.ui

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.costular.atomtasks.BuildConfig
import com.costular.atomtasks.core.logging.AtomLogger
import com.costular.atomtasks.core.logging.FirebaseAtomLogger
import com.costular.atomtasks.core.logging.LogcatAtomLogger
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AtomTasksApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var autoforwardManager: AutoforwardManager

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        initLogger()
    }

    private fun initLogger() {
        val logger = if (BuildConfig.DEBUG) {
            LogcatAtomLogger()
        } else {
            FirebaseAtomLogger(Firebase.crashlytics)
        }

        AtomLogger.initialize(logger)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}
