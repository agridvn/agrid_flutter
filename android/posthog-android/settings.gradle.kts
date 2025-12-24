pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Cấu hình toolchain download repositories để tự động tải Java 8
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "PostHog"

include(":posthog")
include(":posthog-android")
include(":posthog-server")

// samples
include(":posthog-samples:posthog-android-sample")
include(":posthog-samples:posthog-java-sample")
include(":posthog-samples:posthog-spring-sample")
