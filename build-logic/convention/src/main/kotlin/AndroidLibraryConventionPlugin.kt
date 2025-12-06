import com.android.build.gradle.LibraryExtension
import com.costular.atomtasks.configureKotlinAndroid
import com.costular.atomtasks.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.gradle.kotlin.dsl.assign

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
            }

            tasks.withType<Test>().configureEach {
                failOnNoDiscoveredTests = false
            }

            dependencies {
                configurations.configureEach {
                    resolutionStrategy {
                        force(libs.findLibrary("junit").get())
                        // Temporary workaround for https://issuetracker.google.com/174733673
                        force("org.objenesis:objenesis:2.6")
                    }
                }
            }
        }
    }

}
