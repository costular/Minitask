import com.android.build.api.dsl.LibraryExtension
import com.costular.atomtasks.configureKotlinAndroid
import com.costular.atomtasks.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.assign

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
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
