import com.android.build.api.dsl.LibraryExtension
import com.costular.atomtasks.configureAndroidCompose
import com.costular.atomtasks.configureKotlinAndroid
import com.costular.atomtasks.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            extensions.configure<LibraryExtension>() {
                configureKotlinAndroid(this)
                configureAndroidCompose(this)
                defaultConfig.testInstrumentationRunner =
                    "com.costular.atomtasks.core.testing.AtomTestRunner"
            }

            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":data"))
                add("implementation", libs.findLibrary("compose.foundation").get())
                add("implementation", libs.findLibrary("compose.runtime").get())
                add("implementation", libs.findLibrary("compose.layout").get())
                add("implementation", libs.findLibrary("compose.material3").get())
                add("implementation", libs.findLibrary("compose.material3.windowsize").get())
                add("implementation", libs.findLibrary("compose.material.icons").get())
                add("implementation", libs.findLibrary("compose.ui").get())
                add("implementation", libs.findLibrary("compose.ui.tooling").get())
                add("implementation", libs.findLibrary("viewmodel").get())
                add("implementation", libs.findLibrary("hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("compose.destinations.core").get())
                add("implementation", libs.findLibrary("compose.destinations.bottomsheet").get())

                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", project(":core:testing"))
            }
        }
    }
}
