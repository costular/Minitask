plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core.analytics"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
}
