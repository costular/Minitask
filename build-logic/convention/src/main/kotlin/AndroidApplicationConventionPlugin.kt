import com.android.build.api.dsl.ApplicationExtension
import com.costular.atomtasks.configureAndroidCompose
import com.costular.atomtasks.configureBuildTypes
import com.costular.atomtasks.configureFlavors
import com.costular.atomtasks.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureFlavors(this)
                configureBuildTypes(this)
                configureAndroidCompose(this)
                defaultConfig.targetSdk = 36
            }
        }
    }
}
