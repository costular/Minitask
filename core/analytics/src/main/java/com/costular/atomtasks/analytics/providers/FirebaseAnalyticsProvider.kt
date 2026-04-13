package com.costular.atomtasks.analytics.providers

import android.os.Bundle
import com.costular.atomtasks.analytics.AtomAnalytics
import com.costular.atomtasks.analytics.TrackingEvent
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import javax.inject.Inject


class FirebaseAnalyticsProvider @Inject constructor() : AtomAnalytics {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    override fun track(event: TrackingEvent) {
        firebaseAnalytics.logEvent(event.name, event.paramsAsBundle())
    }
}

internal fun TrackingEvent.paramsAsBundle(): Bundle? = if (this.attributes != null) {
    Bundle().apply {
        attributes.forEach { entry ->
            putString(entry.key, entry.value?.toString())
        }
    }
} else {
    null
}
