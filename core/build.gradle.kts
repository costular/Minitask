plugins {
    id("atomtasks.android.library")
    id("atomtasks.detekt")
    id("atomtasks.android.hilt")
}

android {
    namespace = "com.costular.atomtasks.core"
}

dependencies {
    api(libs.coroutines)
    testImplementation(libs.truth)
}
