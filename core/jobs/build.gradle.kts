plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core.jobs"
}

dependencies {
    implementation(libs.work)

    testImplementation(libs.work.testing)
}
