package com.costular.atomtasks.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

data class AppSnackbarAction(
    @StringRes val labelRes: Int,
    val onActionPressed: () -> Unit,
)

data class AppSnackbarMessage(
    @StringRes val messageRes: Int,
    val action: AppSnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val withDismissAction: Boolean = false,
)

interface SnackbarManager {
    fun showMessage(message: AppSnackbarMessage)
}

object SnackbarController : SnackbarManager {
    private val messages = Channel<AppSnackbarMessage>(capacity = Channel.UNLIMITED)

    override fun showMessage(message: AppSnackbarMessage) {
        messages.trySend(message)
    }

    internal fun observeMessages() = messages.receiveAsFlow()
}

@Stable
class AppSnackbarState(
    val hostState: SnackbarHostState,
)

@Composable
fun rememberAppSnackbarState(
    hostState: SnackbarHostState = remember { SnackbarHostState() },
): AppSnackbarState = remember(hostState) {
    AppSnackbarState(hostState)
}

@Composable
fun AppSnackbarHostEffect(
    appSnackbarState: AppSnackbarState,
    snackbarController: SnackbarController,
) {
    val context = LocalContext.current

    LaunchedEffect(appSnackbarState, snackbarController, context) {
        snackbarController.observeMessages().collect { message ->
            val result = appSnackbarState.hostState.showSnackbar(
                message = context.getString(message.messageRes),
                actionLabel = message.action?.let { context.getString(it.labelRes) },
                withDismissAction = message.withDismissAction,
                duration = message.duration,
            )

            if (result == SnackbarResult.ActionPerformed) {
                message.action?.onActionPressed?.invoke()
            }
        }
    }
}
