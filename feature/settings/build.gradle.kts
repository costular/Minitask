plugins {
    id("atomtasks.android.feature")
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("atomtasks.android.library.ksp")
    id("kotlin-android")
    alias(libs.plugins.ksp)
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.feature.settings"

    ksp {
        arg("compose-destinations.moduleName", "settings")
    }
}

dependencies {
    implementation(projects.core.analytics)
    implementation(projects.common.tasks)
    implementation(project(mapOf("path" to ":feature:agenda")))
    ksp(libs.hilt.compiler)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.compose.ui.manifest)
    implementation(libs.calendar)

    testImplementation(projects.core.testing)
    testImplementation(projects.core.ui)
    testImplementation(libs.android.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test)
}
