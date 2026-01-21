package com.costular.atomtasks.review.ui

import android.app.Activity
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

    var reviewInfo: ReviewInfo? by remember { mutableStateOf(null) }
    
    atomLog { "ReviewHandler: Composing. shouldRequestReview=$shouldRequestReview" }

    manager.requestReviewFlow().addOnSuccessListener {
        atomLog { "ReviewHandler: Review info received: $it" }
        reviewInfo = it
    }

    LaunchedEffect(reviewInfo, shouldRequestReview) {
        val latestReviewInfo = reviewInfo
        
        atomLog { "ReviewHandler: LaunchedEffect. latestReviewInfo=$latestReviewInfo, shouldRequestReview=$shouldRequestReview" }

        if (latestReviewInfo != null && shouldRequestReview) {
            atomLog { "ReviewHandler: Launching review flow" }
            manager
                .launchReviewFlow(context as Activity, latestReviewInfo)
                .addOnCompleteListener { 
                    atomLog { "ReviewHandler: Review flow completed" }
                    onFinish() 
                }
        }
    }
}
