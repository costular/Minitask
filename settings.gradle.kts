pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AtomReminders"
include(":app")
include(":core")
include(":core:designsystem")
include(":core:testing")
include(":core:ui")
include(":common:tasks")
include(":data")
include(":feature:settings")
include(":feature:agenda")
include(":screenshot_testing")
include(":core:analytics")
include(":feature:postpone-task")
include(":core:notifications")
include(":core:logging")
include(":benchmarks")
include(":core:review")
include(":core:preferences")
include(":core:jobs")
include(":core:ui:tasks")
include(":feature:detail")
include(":feature:onboarding")
include(":core:locale")
