package com.costular.atomtasks.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign

@Composable
fun DestinationsScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    DestinationsBottomSheet(navController = navController) {
        Scaffold(
            floatingActionButton = floatingActionButton,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) {
            content(it)
        }
    }
}

@Composable
fun DestinationsBottomSheet(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    ModalBottomSheetLayout(
        modifier = Modifier.imePadding(),
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
    ) {
        content()
    }
}
