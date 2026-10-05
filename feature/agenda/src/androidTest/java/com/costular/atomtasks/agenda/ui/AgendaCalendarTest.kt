package com.costular.atomtasks.agenda.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.core.ui.date.asDay
import com.costular.atomtasks.core.util.DateTimeFormatters
import com.costular.atomtasks.tasks.model.Task
import com.costular.designsystem.theme.AtomTheme
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.io.File
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class AgendaCalendarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var state by mutableStateOf(AgendaState(selectedDay = InitialDate.asDay()))
    private val selectedDates = mutableListOf<LocalDate>()
    private var searches = 0

    @Test
    fun titleTogglesBetweenWeeklyAndMonthlyCalendars() {
        showAgenda()

        weeklyCalendar().assertIsDisplayed()
        monthlyCalendar().assertDoesNotExist()
        assertExpansion(expanded = false)

        toggleHeader()

        monthlyCalendar().assertIsDisplayed()
        weeklyCalendar().assertDoesNotExist()
        assertExpansion(expanded = true)

        toggleHeader()

        weeklyCalendar().assertIsDisplayed()
        monthlyCalendar().assertDoesNotExist()
        assertExpansion(expanded = false)
    }

    @Test
    fun monthlySelectionUpdatesTasksAndKeepsCalendarExpanded() {
        showAgenda(expanded = true)

        selectMonthlyDay(20)

        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(InitialDate.withDayOfMonth(20))
            assertThat(state.isHeaderExpanded).isTrue()
            assertThat(selectedDates).containsExactly(InitialDate.withDayOfMonth(20))
        }
        monthlyCalendar().assertIsDisplayed()
        weeklyCalendar().assertDoesNotExist()
        val locale = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.locales[0]
        val description = InitialDate.withDayOfMonth(20)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
        composeTestRule.onNodeWithContentDescription(description).assertIsSelected()
        composeTestRule.onNodeWithText("Tasks for 2026-08-20").assertIsDisplayed()
    }

    @Test
    fun monthBrowsingDoesNotSelectDateAndCollapseShowsSelectedWeek() {
        showAgenda(expanded = true)

        monthlyCalendar().performTouchInput { swipeLeft() }
        assertMonth(LocalDate.of(2026, 9, 1))
        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(InitialDate)
            assertThat(selectedDates).isEmpty()
        }

        selectMonthlyDay(20)
        toggleHeader()

        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(LocalDate.of(2026, 9, 20))
        }
        composeTestRule.onNode(
            hasText("20") and hasAnyAncestor(hasTestTag(TestTagWeekCalendar)),
        ).assertIsDisplayed()

        toggleHeader()
        assertMonth(LocalDate.of(2026, 9, 1))
    }

    @Test
    fun reopeningCalendarReturnsToSelectedMonthAfterBrowsing() {
        showAgenda(expanded = true)

        monthlyCalendar().performTouchInput { swipeLeft() }
        assertMonth(LocalDate.of(2026, 9, 1))
        toggleHeader()
        toggleHeader()

        assertMonth(InitialDate)
    }

    @Test
    fun todayChangesMonthWithoutCollapsingCalendar() {
        showAgenda(expanded = true)
        composeTestRule.runOnIdle {
            selectDate(LocalDate.now().minusMonths(2))
        }

        composeTestRule.onNodeWithText(string(R.string.today)).performClick()

        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(LocalDate.now())
            assertThat(state.isHeaderExpanded).isTrue()
        }
        monthlyCalendar().assertIsDisplayed()
        assertMonth(LocalDate.now())
    }

    @Test
    fun taskPageSwipeUpdatesDateWhileCalendarStaysExpanded() {
        showAgenda(expanded = true)

        composeTestRule.onNodeWithTag("AgendaTaskList").performTouchInput { swipeLeft() }

        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(InitialDate.plusDays(1))
            assertThat(state.isHeaderExpanded).isTrue()
        }
        monthlyCalendar().assertIsDisplayed()
    }

    @Test
    fun searchRemainsIndependentOfExpansionAndCalendarActionIsAbsent() {
        showAgenda(expanded = true)

        composeTestRule.onNodeWithContentDescription(string(R.string.search_tasks)).performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.home_menu_calendar)).assertDoesNotExist()

        composeTestRule.runOnIdle {
            assertThat(searches).isEqualTo(1)
            assertThat(state.isHeaderExpanded).isTrue()
        }
        monthlyCalendar().assertIsDisplayed()
    }

    @Test
    fun browsingPreviousMonthDoesNotSelectDate() {
        showAgenda(expanded = true)

        monthlyCalendar().performTouchInput { swipeRight() }

        assertMonth(LocalDate.of(2026, 7, 1))
        composeTestRule.runOnIdle {
            assertThat(state.selectedDay.date).isEqualTo(InitialDate)
            assertThat(selectedDates).isEmpty()
        }
    }

    @Test
    fun sixRowMonthLeavesTasksVisibleInLightTheme() {
        showAgenda(expanded = true)

        selectMonthlyDay(31)

        monthlyCalendar().assertIsDisplayed()
        assertCompactCalendar()
        composeTestRule.onNodeWithText("Tasks for 2026-08-31").assertIsDisplayed()
        saveScreenshot("agenda-monthly-light")
    }

    @Test
    fun sixRowMonthLeavesTasksVisibleOnNarrowScreenWithLargeFontAndDarkTheme() {
        showAgenda(expanded = true, darkTheme = true, maxWidth = 320.dp, fontScale = 1.5f)

        selectMonthlyDay(31)

        monthlyCalendar().assertIsDisplayed()
        assertCompactCalendar()
        composeTestRule.onNodeWithText("Tasks for 2026-08-31").assertIsDisplayed()
        assertExpansion(expanded = true)
        saveScreenshot("agenda-monthly-narrow-dark")
    }

    private fun saveScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), "$name.png").outputStream().use { output ->
            composeTestRule.onRoot().captureToImage().asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    private fun assertCompactCalendar() {
        val bounds = monthlyCalendar().getUnclippedBoundsInRoot()
        assertThat(bounds.bottom.value - bounds.top.value).isAtMost(320f)
    }

    private fun showAgenda(
        expanded: Boolean = false,
        darkTheme: Boolean = false,
        maxWidth: Dp = Dp.Infinity,
        fontScale: Float = 1f,
    ) {
        state = state.copy(isHeaderExpanded = expanded, tasks = tasksFor(InitialDate))
        composeTestRule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                AtomTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AgendaScreen(
                            state = state,
                            onSelectDate = ::selectDate,
                            onSelectToday = { selectDate(LocalDate.now()) },
                            onToggleHeader = { state = state.copy(isHeaderExpanded = !state.isHeaderExpanded) },
                            onMarkTask = { _, _ -> },
                            openTaskDetail = {},
                            openTaskAction = {},
                            onDeleteTask = {},
                            onDragTask = { _, _ -> },
                            onDragStopped = {},
                            modifier = Modifier.fillMaxSize(),
                            onSearch = { searches++ },
                        )
                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        selectedDates.add(date)
        state = state.copy(selectedDay = date.asDay(), tasks = tasksFor(date))
    }

    private fun tasksFor(date: LocalDate) = TasksState.Success(
        persistentListOf(
            Task(
                id = 1L,
                name = "Tasks for $date",
                createdAt = InitialDate,
                day = date,
                reminder = null,
                isDone = false,
                position = 0,
                isRecurring = false,
                recurrenceEndDate = null,
                recurrenceType = null,
                parentId = null,
            ),
        ),
    )

    private fun toggleHeader() = composeTestRule.onNodeWithTag(TestTagHeader).performClick()

    private fun weeklyCalendar() = composeTestRule.onNodeWithTag(TestTagWeekCalendar)

    private fun monthlyCalendar() = composeTestRule.onNodeWithTag(TestTagMonthCalendar)

    private fun selectMonthlyDay(day: Int) {
        val matchingDays = composeTestRule.onAllNodes(
            hasText(day.toString()) and hasClickAction() and isEnabled() and
                hasAnyAncestor(hasTestTag(TestTagMonthCalendar)),
        )
        val visibleIndices = matchingDays.fetchSemanticsNodes().indices.filter { index ->
            matchingDays[index].isDisplayed()
        }
        assertThat(visibleIndices).hasSize(1)
        matchingDays[visibleIndices.single()].performClick()
    }

    private fun assertMonth(date: LocalDate) {
        val locale = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.locales[0]
        composeTestRule.onNodeWithText(date.format(DateTimeFormatters.monthFormatter.withLocale(locale)))
            .assertIsDisplayed()
    }

    private fun assertExpansion(expanded: Boolean) {
        val description = string(
            if (expanded) R.string.agenda_calendar_expanded else R.string.agenda_calendar_collapsed,
        )
        composeTestRule.onNodeWithTag(TestTagHeader)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, description))
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    companion object {
        private val InitialDate = LocalDate.of(2026, 8, 15)
    }
}
