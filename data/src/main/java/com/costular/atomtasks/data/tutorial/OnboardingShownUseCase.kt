package com.costular.atomtasks.data.tutorial

import com.costular.atomtasks.core.usecase.UseCase
import javax.inject.Inject

class OnboardingShownUseCase @Inject constructor(
    private val tutorialRepository: TutorialRepository
): UseCase<Unit, Unit> {
    override suspend fun invoke(params: Unit) {
        tutorialRepository.onboardingShown()
    }
}
