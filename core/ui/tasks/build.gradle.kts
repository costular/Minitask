plugins {
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
}

android {
    namespace = "com.costular.atomtasks.core.ui.tasks"

    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.designsystem)
    implementation(projects.common.tasks)

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
