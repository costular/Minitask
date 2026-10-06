plugins {
    id("atomtasks.android.feature")
    id("atomtasks.android.hilt")
    id("atomtasks.detekt")
}

android {
    namespace = "com.costular.atomtasks.feature.completedtasks"
    ksp { arg("compose-destinations.moduleName", "completedtasks") }
    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(projects.core.ui.tasks)
    implementation(projects.common.tasks)
    implementation(libs.kotlinx.collections.immutable)
    ksp(libs.compose.destinations.ksp)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.android.junit)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.compose.ui.manifest)
}
