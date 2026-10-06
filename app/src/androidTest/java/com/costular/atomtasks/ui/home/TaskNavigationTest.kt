package com.costular.atomtasks.ui.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.costular.atomtasks.core.testing.ui.getString
import com.costular.atomtasks.core.ui.R
import com.costular.atomtasks.data.tutorial.OnboardingShownUseCase
import com.costular.atomtasks.data.settings.DefaultTab
import com.costular.atomtasks.preferences.DataStoreModule
import com.costular.atomtasks.tasks.repository.TasksRepository
import com.costular.atomtasks.ui.HiltTestActivity
import com.costular.atomtasks.verticaltasks.VerticalTasksViewModel
import com.costular.designsystem.theme.AtomTheme
import com.google.common.truth.Truth.assertThat
import com.ramcosta.composedestinations.generated.detail.destinations.TaskDetailScreenDestination
import com.ramcosta.composedestinations.generated.search.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.generated.completedtasks.destinations.CompletedTasksScreenDestination
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(DataStoreModule::class)
class TaskNavigationTest {
    private val preferencesScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @BindValue
    val preferences: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = preferencesScope,
        produceFile = {
            InstrumentationRegistry.getInstrumentation().targetContext
                .preferencesDataStoreFile("navigation-${UUID.randomUUID()}")
        },
    )
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
    @Inject
    lateinit var tasks: TasksRepository
    @Inject
    lateinit var onboardingShown: OnboardingShownUseCase
    private lateinit var appState: AtomAppState
    @Volatile
    private var imeBottom = 0

    private val createdTaskIds = mutableListOf<Long>()

    @Test
    fun givenAgendaDefaultWhenHomeOpensThenAgendaIsSelected() {
        showHome(DefaultTab.Agenda)

        assertTabSelected(HomeNavigationDestination.Agenda)
    }

    @Test
    fun givenVerticalDefaultWhenHomeOpensThenVerticalIsSelectedAndTabsRemainNavigable() {
        showHome(DefaultTab.VerticalTasks)

        assertTabSelected(HomeNavigationDestination.VerticalTasks)
        selectTab(HomeNavigationDestination.Agenda)
        assertTabSelected(HomeNavigationDestination.Agenda)
        selectTab(HomeNavigationDestination.VerticalTasks)
        assertTabSelected(HomeNavigationDestination.VerticalTasks)
    }

    @Test
    fun givenHomeWhenSearchOpensThenKeyboardResizesContent() {
        showHome()

        openSearch()
        composeTestRule.waitUntil(10_000) { imeBottom > 0 }

        composeTestRule.onNode(hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun givenHomeWhenSearchOpensThenEmptyResultsMessageIsHidden() {
        showHome()

        openSearch()
        composeTestRule.waitUntil(10_000) { imeBottom > 0 }

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertDoesNotExist()
    }

    @Test
    fun givenSearchKeyboardWhenSearchActionRunsThenKeyboardDismisses() {
        showHome()
        openSearch()
        composeTestRule.waitUntil(10_000) { imeBottom > 0 }

        composeTestRule.onNode(hasSetTextAction()).performImeAction()

        composeTestRule.waitUntil(10_000) { imeBottom == 0 }
    }

    @Test
    fun givenSearchKeyboardWhenSearchActionRunsThenSearchRemainsOpen() {
        showHome()
        openSearch()
        composeTestRule.waitUntil(10_000) { imeBottom > 0 }

        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitUntil(10_000) { imeBottom == 0 }

        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(SearchScreenDestination.route)
        }
    }

    @Test
    fun givenSearchResultsWhenTaskOpensThenDetailsAreShown() = runTest {
        val query = "Search anchor verification"
        val names = (0..15).map { "$query ${it.toString().padStart(2, '0')}" }
        names.forEach { createTask(it) }
        showHome()
        searchFor(query, names.first())
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(8)

        composeTestRule.onNodeWithText(names[8]).performClick()

        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(TaskDetailScreenDestination.route)
        }
    }

    @Test
    fun givenTaskDetailsWhenReturningThenSearchQueryIsRestored() = runTest {
        val query = "Search anchor verification"
        val names = (0..15).map { "$query ${it.toString().padStart(2, '0')}" }
        names.forEach { createTask(it) }
        showHome()
        searchFor(query, names.first())
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(8)
        composeTestRule.onNodeWithText(names[8]).performClick()

        composeTestRule.runOnIdle { appState.navController.popBackStack() }

        composeTestRule.onNode(hasSetTextAction()).assertTextContains(query)
    }

    @Test
    fun givenTaskDetailsWhenReturningThenSearchScrollIsRestored() = runTest {
        val query = "Search anchor verification"
        val names = (0..15).map { "$query ${it.toString().padStart(2, '0')}" }
        names.forEach { createTask(it) }
        showHome()
        searchFor(query, names.first())
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(8)
        composeTestRule.onNodeWithText(names[8]).performClick()

        composeTestRule.runOnIdle { appState.navController.popBackStack() }

        composeTestRule.onNodeWithText(names[8]).assertIsDisplayed()
    }

    @Test
    fun givenSearchResultWhenMarkedDoneThenLiveResultIsSelected() = runTest {
        val name = "Search verification task"
        createTask(name)
        showHome()
        searchFor("verification", name)

        openTaskActions()
        clickText(R.string.agenda_mark_as_done)

        composeTestRule.onNodeWithTag("Markable", useUnmergedTree = true).assertIsSelected()
    }

    @Test
    fun givenSearchResultWhenDeletionIsRequestedThenConfirmationIsShown() = runTest {
        val name = "Search verification task"
        createTask(name)
        showHome()
        searchFor("verification", name)

        openTaskActions()
        clickText(R.string.agenta_delete_task)

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.remove_task_message)).assertIsDisplayed()
    }

    @Test
    fun givenSearchResultWhenDeletionIsConfirmedThenLiveResultsBecomeEmpty() = runTest {
        val name = "Search verification task"
        createTask(name)
        showHome()
        searchFor("verification", name)

        openTaskActions()
        clickText(R.string.agenta_delete_task)
        clickText(R.string.delete)
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(name).fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText(composeTestRule.getString(R.string.search_no_results)).assertIsDisplayed()
    }

    @Test
    fun givenAgendaWhenShownThenCreateButtonIsVisible() {
        showHome()

        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertIsDisplayed()
    }

    @Test
    fun givenAgendaWhenSearchOpensThenCreateButtonIsHidden() {
        showHome()

        openSearch()

        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertDoesNotExist()
    }

    @Test
    fun givenSearchFromAgendaWhenBackPressedThenOriginIsRestored() {
        showHome()
        openSearch()

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.navigate_back)).performClick()

        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(HomeNavigationDestination.Agenda.screen.route)
        }
    }

    @Test
    fun givenVerticalTasksWhenShownThenCreateButtonIsVisible() {
        showHome()
        selectTab(HomeNavigationDestination.VerticalTasks)

        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertIsDisplayed()
    }

    @Test
    fun givenVerticalTasksWhenSearchOpensThenCreateButtonIsHidden() {
        showHome()
        selectTab(HomeNavigationDestination.VerticalTasks)

        openSearch()

        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertDoesNotExist()
    }

    @Test
    fun givenSearchFromVerticalTasksWhenBackPressedThenOriginIsRestored() {
        showHome()
        selectTab(HomeNavigationDestination.VerticalTasks)
        openSearch()

        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.navigate_back)).performClick()

        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(HomeNavigationDestination.VerticalTasks.screen.route)
        }
    }

    @Test
    fun givenHomeWhenSettingsOpensThenCreateButtonIsHidden() {
        showHome()

        selectTab(HomeNavigationDestination.Settings)

        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertDoesNotExist()
    }

    @Test
    fun givenScrolledVerticalListWhenTabIsReopenedThenPinnedDateIsRestored() {
        showHome()
        selectTab(HomeNavigationDestination.VerticalTasks)
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)
        val expectedDay = selectedVerticalDay()

        selectTab(HomeNavigationDestination.Agenda)
        selectTab(HomeNavigationDestination.VerticalTasks)

        assertThat(selectedVerticalDay()).isEqualTo(expectedDay)
    }

    @Test
    fun givenScrolledVerticalListWhenCreateIsClickedThenPinnedDateIsUsed() {
        showHome()
        selectTab(HomeNavigationDestination.VerticalTasks)
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)
        val expectedDay = selectedVerticalDay()

        composeTestRule.onNodeWithTag(CreateTaskFabTag).performClick()

        composeTestRule.runOnIdle {
            val args = TaskDetailScreenDestination.argsFrom(appState.navController.currentBackStackEntry?.arguments)
            assertThat(args.defaultDate).isEqualTo(expectedDay)
        }
    }

    @Test
    fun givenCompletedTasksActionWhenOpenedThenOnlyCompletedTasksAppear() = runTest {
        val completedName = "Completed navigation task"
        val unfinishedName = "Unfinished navigation task"
        createTask(completedName)
        tasks.markTask(createdTaskIds.last(), true)
        createTask(unfinishedName)
        showHome(DefaultTab.VerticalTasks)
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.completed_tasks)).performClick()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(completedName).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(completedName).assertIsDisplayed()
        composeTestRule.onNodeWithText(unfinishedName).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CreateTaskFabTag).assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(CompletedTasksScreenDestination.route)
        }
        composeTestRule.onNodeWithTag("Markable", useUnmergedTree = true).performClick()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(completedName).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun givenScrolledVerticalListWhenReturningFromCompletedTasksThenAnchorIsRestored() {
        showHome(DefaultTab.VerticalTasks)
        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToIndex(40)
        val expectedDay = selectedVerticalDay()
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.completed_tasks)).performClick()
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.navigate_back)).performClick()
        composeTestRule.runOnIdle {
            assertThat(currentRoute()).isEqualTo(HomeNavigationDestination.VerticalTasks.screen.route)
        }
        assertThat(selectedVerticalDay()).isEqualTo(expectedDay)
    }

    @Before
    fun setUp() = runTest {
        hiltRule.inject()
        onboardingShown(Unit)
    }

    @After
    fun tearDown() = runTest {
        try {
            createdTaskIds.forEach { tasks.removeTask(it, null) }
        } finally {
            preferencesScope.cancel()
        }
    }

    private suspend fun createTask(name: String) {
        createdTaskIds += tasks.createTask(name, LocalDate.now(), false, null, null, null)
    }

    private fun showHome(defaultTab: DefaultTab = DefaultTab.Agenda) {
        composeTestRule.setContent {
            AtomTheme {
                val keyboardInset = WindowInsets.ime.getBottom(LocalDensity.current)
                SideEffect { imeBottom = keyboardInset }
                appState = rememberAtomAppState(rememberNavHostEngine().rememberNavController())
                Home(appState, defaultTab = defaultTab)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(500)
    }

    private fun openSearch() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.search_tasks)).performClick()
    }

    private fun searchFor(query: String, expectedResult: String) {
        openSearch()
        composeTestRule.onNode(hasSetTextAction()).performTextInput(query)
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(expectedResult).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openTaskActions() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.getString(R.string.task_more_actions)).performClick()
    }

    private fun clickText(resourceId: Int) {
        composeTestRule.onNodeWithText(composeTestRule.getString(resourceId)).performClick()
    }

    private fun selectTab(destination: HomeNavigationDestination) {
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.getString(destination.contentDescriptionResId),
            useUnmergedTree = true,
        ).performClick()
    }

    private fun assertTabSelected(destination: HomeNavigationDestination) {
        composeTestRule.onNode(
            hasText(composeTestRule.getString(destination.labelResId)).and(isSelectable()),
        )
            .assertIsSelected()
        assertThat(currentRoute()).isEqualTo(destination.screen.route)
    }

    private fun currentRoute() = appState.navController.currentBackStackEntry?.destination?.route

    private fun selectedVerticalDay(): LocalDate = composeTestRule.runOnIdle {
        ViewModelProvider(appState.navController.currentBackStackEntry!!)
            .get(VerticalTasksViewModel::class.java).state.value.selectedDay
    }
}
