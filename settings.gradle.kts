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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "markdown-editor"
