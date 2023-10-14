plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
    kotlin("kapt")
}

android {
    namespace = "com.costular.atomtasks.core.notifications"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(projects.core.ui)

    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
}
