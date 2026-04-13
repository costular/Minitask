plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.room")
    alias(libs.plugins.kotlinx.serialization)
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.data"

    defaultConfig {
        testInstrumentationRunner = "com.costular.atomtasks.core.testing.AtomTestRunner"
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(projects.core.preferences)
    implementation(projects.core.notifications)
    implementation(projects.core.logging)

    implementation(libs.work)
    implementation(libs.preferences.datastore)
    implementation(libs.preferences)
    implementation(libs.hilt.work)
    implementation(libs.kotlinx.serialization)

    testImplementation(projects.common.tasks)
    testImplementation(libs.android.junit)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test)
    testImplementation(libs.mockk)
    testImplementation(libs.testparameterinjector)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.android.junit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.test)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.room.testing)
}
