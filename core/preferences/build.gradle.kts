plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.library.jacoco")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core.preferences"
}

dependencies {
    implementation(projects.core)
    api(libs.preferences.datastore)
}
