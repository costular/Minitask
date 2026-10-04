package com.costular.atomtasks.ui.home

import com.costular.atomtasks.core.testing.MviViewModelTest
import com.costular.atomtasks.data.settings.DefaultTab
import com.costular.atomtasks.data.settings.GetThemeUseCase
import com.costular.atomtasks.data.settings.SettingsRepository
import com.costular.atomtasks.tasks.helper.AutoforwardManager
import com.costular.atomtasks.tasks.helper.recurrence.RecurrenceScheduler
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppViewModelTest : MviViewModelTest() {
    private val settings: SettingsRepository = mockk()
    private val theme: GetThemeUseCase = mockk(relaxed = true)
    private val autoforward: AutoforwardManager = mockk(relaxed = true)
    private val recurrence: RecurrenceScheduler = mockk(relaxed = true)

    @Test
    fun `startup waits until the saved preference is loaded`() = runTest {
        val tabs = MutableSharedFlow<DefaultTab>()
        every { settings.observeDefaultTab() } returns tabs
        val viewModel = createViewModel()
        assertThat(viewModel.state.value.defaultTab).isNull()

        tabs.emit(DefaultTab.VerticalTasks)

        assertThat(viewModel.state.value.defaultTab).isEqualTo(DefaultTab.VerticalTasks)
    }

    @Test
    fun `startup uses the saved Agenda tab`() = runTest {
        every { settings.observeDefaultTab() } returns MutableStateFlow(DefaultTab.Agenda)

        assertThat(createViewModel().state.value.defaultTab).isEqualTo(DefaultTab.Agenda)
    }

    @Test
    fun `setting changes do not replace the start graph during the current activity`() = runTest {
        val tabs = MutableStateFlow(DefaultTab.VerticalTasks)
        every { settings.observeDefaultTab() } returns tabs
        val viewModel = createViewModel()
        assertThat(viewModel.state.value.defaultTab).isEqualTo(DefaultTab.VerticalTasks)

        tabs.value = DefaultTab.Agenda

        assertThat(viewModel.state.value.defaultTab).isEqualTo(DefaultTab.VerticalTasks)
        assertThat(createViewModel().state.value.defaultTab).isEqualTo(DefaultTab.Agenda)
    }

    private fun createViewModel() = AppViewModel(theme, settings, autoforward, recurrence)
}
