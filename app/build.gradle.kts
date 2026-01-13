import com.costular.atomtasks.Versioning

plugins {
    id("atomtasks.android.application")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    id("atomtasks.detekt")
    id("jacoco")
    alias(libs.plugins.ksp)
    id("com.android.application")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.costular.atomtasks"

    defaultConfig {
        applicationId = "com.costular.atomtasks"
        versionCode = Versioning.VersionCode
        versionName = Versioning.VersionName
        testInstrumentationRunner = "com.costular.atomtasks.core.testing.AtomTestRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["dagger.hilt.disableModulesHaveInstallInCheck"] = "true"
            }
        }

        packaging {
            // for JNA and JNA-platform
            resources.excludes.add("META-INF/AL2.0")
            resources.excludes.add("META-INF/LGPL2.1")
            resources.excludes.add("META-INF/INDEX.LIST")
            resources.excludes.add("META-INF/DEPENDENCIES")
            // for byte-buddy
            resources.excludes.add("META-INF/licenses/ASM")
            resources.pickFirsts.add("win32-x86-64/attach_hotspot_windows.dll")
            resources.pickFirsts.add("win32-x86/attach_hotspot_windows.dll")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("production") {
            storeFile = rootProject.file("release.keystore")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("production")
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }
}

configurations {
    androidTestImplementation {
        exclude(group = "io.mockk", module = "mockk-agent-jvm")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(projects.core.notifications)
    implementation(projects.core.logging)
    implementation(project(":data"))
    implementation(project(":feature:agenda"))
    implementation(project(":feature:settings"))
    implementation(projects.common.tasks)
    implementation(projects.feature.postponeTask)
    implementation(projects.feature.detail)
    implementation(projects.feature.onboarding)

    implementation(libs.compose.activity)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.windowsize)
    implementation(libs.compose.adaptative.navigation)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.tooling)
    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.lifecycle.compose)
    implementation(libs.viewmodel)
    implementation(libs.hilt)
    implementation(libs.profileinstaller)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.startup)
    implementation(libs.work)

    ksp(libs.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.compose.destinations.core)
    implementation(libs.compose.destinations.bottomsheet)
    ksp(libs.compose.destinations.ksp)

    testImplementation(libs.android.junit)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test)
    testImplementation(libs.mockk)
    testImplementation(libs.compose.ui.test)

    androidTestImplementation(projects.core.testing)

    baselineProfile(projects.benchmarks)
}

baselineProfile {
    // Don't build on every iteration of a full assemble.
    // Instead enable generation directly for the release build variant.
    automaticGenerationDuringBuild = false
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> = listOf("room.schemaLocation=${schemaDir.path}")
}
