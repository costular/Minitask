plugins {
    id("atomtasks.android.feature")
    alias(libs.plugins.ksp)
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.feature.detail"

    ksp {
        arg("compose-destinations.moduleName", "detail")
    }
}

dependencies {
    implementation(projects.common.tasks)
    implementation(projects.core.analytics)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.accompanist.permissions)

    testImplementation(projects.core.testing)
    testImplementation(libs.android.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.testparameterinjector)
}
