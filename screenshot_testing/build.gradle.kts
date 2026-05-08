plugins {
    id("atomtasks.android.library")
    id("atomtasks.android.library.compose")
    id("atomtasks.detekt")
    alias(libs.plugins.paparazzi)
}

android {
    namespace = "com.costular.atomtasks.screenshot_testing"
}

androidComponents {
    // Disable release builds for this test-only library, no need to run screenshot tests more than
    // once
    beforeVariants(selector().withBuildType("release")) { builder ->
        builder.enable = false
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.ui.tasks)

    testImplementation(projects.data)
    testImplementation(projects.common.tasks)
    testImplementation(libs.compose.ui)
    testImplementation(libs.compose.runtime)
    testImplementation(libs.compose.material3)
    testImplementation(libs.compose.material.icons)
    testImplementation(libs.testparameterinjector)
}

tasks.named("check") {
    dependsOn("verifyPaparazziDemoDebug")
}

tasks.withType<Test>().matching { it.name == "testDebugUnitTest" }.configureEach {
    // Paparazzi verification is run via its dedicated tasks instead of the generic Gradle `test` lifecycle.
    enabled = false
}

tasks.withType<Test>().configureEach {
    // Increase memory for Paparazzi tests
    maxHeapSize = "2g"
}
