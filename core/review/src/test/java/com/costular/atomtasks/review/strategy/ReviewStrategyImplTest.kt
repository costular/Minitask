package com.costular.atomtasks.review.strategy

import com.costular.atomtasks.data.tasks.TasksDao
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReviewStrategyImplTest {

    private val tasksDao: TasksDao = mockk(relaxUnitFun = true)

    lateinit var sut: ReviewStrategy

    @Before
    fun setUp() {
        sut = ReviewStrategyImpl(tasksDao)
    }

    @Test
    fun `Should return true when executed given completed tasks are equal or higher than 10`() =
        runTest {
            coEvery { tasksDao.getDoneTasksCount() } returns 11

            val result = sut.shouldShowReview()

            assertThat(result).isTrue()
        }

    @Test
    fun `Should return false when executed given completed tasks are lower than 10`() = runTest {
        coEvery { tasksDao.getDoneTasksCount() } returns 9

        val result = sut.shouldShowReview()

        assertThat(result).isFalse()
    }
}
