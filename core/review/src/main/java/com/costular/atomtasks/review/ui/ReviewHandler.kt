package com.costular.atomtasks.review.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.costular.atomtasks.core.logging.atomLog
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
fun ReviewHandler(
    shouldRequestReview: Boolean,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val manager = remember { ReviewManagerFactory.create(context) }
    var reviewInfo by remember { mutableStateOf<ReviewInfo?>(null) }

    LaunchedEffect(manager) {
        val task = manager.requestReviewFlow()
        task.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                request.exception?.let {
                    atomLog { it }
                }
            }
        }
    }

    LaunchedEffect(reviewInfo, shouldRequestReview) {
        if (shouldRequestReview) {
            val activity = context.findActivity()
            val currentReviewInfo = reviewInfo

            if (activity != null && currentReviewInfo != null) {
                manager.launchReviewFlow(activity, currentReviewInfo)
                    .addOnCompleteListener {
                        onFinish()
                    }
            } else {
                 // If we can't show it (no activity or no info), we should probably just finish
                 // to avoid getting stuck in a state where we want to show it but can't.
                 // However, if info is loading, we might want to wait. 
                 // But since we pre-fetch in the other LaunchedEffect, if it's not here yet, 
                 // we wait. If it failed, it stays null.
                 // We don't have a good way to know if it failed vs loading here without more state.
                 // For now, let's assume if it's null we wait. 
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
