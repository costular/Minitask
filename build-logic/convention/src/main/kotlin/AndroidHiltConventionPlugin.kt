import com.costular.atomtasks.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
             pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                "implementation"(libs.findLibrary("hilt").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())
                "ksp"(libs.findLibrary("hilt.ext.compiler").get())
                "kspAndroidTest"(libs.findLibrary("hilt.compiler").get())
                "kspTest"(libs.findLibrary("hilt.compiler").get())
            }

            pluginManager.withPlugin("com.android.application") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
            pluginManager.withPlugin("com.android.library") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
            pluginManager.withPlugin("com.android.test") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
        }
    }
}
