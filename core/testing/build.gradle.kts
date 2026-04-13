plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core.testing"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(platform(libs.compose.bom))

    implementation(libs.hilt.android.testing)
    implementation(libs.androidx.test.runner)

    implementation(libs.android.junit)
    implementation(libs.junit)
    implementation(libs.coroutines.test)
    implementation(libs.androidx.test)
    implementation(libs.compose.ui.test)

    api(libs.truth)
}
