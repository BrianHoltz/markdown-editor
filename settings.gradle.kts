pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            // Lets a machine-local gradle.properties pin a different plugin
            // version (e.g. when a corporate mirror lags behind the version
            // declared in build.gradle.kts) without touching this repo.
            val override = providers.gradleProperty("${requested.id.id}.versionOverride")
            if (override.isPresent) {
                useVersion(override.get())
            }
        }
    }
}

rootProject.name = "markdown-editor"
