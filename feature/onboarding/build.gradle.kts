plugins {
    id("atomtasks.android.feature")
    alias(libs.plugins.ksp)
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.feature.onboarding"

    ksp {
        arg("compose-destinations.moduleName", "onboarding")
    }
}

dependencies {
    implementation(projects.core.analytics)
    ksp(libs.compose.destinations.ksp)
    implementation(projects.core.locale)
    implementation(projects.common.tasks)

    testImplementation(projects.core.testing)
    testImplementation(libs.android.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.testparameterinjector)
}
