package com.costular.atomtasks.core.ui

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

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
    private val messages = MutableSharedFlow<AppSnackbarMessage>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun showMessage(message: AppSnackbarMessage) {
        messages.tryEmit(message)
    }

    internal fun observeMessages() = messages
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
    val resources = LocalResources.current

    LaunchedEffect(appSnackbarState, snackbarController, resources) {
        snackbarController.observeMessages().collectLatest { message ->
            val result = appSnackbarState.hostState.showSnackbar(
                message = resources.getString(message.messageRes),
                actionLabel = message.action?.let { resources.getString(it.labelRes) },
                withDismissAction = message.withDismissAction,
                duration = message.duration,
            )

            if (result == SnackbarResult.ActionPerformed) {
                message.action?.onActionPressed?.invoke()
            }
        }
    }
}
