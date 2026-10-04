plugins {
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("atomtasks.android.hilt")
    id("kotlin-parcelize")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
}

android {
    namespace = "com.costular.atomtasks.core.ui.tasks"
    defaultConfig.testInstrumentationRunner = "com.costular.atomtasks.core.testing.AtomTestRunner"

    ksp { arg("compose-destinations.moduleName", "taskactions") }

    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.common.tasks)
    implementation(projects.core.review)
    implementation(projects.core.analytics)
    implementation(libs.viewmodel)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.compose.destinations.ksp)

    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    implementation(libs.androidx.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.windowsize)
    implementation(libs.compose.destinations.core)
    implementation(libs.compose.destinations.bottomsheet)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material.icons)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.android.junit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.preferences.datastore)
    androidTestImplementation(libs.hilt.android.testing)

    debugImplementation(libs.compose.ui.manifest)
}
