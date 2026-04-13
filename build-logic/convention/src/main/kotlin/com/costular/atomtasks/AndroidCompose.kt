/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.costular.atomtasks

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureAndroidCompose(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.buildFeatures.compose = true
    applicationExtension.testOptions.unitTests.isIncludeAndroidResources = true
    addComposeBomDependencies()
    configureComposeCompiler()
}

internal fun Project.configureAndroidCompose(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.buildFeatures.compose = true
    libraryExtension.testOptions.unitTests.isIncludeAndroidResources = true
    addComposeBomDependencies()
    configureComposeCompiler()
}

private fun Project.addComposeBomDependencies() {
    val composeBom = dependencies.platform(libs.findLibrary("compose.bom").get())
    dependencies {
        add("implementation", composeBom)
        add("testImplementation", composeBom)
        add("androidTestImplementation", composeBom)
    }
}

private fun Project.configureComposeCompiler() {
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        fun Provider<String>.onlyIfTrue() = flatMap { provider { it.takeIf(String::toBoolean) } }
        fun Provider<*>.relativeToRootProject(dir: String) = flatMap {
            rootProject.layout.buildDirectory.dir(projectDir.toRelativeString(rootDir))
        }.map { it.dir(dir) }

        project.providers.gradleProperty("enableComposeCompilerMetrics").onlyIfTrue()
            .relativeToRootProject("compose-metrics")
            .let(metricsDestination::set)

        project.providers.gradleProperty("enableComposeCompilerReports").onlyIfTrue()
            .relativeToRootProject("compose-reports")
            .let(reportsDestination::set)

        stabilityConfigurationFile =
            rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
        enableStrongSkippingMode = true
    }
}
