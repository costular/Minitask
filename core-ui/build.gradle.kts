plugins {
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("kotlin-android")
    kotlin("kapt")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("dagger.hilt.android.plugin")
}

dependencies {
    api(project(":core"))

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.destinations)

    implementation(libs.lifecycle.runtime)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.viewmodel)

    testImplementation(libs.android.junit)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test)
    testImplementation(libs.mockk)
    testImplementation(libs.compose.ui.test)
}