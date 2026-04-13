plugins {
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core.review"
}

dependencies {
    implementation(projects.data)
    implementation(projects.core.ui)
    implementation(projects.core.logging)
    implementation(libs.androidx.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.play.review)

    testImplementation(libs.android.junit)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test)
    testImplementation(libs.mockk)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.testparameterinjector)
}
