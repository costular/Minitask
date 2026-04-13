package com.costular.atomtasks

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.dsl.TestExtension

enum class FlavorDimension(
    val naming: String
) {
    Environment("environment")
}

enum class AtomFlavor(
    val naming: String,
    val dimension: FlavorDimension,
    val applicationIdSuffix: String? = null,
) {
    Development("development", FlavorDimension.Environment, applicationIdSuffix = ".dev"),
    Production("production", FlavorDimension.Environment),
}

enum class AtomBuildType(val applicationIdSuffix: String? = null) {
    DEBUG,
    RELEASE,
    BENCHMARK(".benchmark")
}

fun configureFlavors(
    applicationExtension: ApplicationExtension,
    flavorConfigurationBlock: ProductFlavor.(atomFlavor: AtomFlavor) -> Unit = {},
) {
    applicationExtension.flavorDimensions += FlavorDimension.Environment.naming
    applicationExtension.productFlavors {
        AtomFlavor.values().forEach { flavor ->
            create(flavor.naming) {
                dimension = flavor.dimension.naming
                flavorConfigurationBlock(this, flavor)
                flavor.applicationIdSuffix?.let { applicationIdSuffix = it }
            }
        }
    }
}

fun configureFlavors(
    testExtension: TestExtension,
    flavorConfigurationBlock: ProductFlavor.(atomFlavor: AtomFlavor) -> Unit = {},
) {
    testExtension.flavorDimensions += FlavorDimension.Environment.naming
    testExtension.productFlavors {
        AtomFlavor.values().forEach { flavor ->
            create(flavor.naming) {
                dimension = flavor.dimension.naming
                flavorConfigurationBlock(this, flavor)
            }
        }
    }
}

fun configureBuildTypes(applicationExtension: ApplicationExtension) {
    with(applicationExtension) {
        buildTypes {
            val release = getByName("release") {
                isDebuggable = false
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
                applicationIdSuffix = AtomBuildType.RELEASE.applicationIdSuffix
            }
            getByName("debug") {
                isDebuggable = true
                isMinifyEnabled = false
                isShrinkResources = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
                applicationIdSuffix = AtomBuildType.DEBUG.applicationIdSuffix
            }
            create("benchmark") {
                initWith(release)
                matchingFallbacks.add("release")
                proguardFiles("benchmark-rules.pro")
                isMinifyEnabled = true
                applicationIdSuffix = AtomBuildType.BENCHMARK.applicationIdSuffix
            }
        }
    }
}
