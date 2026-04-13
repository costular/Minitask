package com.costular.atomtasks

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CompileOptions
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

internal fun Project.configureKotlinAndroid(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.compileSdk = 36
    applicationExtension.defaultConfig.minSdk = 26
    configureJavaToolchain()
    applicationExtension.compileOptions.configureJvm21()
    configureAndroidCompilerOptions()
}

internal fun Project.configureKotlinAndroid(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.compileSdk = 36
    libraryExtension.defaultConfig.minSdk = 26
    configureJavaToolchain()
    libraryExtension.compileOptions.configureJvm21()
    configureAndroidCompilerOptions()
}

internal fun Project.configureKotlinAndroid(
    testExtension: TestExtension,
) {
    testExtension.compileSdk = 36
    testExtension.defaultConfig.minSdk = 26
    configureJavaToolchain()
    testExtension.compileOptions.configureJvm21()
    configureAndroidCompilerOptions()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinTopLevelExtension> Project.configureKotlin() = configure<T> {
    jvmToolchain(21)
    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> TODO("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget = JvmTarget.JVM_21
        configureCommonCompilerOptions(project)
    }
}

private fun Project.configureAndroidCompilerOptions() {
    extensions.getByType<KotlinAndroidProjectExtension>().compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        configureCommonCompilerOptions(project)
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android-desugarjdk").get())
    }
}

private fun Project.configureJavaToolchain() {
    extensions.findByType<JavaPluginExtension>()?.toolchain?.languageVersion?.set(JavaLanguageVersion.of(21))
}

private fun CompileOptions.configureJvm21() {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    isCoreLibraryDesugaringEnabled = true
}

private fun KotlinCommonCompilerOptions.configureCommonCompilerOptions(project: Project) {
    val warningsAsErrors: String? by project
    allWarningsAsErrors = warningsAsErrors.toBoolean()
    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
}
