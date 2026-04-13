plugins {
    id("atomtasks.android.feature")
    alias(libs.plugins.ksp)
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.feature.postponetask"

    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

configurations {
    androidTestImplementation {
        exclude(group = "io.mockk", module = "mockk-agent-jvm")
    }
}

dependencies {
    implementation(projects.core.analytics)
    implementation(projects.common.tasks)
    implementation(projects.core.notifications)

    testImplementation(projects.core.testing)
    testImplementation(libs.android.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.android.junit)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.compose.ui.manifest)
}
